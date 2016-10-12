package party.danyang.nationalgeographic.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;

import java.util.List;

import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.adapter.AlbumListAdapter;
import party.danyang.nationalgeographic.adapter.base.BaseAdapter;
import party.danyang.nationalgeographic.databinding.LayoutRecyclerBinding;
import party.danyang.nationalgeographic.model.albumlist.Album;
import party.danyang.nationalgeographic.model.albumlist.AlbumList;
import party.danyang.nationalgeographic.net.NGApi;
import party.danyang.nationalgeographic.ui.DetailActivity;
import party.danyang.nationalgeographic.ui.LayoutSpanCountUtils;
import party.danyang.nationalgeographic.utils.NetUtils;
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

public class RecyclerViewTWFragment extends Fragment {

    private HomeActivity activity;

    public AlbumListAdapter adapter;
    public LayoutRecyclerBinding binding;
    public StaggeredGridLayoutManager layoutManager;

    private CompositeSubscription mSubscription;
    private int page = 1;
    private boolean hasLoad = false;

    private static RecyclerViewTWFragment singleton;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (HomeActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSubscription = new CompositeSubscription();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.layout_recycler, container, false);
        setupRecyclerContent();
        initLoad();
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }

    public static RecyclerViewTWFragment getInstance() {
        if (singleton == null) {
            singleton = new RecyclerViewTWFragment();
        }
        return singleton;
    }

    private void initLoad() {
        if (NetUtils.isWiFi(activity)) {
            sendToLoad(1);
        } else {
            getAlbumFromRealm();
        }
    }

    private void sendToLoad(int page) {
        if (!NetUtils.isConnected(activity)) {
            Utils.makeSnackBar(binding.getRoot(), R.string.offline, true);
            Utils.setRefresher(binding.refresher, false);
            return;
        }
        //if wifionly and not in wifi
        if (SettingsModel.getWifiOnly(activity) && !NetUtils.isWiFi(activity)) {
            Utils.makeSnackBar(binding.getRoot(), R.string.load_not_in_wifi_while_in_wifi_only, true);
            Utils.setRefresher(binding.refresher, false);
            return;
        }
        this.page = page;
        getAlbumList();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        layoutManager = new StaggeredGridLayoutManager(
                LayoutSpanCountUtils.getSpanCount(activity, newConfig.orientation)
                , StaggeredGridLayoutManager.VERTICAL);
        binding.recycler.setLayoutManager(layoutManager);
        super.onConfigurationChanged(newConfig);
    }

    private void setupRecyclerContent() {
        binding.setShowErrorView(false);
        binding.errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                sendToLoad(page);
                binding.setShowErrorView(false);
            }
        });

        adapter = new AlbumListAdapter(null);
        adapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                startDetailActivity(view, position);
            }
        });
        layoutManager = new StaggeredGridLayoutManager(
                LayoutSpanCountUtils.getSpanCount(activity, getResources().getConfiguration().orientation)
                , StaggeredGridLayoutManager.VERTICAL);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setLayoutManager(layoutManager);
        //滑动是暂停加载
        RxRecyclerView.scrollStateChanges(binding.recycler)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer newState) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            PicassoHelper.getInstance(getActivity()).resumeTag(AlbumListAdapter.TAG_HOME);
                        } else {
                            PicassoHelper.getInstance(getActivity()).pauseTag(AlbumListAdapter.TAG_HOME);
                        }
                    }
                });
        //load more
        RxRecyclerView.scrollEvents(binding.recycler)
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
        binding.refresher.setColorSchemeResources(R.color.md_grey_600, R.color.md_grey_800);
        RxSwipeRefreshLayout.refreshes(binding.refresher)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        sendToLoad(1);
                    }
                });
    }

    private void getAlbumList() {
        hasLoad = true;
        Utils.setRefresher(binding.refresher, true);
        mSubscription.add(NGApi.loadAlbumList(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<AlbumList>() {
                    @Override
                    public void onCompleted() {
                        hasLoad = false;
                        Utils.setRefresher(binding.refresher, false);
                        binding.setShowErrorView(false);
                        unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hasLoad = false;
                        Utils.setRefresher(binding.refresher, false);
                        binding.setShowErrorView(true);
                        if (e == null || TextUtils.isEmpty(e.getMessage())) {
                            binding.errorView.setTitle(R.string.lalala);
                            binding.errorView.setSubtitle(R.string.error);
                        } else if (e.getMessage().trim().equals(getString(R.string.notfound404))) {
                            binding.errorView.setTitle(R.string.lalala);
                            binding.errorView.setSubtitle(getString(R.string.notfound404) + getString(R.string.maybe_no_more));
                        } else {
                            binding.errorView.setTitle(R.string.lalala);
                            binding.errorView.setSubtitle(e.getMessage());
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
                        Album.updateRealm(activity.realm,albumList.getAlbum());
                    }
                }));
    }

    private void getAlbumFromRealm() {
        mSubscription.add(Observable.create(new Observable.OnSubscribe<List<Album>>() {
            @Override
            public void call(Subscriber<? super List<Album>> subscriber) {
                List<Album> list = Album.all(activity.realm);
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

    private void loadMore() {
        if (hasLoad) {
            return;
        }
        page++;
        sendToLoad(page);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden && mSubscription != null) {
            mSubscription.clear();
            hasLoad = false;
            Utils.setRefresher(binding.refresher, false);
        } else if (!hidden && adapter.size() == 0) {
            initLoad();
        }
    }

    public void startDetailActivity(View v, int i) {
        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra(DetailActivity.INTENT_ALBUM_ID, adapter.get(i).getId());
        intent.putExtra(DetailActivity.INTENT_ALBUM_TITLE, adapter.get(i).getTitle());

        ImageView imageView = (ImageView) v.findViewById(R.id.iv);
        Bitmap bitmap = null;
        BitmapDrawable bd = (BitmapDrawable) imageView.getDrawable();
        if (bd != null) {
            bitmap = bd.getBitmap();
        }

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeThumbnailScaleUpAnimation(v, bitmap, 0, 0);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }
}
