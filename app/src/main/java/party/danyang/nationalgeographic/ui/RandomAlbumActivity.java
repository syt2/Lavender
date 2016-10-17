package party.danyang.nationalgeographic.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;
import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.umeng.analytics.MobclickAgent;

import me.yokeyword.swipebackfragment.SwipeBackActivity;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.databinding.ActivityRandomAlbumBinding;
import party.danyang.nationalgeographic.model.random.Random;
import party.danyang.nationalgeographic.net.random.NGApi_random;
import party.danyang.nationalgeographic.net.random.RamdomImgParser;
import party.danyang.nationalgeographic.utils.DownloadMangerResolver;
import party.danyang.nationalgeographic.utils.NetUtils;
import party.danyang.nationalgeographic.utils.SaveImage;
import party.danyang.nationalgeographic.utils.SettingsModel;
import party.danyang.nationalgeographic.utils.Utils;
import party.danyang.nationalgeographic.utils.singleton.PicassoHelper;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import tr.xip.errorview.ErrorView;

/**
 * Created by dream on 16-8-22.
 */
public class RandomAlbumActivity extends SwipeBackActivity {
    public static final String TAG_RANDOM = "tag.random";
    private static final String TAG = "RandomAlbumActivity";
    private static final String SAVED_INSTANCE_STATE_RANDOM_ID = "savedInstanceState.random_id";

    private int randomId;
    private ActivityRandomAlbumBinding binding;
    private CompositeSubscription mSubscription;
    private String url;
    private Random random;

    public boolean mVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_random_album);

        mSubscription = new CompositeSubscription();
        randomId = savedInstanceState != null
                ? savedInstanceState.getInt(SAVED_INSTANCE_STATE_RANDOM_ID, getRandomInt())
                : getRandomInt();
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        PicassoHelper.getInstance(this).resumeTag(TAG_RANDOM);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        PicassoHelper.getInstance(this).pauseTag(TAG_RANDOM);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
        PicassoHelper.getInstance(this).cancelTag(TAG_RANDOM);
    }

    private void initViews() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_grey_300_24dp);
        setSupportActionBar(binding.toolbar);
        setTitle(null);
        RxToolbar.navigationClicks(binding.toolbar).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                supportFinishAfterTransition();
            }
        });
        RxToolbar.itemClicks(binding.toolbar).subscribe(new Action1<MenuItem>() {
            @Override
            public void call(MenuItem menuItem) {
                onToolbarMenuItemClicked(menuItem);
            }
        });

        binding.setFullScreen(false);
        binding.setShowErrorView(false);

        RxView.clicks(binding.imgTouch).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                toggle();
            }
        });

        RxView.longClicks(binding.imgTouch)
                .compose(RxPermissions.getInstance(this).ensure(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {//有权限
                            showSaveImgDialog();
                        } else {//无权限
                            Utils.makeSnackBar(binding.getRoot(), R.string.permission_denied, true);
                        }
                    }
                });

        binding.errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                binding.setShowErrorView(false);
                getPic(getRandomInt());
            }
        });
        binding.refresh.setProgressViewOffset(true, 100, 200);
        binding.refresh.setColorSchemeResources(R.color.md_grey_600, R.color.md_grey_800);
        RxSwipeRefreshLayout.refreshes(binding.refresh)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        getPic(getRandomInt());
                    }
                });
        getPic(randomId);
    }

    private int getRandomInt() {
        return new java.util.Random().nextInt(10000000);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_INSTANCE_STATE_RANDOM_ID, randomId);
    }

    private void getPic(final int randomId) {
        if (!NetUtils.isConnected(this)) {
            Utils.makeSnackBar(binding.getRoot(), R.string.offline, true);
            return;
        }
        if (SettingsModel.getWifiOnly(this) && !NetUtils.isWiFi(this)) {
            Utils.makeSnackBar(binding.getRoot(), R.string.load_not_in_wifi_while_in_wifi_only, true);
            return;
        }
        Utils.setRefresher(binding.refresh, true);

        NGApi_random.loadRandomJson(randomId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Random>() {
                    @Override
                    public void onCompleted() {
                        RandomAlbumActivity.this.randomId = randomId;
                        loadImg(randomId);
                        unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e != null && !TextUtils.isEmpty(e.getMessage()) && e.getMessage().trim().equals(getString(R.string.notfound404))) {
                            getPic(getRandomInt());
                        } else {
                            binding.setShowErrorView(true);
                            Utils.setRefresher(binding.refresh, false);
                            if (e == null || TextUtils.isEmpty(e.getMessage())) {
                                binding.errorView.setTitle(R.string.lalala);
                                binding.errorView.setSubtitle(R.string.error);
                            } else {
                                binding.errorView.setTitle(R.string.lalala);
                                binding.errorView.setSubtitle(e.getMessage());
                            }
                        }
                        unsubscribe();
                    }

                    @Override
                    public void onNext(Random random) {
                        if (random == null) {
                            onError(new Exception(getString(R.string.notfound404)));
                        }
                        RandomAlbumActivity.this.random = random;
                        setTitle(random.getTitle());
                    }
                });
    }

    private void loadImg(int randomId) {
        NGApi_random.loadRandomHtml(randomId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        binding.setShowErrorView(false);
//                        refresher false 在图片加载完后再设false
                        unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        binding.setShowErrorView(true);
                        Utils.setRefresher(binding.refresh, false);
                        if (e == null || TextUtils.isEmpty(e.getMessage())) {
                            binding.errorView.setTitle(R.string.lalala);
                            binding.errorView.setSubtitle(R.string.error);
                        } else {
                            binding.errorView.setTitle(R.string.lalala);
                            binding.errorView.setSubtitle(e.getMessage());
                        }
                        unsubscribe();
                    }

                    @Override
                    public void onNext(String s) {
                        if (TextUtils.isEmpty(s)) {
                            onError(new Exception(getString(R.string.exception_content_null)));
                        }
                        url = RamdomImgParser.parserImg(s);
                        if (TextUtils.isEmpty(url)) {
                            onError(new Exception(getString(R.string.exception_content_null)));
                        }
                        if (url.startsWith("/")) {
                            url = "http://yourshot.nationalgeographic.com" + url;
                        }
                        PicassoHelper.getInstance(RandomAlbumActivity.this)
                                .load(url)
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .into(binding.imgTouch, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        Utils.setRefresher(binding.refresh, false);
                                        binding.imgTouch.setZoom(1);
                                    }

                                    @Override
                                    public void onError() {
                                        Utils.setRefresher(binding.refresh, false);
                                    }
                                });
                    }
                });
    }

    private void showSaveImgDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setMessage(R.string.save_img);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                saveImg();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void saveImg() {
        if (DownloadMangerResolver.resolve(this))
            SaveImage.saveImg(this, binding.getRoot(), String.valueOf(randomId) + ".jpg", url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_random, menu);
        return true;
    }

    private void onToolbarMenuItemClicked(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_yourshotlink) {
            if (random == null || TextUtils.isEmpty(random.getWeb_page())) return;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(random.getWeb_page()));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Utils.makeSnackBar(binding.getRoot(), R.string.not_legal_yourshotlink, true);
            }
        } else if (id == R.id.action_refresh) {
            getPic(getRandomInt());
        } else if (id == R.id.action_share) {
            if (random == null || TextUtils.isEmpty(random.getTitle()) || TextUtils.isEmpty(url))
                return;
            Utils.shareItem(this, url, random.getTitle(), null, binding.getRoot());
        }
    }

    //hide and show
    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            binding.imgTouch.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            binding.setFullScreen(false);
        }
    };

    public void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        binding.setFullScreen(true);
        mVisible = false;

        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    public void show() {
        binding.imgTouch.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }
}
