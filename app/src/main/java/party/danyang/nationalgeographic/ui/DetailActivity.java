package party.danyang.nationalgeographic.ui;

import android.Manifest;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import me.yokeyword.swipebackfragment.SwipeBackActivity;
import party.danyang.nationalgeographic.BuildConfig;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.adapter.AlbumDetailAdapter;
import party.danyang.nationalgeographic.model.album.AlbumItem;
import party.danyang.nationalgeographic.model.album.Picture;
import party.danyang.nationalgeographic.model.album.PictureRealm;
import party.danyang.nationalgeographic.model.albumlist.Album;
import party.danyang.nationalgeographic.net.NGApi;
import party.danyang.nationalgeographic.utils.PicassoHelper;
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

    private Toolbar toolbar;
    private CollapsingToolbarLayout toolbarLayout;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    private Album album;

    private Realm realm;
    private AlbumDetailAdapter adapter;
    private CompositeSubscription mSubscription;
    StaggeredGridLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        if (intent != null) {
            album = intent.getParcelableExtra(INTENT_ALBUM);
        }
        mSubscription = new CompositeSubscription();
        realm = Realm.getInstance(this);
        initViews();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setExitSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    if (reenterState != null) {
                        int position = reenterState.getInt(AlbumActivity.INTENT_INDEX, 0);
                        sharedElements.clear();
                        sharedElements.put(adapter.getItem(position).getUrl(), layoutManager.findViewByPosition(position));
                        reenterState = null;
                    }
                }
            });
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

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        setSupportActionBar(toolbar);
        toolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.md_grey_100));
        toolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.md_grey_100));
        toolbarLayout.setTitle(album.getTitle());

        adapter = new AlbumDetailAdapter(new ArrayList<Picture>());
        adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int i) {
                startAlbumActivity(view, i);
            }
        });
        recyclerView.setAdapter(adapter);
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        RxRecyclerView.scrollStateChanges(recyclerView).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                if (integer == RecyclerView.SCROLL_STATE_IDLE) {
                    PicassoHelper.getInstance(DetailActivity.this).resumeTag("1");
                } else {
                    PicassoHelper.getInstance(DetailActivity.this).pauseTag("1");
                }
            }
        });
        load();
        RxView.clicks(fab)//点击fab
                .compose(RxPermissions.getInstance(this).ensure(Manifest.permission.WRITE_EXTERNAL_STORAGE))//检查权限
                .subscribe(new Action1<Boolean>() {
                               @Override
                               public void call(Boolean aBoolean) {
                                   if (aBoolean) {//拥有该权限
                                       saveAllImg();
                                   } else {//拒绝该权限
                                       if (recyclerView != null) {
                                           Snackbar.make(recyclerView, R.string.permission_denided, Snackbar.LENGTH_SHORT).show();
                                       }
                                   }
                               }
                           }
                );
    }

    private void saveAllImg() {
        for (int i = 0; i < adapter.getData().size(); i++) {
            mSubscription.add(Utils.saveImageAndGetPathObservable(DetailActivity.this, ((Picture) adapter.getData().get(i)).getUrl(),
                    ((Picture) adapter.getData().get(i)).getAlbumid() + "_" + i)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Uri>() {
                        @Override
                        public void onCompleted() {
                            File appDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
                            String msg = String.format(getString(R.string.save_in_file), appDir.getAbsolutePath());
                            if (recyclerView != null) {
                                Snackbar.make(recyclerView, msg, Snackbar.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (recyclerView != null) {
                                Snackbar.make(recyclerView, e.toString(), Snackbar.LENGTH_SHORT).show();
                            }
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
                        if (recyclerView != null) {
                            Snackbar.make(recyclerView, getString(R.string.error) + ">.<", Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNext(AlbumItem albumItem) {
                        if (albumItem == null || albumItem.getPicture() == null || albumItem.getPicture().size() == 0) {
                            if (BuildConfig.LOG_DEBUG)
                                Log.e(TAG, "get albumItem data == null and id =" + album.getId());
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
        intent.putParcelableArrayListExtra(AlbumActivity.INTENT_PICTURES, new ArrayList<Picture>(adapter.getData()));
        intent.putExtra(AlbumActivity.INTENT_INDEX, i);

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, v, adapter.getItem(i).getUrl());
        ActivityCompat.startActivity(this, intent, options.toBundle());

    }

    private Bundle reenterState;

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        supportPostponeEnterTransition();
        reenterState = new Bundle(data.getExtras());
        recyclerView.scrollToPosition(reenterState.getInt(AlbumActivity.INTENT_INDEX, 0));
        recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                recyclerView.requestLayout();
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
