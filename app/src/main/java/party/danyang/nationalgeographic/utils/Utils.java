package party.danyang.nationalgeographic.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import party.danyang.nationalgeographic.R;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by dream on 16-6-9.
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

    public static boolean isIntentSafe(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return activities.size() > 0;
    }
}
