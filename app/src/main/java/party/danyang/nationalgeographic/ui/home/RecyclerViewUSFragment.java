package party.danyang.nationalgeographic.ui.home;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.adapter.AlbumListUSAdapter;
import party.danyang.nationalgeographic.adapter.BaseAdapter;
import party.danyang.nationalgeographic.databinding.LayoutRecyclerBinding;
import party.danyang.nationalgeographic.model.album_us.AlbumList;
import party.danyang.nationalgeographic.model.album_us.Items;
import party.danyang.nationalgeographic.model.album_us.ItemsRealm;
import party.danyang.nationalgeographic.net.NGApi_US;
import party.danyang.nationalgeographic.ui.AlbumActivity;
import party.danyang.nationalgeographic.utils.NetUtils;
import party.danyang.nationalgeographic.utils.SettingsModel;
import party.danyang.nationalgeographic.utils.Utils;
import party.danyang.nationalgeographic.utils.singleton.PicassoHelper;
import party.danyang.nationalgeographic.utils.singleton.PreferencesHelper;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import tr.xip.errorview.ErrorView;

public class RecyclerViewUSFragment extends Fragment {

    private static final String REPLACE_TEXT = "This photo was submitted to Your Shot, our storytelling community where members can take part in photo assignments, get expert feedback, be published, and more. Join now >>";

    private HomeActivity activity;
    public AlbumListUSAdapter adapter;
    public LayoutRecyclerBinding binding;
    public StaggeredGridLayoutManager layoutManager;

    private int year;
    private int month;
    private boolean hasLoad = false;

    private static RecyclerViewUSFragment singleton;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (HomeActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.layout_recycler, container, false);
        setupRecyclerContent();
        if (NetUtils.isWiFi(activity)) {
            sendToLoad(Utils.getYearOfNow(), Utils.getMonthOfNow());
        } else {
            getAlbumFromRealm();
        }
        return binding.getRoot();
    }

    public static RecyclerViewUSFragment getInstance() {
        if (singleton == null) {
            singleton = new RecyclerViewUSFragment();
        }
        return singleton;
    }

    private void sendToLoad(int year, int month) {
        if (!NetUtils.isConnected(activity)) {
            Utils.makeSnackBar(binding.getRoot(), R.string.offline, true);
            Utils.setRefresher(binding.refresher, false);
            return;
        }
        if (PreferencesHelper.getInstance(getActivity()).getBoolean(SettingsModel.PREF_WIFI_ONLY, false) && !NetUtils.isWiFi(activity)) {
            Utils.makeSnackBar(binding.getRoot(), R.string.load_not_in_wifi_while_in_wifi_only, true);
            Utils.setRefresher(binding.refresher, false);
            return;
        }
        this.year = year;
        this.month = month;
        getAlbumUSList();
    }

    private void setupRecyclerContent() {
        binding.setShowErrorView(false);
        binding.errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                sendToLoad(year, month);
                binding.setShowErrorView(false);
            }
        });

        adapter = new AlbumListUSAdapter(null);
        adapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                startAlbumActivity(view, position);
            }
        });
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setLayoutManager(layoutManager);
        //滑动是暂停加载
        RxRecyclerView.scrollStateChanges(binding.recycler)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer newState) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            PicassoHelper.getInstance(getActivity()).resumeTag(AlbumListUSAdapter.TAG_LIST_US);
                        } else {
                            PicassoHelper.getInstance(getActivity()).pauseTag(AlbumListUSAdapter.TAG_LIST_US);
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
                        sendToLoad(Utils.getYearOfNow(), Utils.getMonthOfNow());
                    }
                });
    }

    private void getAlbumUSList() {
        hasLoad = true;
        Utils.setRefresher(binding.refresher, true);
        activity.mSubscription.add(NGApi_US.loadPictures(year, month)
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
                        if (e == null) {
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
                        if (albumList == null || albumList.getItems() == null || albumList.getItems().size() == 0) {
                            onError(new Exception(getString(R.string.exception_content_null)));
                        }
                        if (year == Utils.getYearOfNow() && month == Utils.getMonthOfNow()) {
                            adapter.setNewData(albumList.getItems());
                        } else {
                            adapter.addAll(albumList.getItems());
                        }

                        activity.realm.beginTransaction();
                        for (Items a : albumList.getItems()) {
                            activity.realm.copyToRealmOrUpdate(new ItemsRealm(a));
                        }
                        activity.realm.commitTransaction();
                    }
                }));
    }

    private void getAlbumFromRealm() {
        activity.mSubscription.add(Observable.create(new Observable.OnSubscribe<List<Items>>() {
            @Override
            public void call(Subscriber<? super List<Items>> subscriber) {
                List<ItemsRealm> albums = ItemsRealm.all(activity.realm);
                List<Items> list = new ArrayList<>();
                for (ItemsRealm a : albums) {
                    list.add(new Items(a));
                }
                subscriber.onNext(list);
                subscriber.onCompleted();
            }
        }).subscribeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Items>>() {
                    @Override
                    public void call(List<Items> alba) {
                        if (alba != null && alba.size() > 0) {
                            adapter.setNewData(alba);
                        } else {
                            sendToLoad(Utils.getYearOfNow(), Utils.getMonthOfNow());
                        }
                    }
                }));
    }

    private void loadMore() {
        if (hasLoad) {
            return;
        }
        month--;
        if (month == 0) {
            year--;
            month = 12;
        }
        sendToLoad(year, month);
    }

    private void startAlbumActivity(View v, int i) {
        Intent intent = new Intent(activity, AlbumActivity.class);
        List<Items> pictures = adapter.getList();
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<String> contents = new ArrayList<>();
        ArrayList<String> authors = new ArrayList<>();
        ArrayList<String> urls = new ArrayList<>();
        ArrayList<String> pageUrls = new ArrayList<>();

        for (Items p : pictures) {
            titles.add(p.getTitle());
//            contents.add(HtmlUtils.getTextFromHtml(p.getCaption()));
            authors.add(p.getPublishDate());
            urls.add(p.getUrl());
            pageUrls.add(p.getPageUrl());
            Document doc = Jsoup.parse(p.getCaption());
            String content = doc.text().replace(
                    REPLACE_TEXT, "");
            contents.add(content);
        }
        intent.putStringArrayListExtra(AlbumActivity.INTENT_TITLES, titles);
        intent.putStringArrayListExtra(AlbumActivity.INTENT_CONTENTS, contents);
        intent.putStringArrayListExtra(AlbumActivity.INTENT_AUTHORS, authors);
        intent.putStringArrayListExtra(AlbumActivity.INTENT_URLS, urls);
        intent.putStringArrayListExtra(AlbumActivity.INTENT_PAGE_URLS, pageUrls);
        intent.putExtra(AlbumActivity.INTENT_INDEX, i);

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity, v, adapter.get(i).getUrl());
        ActivityCompat.startActivity(activity, intent, options.toBundle());

    }
}
