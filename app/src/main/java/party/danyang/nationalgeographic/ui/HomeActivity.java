package party.danyang.nationalgeographic.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;
import com.jakewharton.rxbinding.view.RxView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import me.yokeyword.swipebackfragment.SwipeBackActivity;
import party.danyang.nationalgeographic.BuildConfig;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.adapter.AlbumListAdapter;
import party.danyang.nationalgeographic.model.album.PictureRealm;
import party.danyang.nationalgeographic.model.albumlist.Album;
import party.danyang.nationalgeographic.model.albumlist.AlbumList;
import party.danyang.nationalgeographic.model.albumlist.AlbumRealm;
import party.danyang.nationalgeographic.net.NGApi;
import party.danyang.nationalgeographic.utils.PicassoHelper;
import party.danyang.nationalgeographic.utils.Utils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends SwipeBackActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final String SP_FIRST_USE = "party.danyang.ng.first_use";

    private Toolbar toolbar;
    private CollapsingToolbarLayout toolbarLayout;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refresher;

    private Realm realm;
    private AlbumListAdapter adapter;

    private int page = 1;
    private boolean hasLoad = false;

    private long lastClickTime;

    private CompositeSubscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        mSubscription = new CompositeSubscription();
        realm = Realm.getInstance(this);
        initViews();
        //第一次使打开则提醒用户Lavender为流量杀手
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SP_FIRST_USE, true)) {
            showAttention();
        }
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        refresher = (SwipeRefreshLayout) findViewById(R.id.refresher);

        setSupportActionBar(toolbar);
        //双击toolbar recyclerView回滚
        RxView.clicks(toolbar).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                onToolbarClicked();
            }
        });
        //toolbar的menu单击事件
        RxToolbar.itemClicks(toolbar).subscribe(
                new Action1<MenuItem>() {
                    @Override
                    public void call(MenuItem menuItem) {
                        onToolbarMenuItemClicked(menuItem);
                    }
                }
        );
        toolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.md_grey_100));
        toolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.md_grey_100));

        adapter = new AlbumListAdapter(null);
        adapter.setOnRecyclerViewItemClickListener(
                new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                    @Override
                    public void onItemClick(View view, int i) {
                        startDetailActivity(view, i);
                    }
                });
        recyclerView.setAdapter(adapter);
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        RxRecyclerView.scrollStateChanges(recyclerView)
                .subscribe(new Action1<Integer>() {
                               @Override
                               public void call(Integer newState) {
                                   if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                       PicassoHelper.getInstance(HomeActivity.this).resumeTag("1");
                                   } else {
                                       PicassoHelper.getInstance(HomeActivity.this).pauseTag("1");
                                   }
                               }
                           }

                );
        RxRecyclerView.scrollEvents(recyclerView)
                .subscribe(new Action1<RecyclerViewScrollEvent>() {
                               @Override
                               public void call(RecyclerViewScrollEvent recyclerViewScrollEvent) {
                                   int[] positions = new int[layoutManager.getSpanCount()];
                                   layoutManager.findLastVisibleItemPositions(positions);
                                   int position = Math.max(positions[0], positions[1]);
                                   if (position == layoutManager.getItemCount() - 1) {
                                       loadMore();
                                   }
                               }
                           }

                );
        refresher.setColorSchemeResources(R.color.md_grey_600, R.color.md_grey_800);
        RxSwipeRefreshLayout.refreshes(refresher)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        sendToLoad(1);
                    }
                });
        getAlbumFromRealm();
    }

    private void loadMore() {
        if (hasLoad) {
            return;
        }
        page++;
        sendToLoad(page);
    }

    private void getAlbumFromRealm() {
        mSubscription.add(Observable.create(new Observable.OnSubscribe<List<Album>>() {
            @Override
            public void call(Subscriber<? super List<Album>> subscriber) {
                List<AlbumRealm> albums = AlbumRealm.all(realm);
                List<Album> list = new ArrayList<Album>();
                for (AlbumRealm a : albums) {
                    list.add(new Album(a));
                }
                subscriber.onNext(list);
                subscriber.onCompleted();
            }
        }).subscribeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Album>>() {
                    @Override
                    public void call(List<Album> alba) {
                        if (alba != null && alba.size() > 0) {
                            adapter.setNewData(alba);
                        } else {
                            sendToLoad(1);
                        }
                    }
                }));
    }

    private void sendToLoad(int page) {
        this.page = page;
        getAlbumList();
    }

    private void getAlbumList() {
        hasLoad = true;
        setRefresher(true);
        mSubscription.add(NGApi.loadAlbumList(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<AlbumList>() {
                    @Override
                    public void onCompleted() {
                        hasLoad = false;
                        setRefresher(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        hasLoad = false;
                        setRefresher(false);
                        if (BuildConfig.LOG_DEBUG)
                            Log.e(TAG, e.toString());
                        if (e.toString().trim().equals(getString(R.string.notfound404))) {
                            if (refresher != null) {
                                Snackbar.make(refresher, getString(R.string.no_more) + ">.<", Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            if (refresher != null) {
                                Snackbar.make(refresher, getString(R.string.error) + ">.<", Snackbar.LENGTH_SHORT).show();
                                if (page >= 2) page--;
                            }
                        }
                    }

                    @Override
                    public void onNext(AlbumList albumList) {
                        if (albumList == null || albumList.getAlbum() == null || albumList.getAlbum().size() == 0) {
                            if (BuildConfig.LOG_DEBUG)
                                Log.e(TAG, "get albumList data == null and page ==" + page);
                            return;
                        }
                        if (page == 1) {
                            adapter.setNewData(albumList.getAlbum());
                        } else {
                            adapter.addData(albumList.getAlbum());
                        }

                        realm.beginTransaction();
                        for (Album a : albumList.getAlbum()) {
                            realm.copyToRealmOrUpdate(new AlbumRealm(a));
                        }
                        realm.commitTransaction();
                    }
                }));
    }

    private void setRefresher(boolean isRefresh) {
        if (refresher != null) {
            refresher.setRefreshing(isRefresh);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscription == null) {
            mSubscription.unsubscribe();
        }
        realm.close();
    }

    private void startDetailActivity(View v, int i) {
        Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
        intent.putExtra(DetailActivity.INTENT_ALBUM, adapter.getItem(i));

        ImageView imageView = (ImageView) v.findViewById(R.id.iv_album_list);
        Bitmap bitmap = null;
        BitmapDrawable bd = (BitmapDrawable) imageView.getDrawable();
        if (bd != null) {
            bitmap = bd.getBitmap();
        }

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeThumbnailScaleUpAnimation(v, bitmap, 0, 0);
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    private void onToolbarClicked() {
        if (System.currentTimeMillis() - lastClickTime < 300) {
            if (recyclerView != null) {
                recyclerView.smoothScrollToPosition(0);
            }
        } else {
            lastClickTime = System.currentTimeMillis();
        }
    }

    private void onToolbarMenuItemClicked(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_delete_cache) {
            clearCache();
        } else if (id == R.id.action_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setMessage(R.string.about_content);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton(R.string.go_to_github, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(getString(R.string.github_address)));
                    startActivity(intent);
                }
            });
            builder.show();
        } else if (id == R.id.action_crash_report) {

            File file = new File(
                    getExternalCacheDir() + "/Crash.log");


            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.my_email)});
            intent.putExtra(Intent.EXTRA_SUBJECT,
                    getString(R.string.app_name) + " " + getString(R.string.suggest) + "&" + getString(R.string.action_crash_report));
            if (file.exists()) {
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            }
            intent.setType("message/rfc822");
            startActivity(intent);
        } else if (id == R.id.action_set_icon) {
            View v = LayoutInflater.from(HomeActivity.this).inflate(R.layout.dialog_choose_icon, null, false);
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setView(v);
            builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
            RxView.clicks(v.findViewById(R.id.icon1)).subscribe(new Action1<Void>() {
                @Override
                public void call(Void aVoid) {
                    dialog.dismiss();
                    setIcon(R.mipmap.ic_launcher_1);
                }
            });
            RxView.clicks(v.findViewById(R.id.icon2)).subscribe(new Action1<Void>() {
                @Override
                public void call(Void aVoid) {
                    dialog.dismiss();
                    setIcon(R.mipmap.ic_launcher_2);
                }
            });
        }
    }

    private void setIcon(int iconId) {
        PackageManager pm = getPackageManager();
        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);

        pm.setComponentEnabledSetting(
                new ComponentName(this, "party.danyang.nationalgeographic.ui.HomeActivity-icon1"),
                iconId == R.mipmap.ic_launcher_1 ?
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(
                new ComponentName(this, "party.danyang.nationalgeographic.ui.HomeActivity-icon2"),
                iconId == R.mipmap.ic_launcher_2 ?
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void clearCache() {
        mSubscription.add(Utils.deleteFileObservable(getString(R.string.dir_picasso_cache))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        realm.beginTransaction();
                        realm.clear(PictureRealm.class);
                        realm.clear(AlbumRealm.class);
                        realm.commitTransaction();
                        if (recyclerView != null) {
                            Snackbar.make(recyclerView, R.string.cache_cleaned, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }));
    }

    private void showAttention() {
        if (recyclerView != null) {
            Snackbar.make(recyclerView, R.string.attention, Snackbar.LENGTH_LONG).show();
        }
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(SP_FIRST_USE, false).apply();
    }
}
