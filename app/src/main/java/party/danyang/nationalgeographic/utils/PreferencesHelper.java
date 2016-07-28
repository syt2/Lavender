package party.danyang.nationalgeographic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import party.danyang.nationalgeographic.BuildConfig;

/**
 * Created by Mr_Wrong on 16/1/15.
 */
public class PreferencesHelper {
    private static final String TAG = PreferencesHelper.class.getSimpleName();
    static volatile SharedPreferences singleton = null;

    public static SharedPreferences getInstance(Context context) {
        if (singleton == null) {
            synchronized (SharedPreferences.class) {
                if (singleton == null) {
                    singleton = PreferenceManager.getDefaultSharedPreferences(context);
                }
            }
        }
        return singleton;
    }
}
