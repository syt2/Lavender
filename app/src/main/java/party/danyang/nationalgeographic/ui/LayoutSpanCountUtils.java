package party.danyang.nationalgeographic.ui;

import android.content.Context;
import android.content.res.Configuration;

import party.danyang.nationalgeographic.R;

/**
 * Created by dream on 16-9-12.
 */
public class LayoutSpanCountUtils {
        public static int getSpanCount(Context context, int configOrientation) {
        if (isTabletDevice(context)) {
            if (configOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                return 4;
            } else if (configOrientation == Configuration.ORIENTATION_PORTRAIT) {
                return 3;
            }
        } else {
            if (configOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                return 3;
            } else if (configOrientation == Configuration.ORIENTATION_PORTRAIT) {
                return 2;
            }
        }
        return 2;
    }

//    public static int getSpanCount(Context context, int configOrientation) {
//        if (configOrientation == Configuration.ORIENTATION_LANDSCAPE)
//            return context.getResources().getInteger(R.integer.layout_span_count_landscape);
//        else
//            return context.getResources().getInteger(R.integer.layout_span_count_portrait);
//    }

    private static boolean isTabletDevice(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
