package party.danyang.nationalgeographic.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
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
import party.danyang.nationalgeographic.utils.SettingsModel;
import party.danyang.nationalgeographic.utils.singleton.PicassoHelper;
import party.danyang.nationalgeographic.utils.singleton.PreferencesHelper;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import tr.xip.errorview.ErrorView;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private static final String SP_FIRST_USE = "party.danyang.ng.first_use";

    private ActivityHomeBinding binding;

    private Realm realm;
    private AlbumListAdapter adapter;

    private int page = 1;
    private boolean hasLoad = false;

    private long lastClickTime;

    private CompositeSubscription mSubscription;


    private CollapsingToolbarLayoutState state;

    private enum CollapsingToolbarLayoutState {
        EXPANDED,
        COLLAPSED,
        INTERNEDIATE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        mSubscription = new CompositeSubscription();

        realm = Realm.getDefaultInstance();

        initViews();
        //第一次打开则提醒用户Lavender为流量杀手
        if (PreferencesHelper.getInstance(this).getBoolean(SP_FIRST_USE, true)) {
            showAttention();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        PicassoHelper.getInstance(this).resumeTag(BindingAdapters.TAG_HOME_ACTIVITY);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        PicassoHelper.getInstance(this).pauseTag(BindingAdapters.TAG_HOME_ACTIVITY);
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

    private void initViews() {
        setupToolbar();
        setupDrawer();
        setupRecyclerContent();
        //init data
        //in wifi from net ,others from realm
        if (NetUtils.isWiFi(this)) {
            sendToLoad(1);
        } else {
            getAlbumFromRealm();
        }
    }

    public void setupToolbar() {
        setSupportActionBar(binding.toolbarContent.toolbar);
        //双击toolbar recyclerView回滚
        RxView.clicks(binding.toolbarContent.toolbar).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                onToolbarClicked();
            }
        });
    }

    private void setupDrawer() {
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbarContent.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setCheckedItem(R.id.nav_group_pic);

        binding.navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                onNavItemSelected(item);
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

//        RxView.longClicks(binding.navView.getHeaderView(0).findViewById(R.id.nav_img)).subscribe(new Action1<Void>() {
//            @Override
//            public void call(Void aVoid) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/*");
//                intent.putExtra("crop", true);
//                intent.putExtra("scale", true);
//                intent.putExtra("aspectX", 28);
//                intent.putExtra("aspectY", 16);
//                intent.putExtra("return-data", true);
//                startActivityForResult(intent, 17);
//            }
//        });

        binding.toolbarContent.appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == 0) {
                    if (state != CollapsingToolbarLayoutState.EXPANDED) {
                        state = CollapsingToolbarLayoutState.EXPANDED;//修改状态标记为展开
                        toggle.setDrawerIndicatorEnabled(false);
                    }
                } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
                    if (state != CollapsingToolbarLayoutState.COLLAPSED) {
                        //折叠
                        toggle.setDrawerIndicatorEnabled(true);
                        state = CollapsingToolbarLayoutState.COLLAPSED;//修改状态标记为折叠
                    }
                } else {
                    if (state != CollapsingToolbarLayoutState.INTERNEDIATE) {
                        if (state == CollapsingToolbarLayoutState.COLLAPSED) {
                            //由折叠变为中间状态时
                            toggle.setDrawerIndicatorEnabled(false);
                        }
                        toggle.setDrawerIndicatorEnabled(false);
                        state = CollapsingToolbarLayoutState.INTERNEDIATE;//修改状态标记为中间
                    }
                }
            }
        });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == 17 && resultCode == RESULT_OK) {
//            Uri uri = data.getData();
//            Log.e("uri", uri.toString());
//            Drawable drawable = Drawable.createFromPath(Utils.getRealFilePath(this, uri));
//            ((LinearLayout) binding.navView.getHeaderView(0).findViewById(R.id.nav_img)).setBackground(drawable);
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    private void onNavItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_single_pic:
                break;
            case R.id.nav_single_pic_us:
                Intent intent2 = new Intent(this, AlbumUSActivity.class);
                ActivityOptionsCompat options2 = ActivityOptionsCompat
                        .makeCustomAnimation(this, R.anim.slide_right_in, R.anim.slide_right_out);
                ActivityCompat.startActivity(this, intent2, options2.toBundle());
                break;
            case R.id.nav_group_pic:
                break;
            case R.id.nav_article:
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeCustomAnimation(this, R.anim.slide_right_in, R.anim.slide_right_out);
                ActivityCompat.startActivity(this, intent, options.toBundle());
                break;
            case R.id.nav_about:
                Intent i = new Intent(this, AboutActivity.class);
                ActivityOptionsCompat opts = ActivityOptionsCompat
                        .makeCustomAnimation(this, R.anim.slide_right_in, R.anim.slide_right_out);
                ActivityCompat.startActivity(this, i, opts.toBundle());
                break;
        }
    }

    private void setupRecyclerContent() {
        binding.recyclerContent.setShowErrorView(false);
        binding.recyclerContent.errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                sendToLoad(page);
                binding.recyclerContent.setShowErrorView(false);
            }
        });

        adapter = new AlbumListAdapter(null);
        adapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                startDetailActivity(view, position);
            }
        });
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        binding.recyclerContent.recycler.setAdapter(adapter);
        binding.recyclerContent.recycler.setLayoutManager(layoutManager);
        //滑动是暂停加载
        RxRecyclerView.scrollStateChanges(binding.recyclerContent.recycler)
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
        RxRecyclerView.scrollEvents(binding.recyclerContent.recycler)
                .subscribe(new Action1<RecyclerViewScrollEvent>() {
                    @Override
                    public void call(RecyclerViewScrollEvent recyclerViewScrollEvent) {
                        int[] positions = new int[layoutManager.getSpanCount()];
                        layoutManager.findLastCompletelyVisibleItemPositions(positions);
                        int maxPosition = positions[0];
                        for (int position : positions) {
                            maxPosition = Math.max(position, maxPosition);
                        }
                        if (maxPosition == layoutManager.getItemCount() - 1) {
                            loadMore();
                        }
                    }
                });
        binding.recyclerContent.refresher.setColorSchemeResources(R.color.md_grey_600, R.color.md_grey_800);
        RxSwipeRefreshLayout.refreshes(binding.recyclerContent.refresher)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        sendToLoad(1);
                    }
                });
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
        if (!NetUtils.isConnected(this)) {
            makeSnackBar(R.string.offline, true);
            setRefresher(false);
            return;
        }
        //if wifionly and not in wifi
        if (PreferencesHelper.getInstance(this).getBoolean(SettingsModel.PREF_WIFI_ONLY, false) && !NetUtils.isWiFi(this)) {
            makeSnackBar(R.string.load_not_in_wifi_while_in_wifi_only, true);
            setRefresher(false);
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
                        binding.recyclerContent.setShowErrorView(false);
                        unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hasLoad = false;
                        setRefresher(false);
                        binding.recyclerContent.setShowErrorView(true);
                        if (e == null) {
                            binding.recyclerContent.errorView.setTitle(R.string.lalala);
                            binding.recyclerContent.errorView.setSubtitle(R.string.error);
                        } else if (e.getMessage().trim().equals(getString(R.string.notfound404))) {
                            binding.recyclerContent.errorView.setTitle(R.string.lalala);
                            binding.recyclerContent.errorView.setSubtitle(getString(R.string.notfound404) + getString(R.string.maybe_no_more));
                        } else {
                            binding.recyclerContent.errorView.setTitle(R.string.lalala);
                            binding.recyclerContent.errorView.setSubtitle(e.getMessage());
                        }
                        unsubscribe();
                    }

                    @Override
                    public void onNext(AlbumList albumList) {
                        if (albumList == null || albumList.getAlbum() == null || albumList.getAlbum().size() == 0) {
                            onError(new Exception(getString(R.string.exception_content_null)));
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

    private void startDetailActivity(View v, int i) {
        Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
        intent.putExtra(DetailActivity.INTENT_ALBUM, adapter.get(i));

        ImageView imageView = (ImageView) v.findViewById(R.id.iv_album_list);
//        ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, 0, 0);
        Bitmap bitmap = null;
        BitmapDrawable bd = (BitmapDrawable) imageView.getDrawable();
        if (bd != null) {
            bitmap = bd.getBitmap();
        }

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeThumbnailScaleUpAnimation(v, bitmap, 0, 0);
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    private void onToolbarClicked() {
        if (System.currentTimeMillis() - lastClickTime < 300) {
            if (binding.recyclerContent.recycler != null) {
                binding.recyclerContent.recycler.smoothScrollToPosition(0);
            }
        } else {
            lastClickTime = System.currentTimeMillis();
        }
    }

    private void showAttention() {
        makeSnackBar(R.string.attention, false);
        PreferencesHelper.getInstance(this).edit().putBoolean(SP_FIRST_USE, false).apply();
    }

    private void makeSnackBar(String msg, boolean lengthShort) {
        if (binding != null && binding.getRoot() != null) {
            Snackbar snackbar = Snackbar.make(binding.getRoot(), msg, lengthShort ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundResource(R.color.colorPrimary);
            snackbar.show();
        }
    }

    private void makeSnackBar(int resId, boolean lengthShort) {
        makeSnackBar(getString(resId), lengthShort);
    }

    private void setRefresher(final boolean isRefresh) {
        binding.recyclerContent.refresher.post(new Runnable() {
            @Override
            public void run() {
                if (binding.recyclerContent.refresher != null) {
                    binding.recyclerContent.refresher.setRefreshing(isRefresh);
                }
            }
        });
    }
}
