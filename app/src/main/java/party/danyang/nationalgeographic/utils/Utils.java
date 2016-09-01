package party.danyang.nationalgeographic.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.utils.singleton.PicassoHelper;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by yaki on 16-6-9.
 */
public class Utils {

    public static boolean deleteFile(String filename) {
        return new File(filename).delete();
    }

    public static boolean deleteFiles(String folder) {
        if (folder == null || folder.length() == 0 || folder.trim().length() == 0) {
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

    public static Observable<Boolean> deleteFileObservable(final String folder) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                subscriber.onNext(deleteFiles(folder));
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<Uri> saveImgFromUrl(final Context context, final String url, final String name) {
        return Observable.just(url)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<String, Observable<Bitmap>>() {
                    @Override
                    public Observable<Bitmap> call(String s) {
                        return getBitmap(context, s);
                    }
                })
                .flatMap(new Func1<Bitmap, Observable<Uri>>() {
                    @Override
                    public Observable<Uri> call(Bitmap bitmap) {
                        return saveImg(context, name, bitmap);
                    }
                });
    }

    private static Observable<Uri> saveImg(final Context context, final String name, final Bitmap bitmap) {
        return Observable
                .create(new Observable.OnSubscribe<Uri>() {
                    @Override
                    public void call(Subscriber<? super Uri> subscriber) {
                        File dir = new File(Environment.getExternalStorageDirectory(), "Lavender");
                        if (!dir.exists()) {
                            dir.mkdir();
                        }
                        File file = new File(dir, name + ".jpg");
                        try {
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();
                        } catch (FileNotFoundException e) {
                            subscriber.onError(e);
                        } catch (IOException e) {
                            subscriber.onError(e);
                        }
                        Uri uri = Uri.fromFile(file);
                        subscriber.onNext(uri);
                        Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
                        context.sendBroadcast(scannerIntent);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static Observable<Bitmap> getBitmap(final Context context, final String url) {
        return Observable
                .create(new Observable.OnSubscribe<Bitmap>() {
                    @Override
                    public void call(Subscriber<? super Bitmap> subscriber) {
                        Bitmap bitmap = null;
                        try {
                            bitmap = PicassoHelper.getInstance(context)
                                    .load(url)
                                    .get();
                        } catch (IOException e) {
                            subscriber.onError(e);
                        }
                        if (bitmap == null) {
                            subscriber.onError(new Exception(context.getString(R.string.cannot_download_pic)));
                        } else {
                            subscriber.onNext(bitmap);
                        }
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
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
                    if (refresher != null) {
                        refresher.setRefreshing(isRefresh);
                    }
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

}
