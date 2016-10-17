package party.danyang.nationalgeographic.ui;

import android.Manifest;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;

import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.adapter.AlbumDetailAdapter;
import party.danyang.nationalgeographic.adapter.base.BaseAdapter;
import party.danyang.nationalgeographic.databinding.ActivityDetailBinding;
import party.danyang.nationalgeographic.model.album.AlbumItem;
import party.danyang.nationalgeographic.model.album.Picture;
import party.danyang.nationalgeographic.net.NGApi;
import party.danyang.nationalgeographic.ui.base.ToolbarActivity;
import party.danyang.nationalgeographic.utils.DownloadMangerResolver;
import party.danyang.nationalgeographic.utils.NetUtils;
import party.danyang.nationalgeographic.utils.SaveImage;
import party.danyang.nationalgeographic.utils.SettingsModel;
import party.danyang.nationalgeographic.utils.Utils;
import party.danyang.nationalgeographic.utils.singleton.PicassoHelper;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import tr.xip.errorview.ErrorView;

public class DetailActivity extends ToolbarActivity {
    public static final String TAG = DetailActivity.class.getSimpleName();
    public static final String INTENT_ALBUM_ID = "party.danyang.ng.album.id";
    public static final String INTENT_ALBUM_TITLE = "party.danyang.ng.album.title";

    private ActivityDetailBinding binding;

    //    private Album album;
    private String id;
    private String title;

    private Realm realm;
    private AlbumDetailAdapter adapter;
    private CompositeSubscription mSubscription;
    StaggeredGridLayoutManager layoutManager;

    private Bundle reenterState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);
        Intent intent = getIntent();
        if (intent != null) {
            id = intent.getStringExtra(INTENT_ALBUM_ID);
            title = intent.getStringExtra(INTENT_ALBUM_TITLE);
        }

        mSubscription = new CompositeSubscription();

        realm = Realm.getDefaultInstance();

        initViews();

        setExitAnimator();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        PicassoHelper.getInstance(this).resumeTag(AlbumDetailAdapter.TAG_DETAIL);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        PicassoHelper.getInstance(this).pauseTag(AlbumDetailAdapter.TAG_DETAIL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PicassoHelper.getInstance(this).cancelTag(AlbumDetailAdapter.TAG_DETAIL);
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
        realm.close();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        layoutManager = new StaggeredGridLayoutManager(
                LayoutSpanCountUtils.getSpanCount(this, newConfig.orientation)
                , StaggeredGridLayoutManager.VERTICAL);
        binding.recyclerContent.recycler.setLayoutManager(layoutManager);
        super.onConfigurationChanged(newConfig);
    }

    private void initViews() {
        setupToolbar(binding.toolbarContent);
        binding.toolbarContent.toolbarLayout.setTitle(title);

        binding.recyclerContent.setShowErrorView(false);
        binding.recyclerContent.errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                getAlbum();
                binding.recyclerContent.setShowErrorView(false);
            }
        });

        adapter = new AlbumDetailAdapter(new ArrayList<Picture>());
        adapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                startAlbumActivity(view, position);
            }
        });
        layoutManager = new StaggeredGridLayoutManager(
                LayoutSpanCountUtils.getSpanCount(this, getResources().getConfiguration().orientation)
                , StaggeredGridLayoutManager.VERTICAL);
        binding.recyclerContent.recycler.setAdapter(adapter);
        binding.recyclerContent.recycler.setLayoutManager(layoutManager);
        RxRecyclerView.scrollStateChanges(binding.recyclerContent.recycler).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                if (integer == RecyclerView.SCROLL_STATE_IDLE) {
                    PicassoHelper.getInstance(DetailActivity.this).resumeTag(AlbumDetailAdapter.TAG_DETAIL);
                } else {
                    PicassoHelper.getInstance(DetailActivity.this).pauseTag(AlbumDetailAdapter.TAG_DETAIL);
                }
            }
        });
        load();
        RxView.clicks(binding.fab)//点击fab
                .compose(RxPermissions.getInstance(this).ensure(Manifest.permission.WRITE_EXTERNAL_STORAGE))//检查权限
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {//拥有该权限
                            if (adapter.size() <= 0 || TextUtils.isEmpty(adapter.get(0).getUrl())) {
                                Utils.makeSnackBar(binding.getRoot(), R.string.exception_content_null, true);
                                return;
                            }
                            saveAllImg();
                        } else {//拒绝该权限
                            Utils.makeSnackBar(binding.getRoot(), R.string.permission_denied, true);
                        }
                    }
                });

        binding.recyclerContent.refresher.setEnabled(false);
    }

    private void saveAllImg() {
        if (!NetUtils.isConnected(this)) {
            Utils.makeSnackBar(binding.getRoot(), R.string.offline, true);
            return;
        }
        //if wifionly and not in wifi
        if (SettingsModel.getWifiOnly(this) && !NetUtils.isWiFi(this)) {
            Utils.makeSnackBar(binding.getRoot(), R.string.load_not_in_wifi_while_in_wifi_only, true);
            return;
        }

        if (DownloadMangerResolver.resolve(this))
            for (int i = 0; i < adapter.size(); i++) {
                SaveImage.saveImg(this, binding.getRoot(), adapter.get(i).getAlbumid() + "_" + i + ".jpg", adapter.get(i).getUrl());
            }
    }

    private void load() {
        getAlbumFromRealm();
    }

    private void getAlbumFromRealm() {
        mSubscription.add(Observable.create(new Observable.OnSubscribe<List<Picture>>() {
            @Override
            public void call(Subscriber<? super List<Picture>> subscriber) {
                List<Picture> list = Picture.all(realm, id);
                subscriber.onNext(list);
                subscriber.onCompleted();
            }
        }).subscribeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Picture>>() {
                    @Override
                    public void call(List<Picture> pictures) {
                        if (pictures != null && pictures.size() > 0) {
                            adapter.setNewData(pictures);
                        } else {
                            getAlbum();
                        }
                    }
                }));
    }

    private void getAlbum() {
        if (!NetUtils.isConnected(this)) {
            Utils.makeSnackBar(binding.getRoot(), R.string.offline, true);
            return;
        }
        //if wifionly and not in wifi
        if (SettingsModel.getWifiOnly(this) && !NetUtils.isWiFi(this)) {
            Utils.makeSnackBar(binding.getRoot(), R.string.load_not_in_wifi_while_in_wifi_only, true);
            return;
        }

        Utils.setRefresher(binding.recyclerContent.refresher, true);
        mSubscription.add(NGApi.loadAlbum(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<AlbumItem>() {
                    @Override
                    public void onCompleted() {
                        Utils.setRefresher(binding.recyclerContent.refresher, false);
                        binding.recyclerContent.setShowErrorView(false);
                        unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.setRefresher(binding.recyclerContent.refresher, false);
                        binding.recyclerContent.setShowErrorView(true);
                        if (e == null || TextUtils.isEmpty(e.getMessage())) {
                            binding.recyclerContent.errorView.setTitle(R.string.lalala);
                            binding.recyclerContent.errorView.setSubtitle(R.string.error);
                        } else {
                            binding.recyclerContent.errorView.setTitle(R.string.lalala);
                            binding.recyclerContent.errorView.setSubtitle(e.getMessage());
                        }
                        unsubscribe();
                    }

                    @Override
                    public void onNext(AlbumItem albumItem) {
                        if (albumItem == null || albumItem.getPicture() == null || albumItem.getPicture().size() == 0) {
                            onError(new Exception(getString(R.string.exception_content_null)));
                        }
                        adapter.setNewData(albumItem.getPicture());
                        Picture.updateRealm(realm, albumItem.getPicture());
                    }
                }));
    }

    private void startAlbumActivity(View v, int i) {
        Intent intent = new Intent(DetailActivity.this, AlbumActivity.class);
        List<Picture> pictures = adapter.getList();
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<String> contents = new ArrayList<>();
        ArrayList<String> authors = new ArrayList<>();
        ArrayList<String> urls = new ArrayList<>();
        ArrayList<String> pageUrls = new ArrayList<>();

        for (Picture p : pictures) {
            titles.add(p.getTitle());
            contents.add(p.getContent());
            authors.add(p.getAuthor());
            urls.add(p.getUrl());
            pageUrls.add(p.getYourshotlink());
        }
        intent.putStringArrayListExtra(AlbumActivity.INTENT_TITLES, titles);
        intent.putStringArrayListExtra(AlbumActivity.INTENT_CONTENTS, contents);
        intent.putStringArrayListExtra(AlbumActivity.INTENT_AUTHORS, authors);
        intent.putStringArrayListExtra(AlbumActivity.INTENT_URLS, urls);
        intent.putStringArrayListExtra(AlbumActivity.INTENT_PAGE_URLS, pageUrls);
        intent.putExtra(AlbumActivity.INTENT_INDEX, i);

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, v, adapter.get(i).getUrl());
        ActivityCompat.startActivity(this, intent, options.toBundle());

    }

    private void setExitAnimator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setExitSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    if (reenterState != null) {
                        int position = reenterState.getInt(AlbumActivity.INTENT_INDEX, 0);
                        if (adapter == null || position >= adapter.size())
                            return;
                        sharedElements.clear();
                        sharedElements.put(adapter.get(position).getUrl(), layoutManager.findViewByPosition(position));
                        reenterState = null;
                    }
                }
            });
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        reenterState = new Bundle(data.getExtras());
        int position = reenterState.getInt(AlbumActivity.INTENT_INDEX, 0);
        if (binding == null || position >= adapter.size()) return;
        supportPostponeEnterTransition();
        binding.recyclerContent.recycler.scrollToPosition(position);
        binding.recyclerContent.recycler.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                binding.recyclerContent.recycler.getViewTreeObserver().removeOnPreDrawListener(this);
                binding.recyclerContent.recycler.requestLayout();
                supportStartPostponedEnterTransition();
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }
}
