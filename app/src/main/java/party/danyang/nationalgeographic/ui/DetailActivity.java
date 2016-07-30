package party.danyang.nationalgeographic.ui;

import android.Manifest;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import me.yokeyword.swipebackfragment.SwipeBackActivity;
import party.danyang.nationalgeographic.BuildConfig;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.adapter.AlbumDetailAdapter;
import party.danyang.nationalgeographic.adapter.BaseAdapter;
import party.danyang.nationalgeographic.databinding.ActivityDetailBinding;
import party.danyang.nationalgeographic.model.album.AlbumItem;
import party.danyang.nationalgeographic.model.album.Picture;
import party.danyang.nationalgeographic.model.album.PictureRealm;
import party.danyang.nationalgeographic.model.albumlist.Album;
import party.danyang.nationalgeographic.net.NGApi;
import party.danyang.nationalgeographic.utils.BindingAdapters;
import party.danyang.nationalgeographic.utils.NetUtils;
import party.danyang.nationalgeographic.utils.singleton.PicassoHelper;
import party.danyang.nationalgeographic.utils.singleton.PreferencesHelper;
import party.danyang.nationalgeographic.utils.SettingsModel;
import party.danyang.nationalgeographic.utils.Utils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class DetailActivity extends SwipeBackActivity {
    public static final String TAG = DetailActivity.class.getSimpleName();
    public static final String INTENT_ALBUM = "party.danyang.ng.album";

    private ActivityDetailBinding binding;

    private Album album;

    private Realm realm;
    private AlbumDetailAdapter adapter;
    private CompositeSubscription mSubscription;
    StaggeredGridLayoutManager layoutManager;

    private Bundle reenterState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);
//        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        if (intent != null) {
            album = intent.getParcelableExtra(INTENT_ALBUM);
        }

        mSubscription = new CompositeSubscription();

        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(this).build());
        realm = Realm.getDefaultInstance();

        initViews();

        setExitAnimator();
    }

    private void setExitAnimator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setExitSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    if (reenterState != null) {
                        int position = reenterState.getInt(AlbumActivity.INTENT_INDEX, 0);
                        sharedElements.clear();
                        sharedElements.put(adapter.get(position).getUrl(), layoutManager.findViewByPosition(position));
                        reenterState = null;
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PicassoHelper.getInstance(this).cancelTag(BindingAdapters.TAG_DETAIL_ACTIVITY);
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
        realm.close();
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

    @Override
    protected void onStop() {
        super.onStop();
        PicassoHelper.getInstance(this).pauseTag(BindingAdapters.TAG_DETAIL_ACTIVITY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PicassoHelper.getInstance(this).resumeTag(BindingAdapters.TAG_DETAIL_ACTIVITY);
    }

    private void initViews() {
        setSupportActionBar(binding.toolbar);
        binding.toolbarLayout.setTitle(album.getTitle());

        adapter = new AlbumDetailAdapter(new ArrayList<Picture>());
        adapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                startAlbumActivity(view, position);
            }
        });
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setLayoutManager(layoutManager);
        RxRecyclerView.scrollStateChanges(binding.recycler).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                if (integer == RecyclerView.SCROLL_STATE_IDLE) {
                    PicassoHelper.getInstance(DetailActivity.this).resumeTag(BindingAdapters.TAG_DETAIL_ACTIVITY);
                } else {
                    PicassoHelper.getInstance(DetailActivity.this).pauseTag(BindingAdapters.TAG_DETAIL_ACTIVITY);
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
                            saveAllImg();
                        } else {//拒绝该权限
                            makeSnackBar(R.string.permission_denied, true);
                        }
                    }
                });
    }

    private void saveAllImg() {
        if (adapter.getList().size() <= 0) {
            return;
        }
        for (int i = 0; i < adapter.getList().size(); i++) {
            mSubscription.add(Utils.saveImageAndGetPathObservable(DetailActivity.this, adapter.getList().get(i).getUrl(),
                    adapter.getList().get(i).getAlbumid() + "_" + i)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Uri>() {
                        @Override
                        public void onCompleted() {
                            File appDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
                            String msg = String.format(getString(R.string.save_in_file), appDir.getAbsolutePath());
                            makeSnackBar(msg, true);
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (BuildConfig.LOG_DEBUG)
                                Log.e("saveAllImg", e.toString());
                            makeSnackBar(e.toString(), true);
                        }

                        @Override
                        public void onNext(Uri uri) {

                        }
                    }));
        }
    }

    private void load() {
        getAlbumFromRealm();
    }

    private void getAlbumFromRealm() {
        mSubscription.add(Observable.create(new Observable.OnSubscribe<List<Picture>>() {
            @Override
            public void call(Subscriber<? super List<Picture>> subscriber) {
                List<PictureRealm> pictures = PictureRealm.all(realm, album.getId());
                List<Picture> list = new ArrayList<Picture>();
                for (PictureRealm p : pictures) {
                    list.add(new Picture(p));
                }
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
        //if wifionly and not in wifi
        if (PreferencesHelper.getInstance(this).getBoolean(SettingsModel.PREF_WIFI_ONLY, false) && !NetUtils.isWiFi(this)) {
            makeSnackBar(R.string.load_not_in_wifi_while_in_wifi_only, true);
            return;
        }

        mSubscription.add(NGApi.loadAlbum(album.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<AlbumItem>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.LOG_DEBUG)
                            Log.e(TAG, e.toString());
                        makeSnackBar(R.string.error, true);
                    }

                    @Override
                    public void onNext(AlbumItem albumItem) {
                        if (albumItem == null || albumItem.getPicture() == null || albumItem.getPicture().size() == 0) {
                            if (BuildConfig.LOG_DEBUG)
                                Log.e(TAG, "get albumItem data == null and id = " + album.getId());
                            return;
                        }
                        adapter.setNewData(albumItem.getPicture());
                        realm.beginTransaction();
                        for (Picture p : albumItem.getPicture()) {
                            realm.copyToRealmOrUpdate(new PictureRealm(p));
                        }
                        realm.commitTransaction();
                    }
                }));
    }

    private void startAlbumActivity(View v, int i) {
        Intent intent = new Intent(DetailActivity.this, AlbumActivity.class);
        intent.putParcelableArrayListExtra(AlbumActivity.INTENT_PICTURES, new ArrayList<Picture>(adapter.getList()));
        intent.putExtra(AlbumActivity.INTENT_INDEX, i);

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, v, adapter.get(i).getUrl());
        ActivityCompat.startActivity(this, intent, options.toBundle());

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        supportPostponeEnterTransition();
        reenterState = new Bundle(data.getExtras());
        binding.recycler.scrollToPosition(reenterState.getInt(AlbumActivity.INTENT_INDEX, 0));
        binding.recycler.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                binding.recycler.getViewTreeObserver().removeOnPreDrawListener(this);
                binding.recycler.requestLayout();
                supportStartPostponedEnterTransition();
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
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
