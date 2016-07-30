package party.danyang.nationalgeographic.utils;

import android.content.Context;

import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.utils.singleton.PreferencesHelper;

/**
 * Created by dream on 16-7-27.
 */
public class SettingsModel {
    public static final String PREF_WIFI_ONLY = "pref_wifi_only";

    public static String getCacheSize(Context context) {
        return FileSizeUtil.getAutoFileOrFilesSize(context.getString(R.string.dir_picasso_cache));
    }

    public static void setWifiOnly(Context context, boolean wifiOnly) {
        PreferencesHelper.getInstance(context).edit().putBoolean(PREF_WIFI_ONLY, wifiOnly).commit();
    }

    public static boolean getWifiOnly(Context context) {
        return PreferencesHelper.getInstance(context).getBoolean(PREF_WIFI_ONLY, false);
    }
}
