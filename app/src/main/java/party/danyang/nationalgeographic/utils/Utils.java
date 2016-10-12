package party.danyang.nationalgeographic.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.utils.singleton.PicassoHelper;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by dream on 16-6-9.
 */
public final class Utils {

    public static boolean deleteFile(String filename) {
        return new File(filename).delete();
    }

    public static boolean deleteFiles(String folder) {
        if (TextUtils.isEmpty(folder)) {
            return true;
        }
        File file = new File(folder);
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

    public static Observable<Boolean> deleteFileObservable(final String[] folders) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                for (String folder : folders) {
                    subscriber.onNext(deleteFiles(folder));
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public static void makeSnackBar(View v, String msg, boolean lengthShort) {
        if (v == null) return;
        Snackbar snackbar = Snackbar.make(v, msg, lengthShort ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundResource(R.color.colorPrimary);
        snackbar.show();
    }

    public static void makeSnackBar(View v, int resId, boolean lengthShort) {
        makeSnackBar(v, v.getContext().getString(resId), lengthShort);
    }

    public static void setRefresher(final SwipeRefreshLayout refresher, final boolean isRefresh) {
        if (refresher != null) {
            refresher.post(new Runnable() {
                @Override
                public void run() {
                    refresher.setRefreshing(isRefresh);
                }
            });
        }
    }

    public static int getYearOfNow() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-4"));
        return calendar.get(Calendar.YEAR);
    }

    public static int getMonthOfNow() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-4"));
        return calendar.get(Calendar.MONTH) + 1;
    }

    public static boolean isIntentSafe(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return activities.size() > 0;
    }

    public static void shareItem(final Context context, String url,
                                 final String title, final String describe, final View snackView) {
        final String name = String.valueOf(url.hashCode());
        if (!TextUtils.isEmpty(describe)) {
            url = convertImageUrl(context, url);
        }
        PicassoHelper.getInstance(context)
                .load(url)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(context, bitmap, name));
                        intent.putExtra(Intent.EXTRA_TITLE, title);
                        intent.putExtra(Intent.EXTRA_TEXT, describe);
                        intent.putExtra(Intent.EXTRA_SUBJECT, title);
                        if (isIntentSafe(context, intent)) {
                            context.startActivity(intent);
                        }
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        makeSnackBar(snackView, R.string.error_share, true);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    public static Uri getLocalBitmapUri(Context context, Bitmap bmp, String name) {
        Uri bmpUri = null;
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + name + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    public static String convertImageUrl(Context context, String url) {
        int length = SettingsModel.getAccelerateImageSize(context);
        if (SettingsModel.getAccelerate(context)) {
            if (url.startsWith("http://pic01.bdatu.com/Upload/picimg/")) {
                url = url.replace("http://pic01.bdatu.com/Upload/picimg/", "https://ocgasl9gh.qnssl.com/") + "?imageMogr2/thumbnail/" + length + "x" + length;
            }
        }
        //us接口图太大故直接七牛云转换
        if (url.startsWith("http://yourshot.nationalgeographic.com/")) {
            url = url.replace("http://yourshot.nationalgeographic.com/", "https://ocgawl9z2.qnssl.com/") + "?imageMogr2/thumbnail/" + length + "x" + length;
        } else if (url.startsWith("http://www.nationalgeographic.com/")) {
            url = url.replace("http://www.nationalgeographic.com/", "https://ocwluxhzm.qnssl.com/") + "?imageMogr2/thumbnail/" + length + "x" + length;
        }
        return url;
    }
}
