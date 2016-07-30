package party.danyang.nationalgeographic.utils.singleton;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Mr_Wrong on 16/1/15.
 */
public class PreferencesHelper {
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
