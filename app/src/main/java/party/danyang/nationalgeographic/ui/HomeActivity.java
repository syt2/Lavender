package party.danyang.nationalgeographic.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;
import com.jakewharton.rxbinding.view.RxView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import me.yokeyword.swipebackfragment.SwipeBackActivity;
import party.danyang.nationalgeographic.BuildConfig;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.adapter.AlbumListAdapter;
import party.danyang.nationalgeographic.adapter.BaseAdapter;
import party.danyang.nationalgeographic.databinding.ActivityHomeBinding;
import party.danyang.nationalgeographic.model.albumlist.Album;
import party.danyang.nationalgeographic.model.albumlist.AlbumList;
import party.danyang.nationalgeographic.model.albumlist.AlbumRealm;
import party.danyang.nationalgeographic.net.NGApi;
import party.danyang.nationalgeographic.utils.BindingAdapters;
import party.danyang.nationalgeographic.utils.NetUtils;
import party.danyang.nationalgeographic.utils.PicassoHelper;
import party.danyang.nationalgeographic.utils.PreferencesHelper;
import party.danyang.nationalgeographic.utils.SettingsModel;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends SwipeBackActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private static final String SP_FIRST_USE = "party.danyang.ng.first_use";

    private ActivityHomeBinding binding;

    private Realm realm;
    private AlbumListAdapter adapter;

    private int page = 1;
    private boolean hasLoad = false;

    private long lastClickTime;

    private CompositeSubscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        mSubscription = new CompositeSubscription();

        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(this).build());
        realm = Realm.getDefaultInstance();

        initViews();
        //第一次使打开则提醒用户Lavender为流量杀手
        if (PreferencesHelper.getInstance(this).getBoolean(SP_FIRST_USE, true)) {
            showAttention();
        }
    }

    private void initViews() {

        setSupportActionBar(binding.toolbar);
        //双击toolbar recyclerView回滚
        RxView.clicks(binding.toolbar).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                onToolbarClicked();
            }
        });
        //toolbar menu事件
        RxToolbar.itemClicks(binding.toolbar).subscribe(new Action1<MenuItem>() {
            @Override
            public void call(MenuItem menuItem) {
                onToolbarMenuItemClicked(menuItem);
            }
        });
        binding.toolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.md_grey_100));
        binding.toolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.md_grey_100));

        adapter = new AlbumListAdapter(null);
        adapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                startDetailActivity(view, position);
            }
        });
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setLayoutManager(layoutManager);
        //滑动是暂停加载
        RxRecyclerView.scrollStateChanges(binding.recycler)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer newState) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            PicassoHelper.getInstance(HomeActivity.this).resumeTag(BindingAdapters.TAG_HOME_ACTIVITY);
                        } else {
                            PicassoHelper.getInstance(HomeActivity.this).pauseTag(BindingAdapters.TAG_HOME_ACTIVITY);
                        }
                    }
                });
        //load more
        RxRecyclerView.scrollEvents(binding.recycler)
                .subscribe(new Action1<RecyclerViewScrollEvent>() {
                    @Override
                    public void call(RecyclerViewScrollEvent recyclerViewScrollEvent) {
                        int[] positions = new int[layoutManager.getSpanCount()];
                        layoutManager.findLastVisibleItemPositions(positions);
                        int maxPosition = positions[0];
                        for (int position : positions) {
                            maxPosition = Math.max(position, maxPosition);
                        }
                        if (maxPosition == layoutManager.getItemCount() - 1) {
                            loadMore();
                        }
                    }
                });
        binding.refresher.setColorSchemeResources(R.color.md_grey_600, R.color.md_grey_800);
        RxSwipeRefreshLayout.refreshes(binding.refresher)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        sendToLoad(1);
                    }
                });
        //init data
        //in wifi from net ,others from realm
        if (NetUtils.isWiFi(this)) {
            sendToLoad(1);
        } else {
            getAlbumFromRealm();
        }
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
        //if wifionly and not in wifi
        if (PreferencesHelper.getInstance(this).getBoolean(SettingsModel.PREF_WIFI_ONLY, false) && !NetUtils.isWiFi(this)) {
            makeSnackBar(R.string.load_not_in_wifi_while_in_wifi_only, true);
            return;
        }
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
                            makeSnackBar(R.string.no_more, true);
                        } else {
                            makeSnackBar(R.string.error, true);
                            if (page >= 2) page--;
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
                            adapter.addAll(albumList.getAlbum());
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
        if (binding.refresher != null) {
            binding.refresher.setRefreshing(isRefresh);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PicassoHelper.getInstance(this).cancelTag(BindingAdapters.TAG_HOME_ACTIVITY);
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
        realm.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        PicassoHelper.getInstance(this).pauseTag(BindingAdapters.TAG_HOME_ACTIVITY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PicassoHelper.getInstance(this).resumeTag(BindingAdapters.TAG_HOME_ACTIVITY);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void startDetailActivity(View v, int i) {
        Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
        intent.putExtra(DetailActivity.INTENT_ALBUM, adapter.get(i));

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
            if (binding.recycler != null) {
                binding.recycler.smoothScrollToPosition(0);
            }
        } else {
            lastClickTime = System.currentTimeMillis();
        }
    }

    private void onToolbarMenuItemClicked(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeCustomAnimation(this, R.anim.slide_right_in, R.anim.slide_right_out);
            ActivityCompat.startActivity(this, intent, options.toBundle());
        }
    }

    private void showAttention() {
        makeSnackBar(R.string.attention, false);
        PreferencesHelper.getInstance(this).edit().putBoolean(SP_FIRST_USE, false).apply();
    }

    private void makeSnackBar(String msg, boolean lengthShort) {
        if (binding != null && binding.getRoot() != null) {
            Snackbar.make(binding.getRoot(), msg, lengthShort ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG).show();
        }
    }

    private void makeSnackBar(int resId, boolean lengthShort) {
        if (binding != null && binding.getRoot() != null) {
            Snackbar.make(binding.getRoot(), resId, lengthShort ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG).show();
        }
    }
}
