package party.danyang.nationalgeographic.utils;

import android.content.Context;
import android.os.Environment;

import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.utils.singleton.PreferencesHelper;

/**
 * Created by dream on 16-7-27.
 */
public class SettingsModel {
    public static final String PREF_WIFI_ONLY = "pref_wifi_only";
    public static final String PREF_USE_ACCELERATE = "pref_use_accelerate";
    public static final String PREF_ACCELERATE_IMAGE_SIZE = "pref_image_size";
    public static final String PREF_DOUBLE_CLICK_EXIT = "pref_double_click_exit";

    public static String getCacheSize(Context context) {
        String sharePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) != null ?
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() : null;
        return FileSizeUtil.getAutoMultiFileOrFilesSize(new String[]{
                context.getString(R.string.dir_picasso_cache),
                sharePath});
    }

    public static void setWifiOnly(Context context, boolean wifiOnly) {
        PreferencesHelper.getInstance(context).edit().putBoolean(PREF_WIFI_ONLY, wifiOnly).commit();
    }

    public static boolean getWifiOnly(Context context) {
        return PreferencesHelper.getInstance(context).getBoolean(PREF_WIFI_ONLY, false);
    }

    public static void setDoubleClickExit(Context context, boolean b) {
        PreferencesHelper.getInstance(context).edit().putBoolean(PREF_DOUBLE_CLICK_EXIT, b).commit();
    }

    public static boolean getDoubleClickExit(Context context) {
        return PreferencesHelper.getInstance(context).getBoolean(PREF_DOUBLE_CLICK_EXIT, false);
    }

    public static void setAccelerate(Context context, boolean accelerate) {
        PreferencesHelper.getInstance(context).edit().putBoolean(PREF_USE_ACCELERATE, accelerate).commit();
    }

    public static boolean getAccelerate(Context context) {
        return PreferencesHelper.getInstance(context).getBoolean(PREF_USE_ACCELERATE, false);
    }

    public static void setAccelerateImageSize(Context context, int size) {
        PreferencesHelper.getInstance(context).edit().putInt(PREF_ACCELERATE_IMAGE_SIZE, size).commit();
    }

    public static int getAccelerateImageSize(Context context) {
        return PreferencesHelper.getInstance(context).getInt(PREF_ACCELERATE_IMAGE_SIZE, 1000);
    }
}
