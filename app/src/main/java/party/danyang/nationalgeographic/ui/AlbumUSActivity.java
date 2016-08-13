package party.danyang.nationalgeographic.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;

import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;
import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.yokeyword.swipebackfragment.SwipeBackActivity;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.databinding.ActivityAlbumUsBinding;
import party.danyang.nationalgeographic.model.Picture_US;
import party.danyang.nationalgeographic.net.NGApi_US;
import party.danyang.nationalgeographic.utils.BindingAdapters;
import party.danyang.nationalgeographic.utils.NetUtils;
import party.danyang.nationalgeographic.utils.SettingsModel;
import party.danyang.nationalgeographic.utils.Utils;
import party.danyang.nationalgeographic.utils.singleton.PicassoHelper;
import party.danyang.nationalgeographic.utils.singleton.PreferencesHelper;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import tr.xip.errorview.ErrorView;

public class AlbumUSActivity extends SwipeBackActivity {
    private static final String TAG = AlbumUSActivity.class.getSimpleName();
    public static final String INTENT_PICTURES = "party.danyang.ng.pictures";
    public static final String INTENT_INDEX = "party.danyang.ng.index";

    private ActivityAlbumUsBinding binding;

    private Picture_US picture;

    public boolean mVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_album_us);
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        PicassoHelper.getInstance(this).resumeTag(BindingAdapters.TAG_ALBUM_ACTIVITY);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        PicassoHelper.getInstance(this).pauseTag(BindingAdapters.TAG_ALBUM_ACTIVITY);
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
        //初始化为可见
        binding.setFullScreen(false);
        binding.setShowErrorView(false);

        RxView.clicks(binding.imgTouch).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                toggle();
            }
        });
        //检查读写权限
        RxView.longClicks(binding.imgTouch)
                .compose(RxPermissions.getInstance(this).ensure(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {//有权限
                            showSaveImgDialog();
                        } else {//无权限
                            makeSnackBar(R.string.permission_denied, true);
                        }
                    }
                });

        binding.errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                getPic();
                binding.setShowErrorView(false);
            }
        });
        binding.refresh.setEnabled(false);
        getPic();
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void showSaveImgDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (binding.imgTouch != null && binding.imgTouch.getDrawable() != null &&
                ((BitmapDrawable) binding.imgTouch.getDrawable()).getBitmap() != null) {
            builder.setMessage(String.format(getString(R.string.save_img_with_resolution)
                    , ((BitmapDrawable) binding.imgTouch.getDrawable()).getBitmap().getWidth()
                    , ((BitmapDrawable) binding.imgTouch.getDrawable()).getBitmap().getHeight()));
        } else {
            builder.setMessage(R.string.save_img);
        }
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                saveImg();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void saveImg() {
        Utils.saveImageAndGetPathObservable(this, picture.getSrc(),
                new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "_" + "US")
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
                        makeSnackBar(e.toString(), true);
                    }

                    @Override
                    public void onNext(Uri uri) {

                    }
                });
    }

    private void setRefresher(final boolean isRefresh) {
        binding.refresh.post(new Runnable() {
            @Override
            public void run() {
                if (binding.refresh != null) {
                    binding.refresh.setRefreshing(isRefresh);
                }
            }
        });
    }

    private void getPic() {
        if (!NetUtils.isConnected(this)) {
            makeSnackBar(R.string.offline, true);
            return;
        }
        if (PreferencesHelper.getInstance(this).getBoolean(SettingsModel.PREF_WIFI_ONLY, false) && !NetUtils.isWiFi(this)) {
            makeSnackBar(R.string.load_not_in_wifi_while_in_wifi_only, true);
            return;
        }
        setRefresher(true);
        NGApi_US.loadPicture()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Picture_US>() {
                    @Override
                    public void onCompleted() {
                        binding.setShowErrorView(false);
                        setRefresher(false);
                        unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        binding.setShowErrorView(true);
                        setRefresher(false);
                        if (e == null) {
                            binding.errorView.setTitle(R.string.lalala);
                            binding.errorView.setSubtitle(R.string.error);
                        } else {
                            binding.errorView.setTitle(R.string.lalala);
                            binding.errorView.setSubtitle(e.getMessage());
                        }
                        unsubscribe();
                    }

                    @Override
                    public void onNext(Picture_US picture_us) {
                        if (TextUtils.isEmpty(picture_us.getSrc())) {
                            onError(new Exception(getString(R.string.exception_content_null)));
                        }
                        picture = picture_us;
                        binding.setPicture(picture_us);
                    }
                });
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
