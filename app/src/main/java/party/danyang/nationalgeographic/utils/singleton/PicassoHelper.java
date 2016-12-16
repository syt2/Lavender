package party.danyang.nationalgeographic.utils.singleton;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import party.danyang.nationalgeographic.BuildConfig;

/**
 * Created by Mr_Wrong on 16/1/15.
 */
public class PicassoHelper {
    private static final String TAG = PicassoHelper.class.getSimpleName();
    static volatile Picasso singleton = null;
    private static final int MAX_DISK_CACHE_SIZE = 96 * 1024 * 1024;

    public static Picasso getInstance(Context context) {
        if (singleton == null) {
            synchronized (Picasso.class) {
                if (singleton == null) {
                    singleton = new Picasso.Builder(context)
                            .downloader(new OkHttp3Downloader(context, MAX_DISK_CACHE_SIZE))
                            .listener(new Picasso.Listener() {
                                @Override
                                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                                        Log.e(TAG, "Failed to load image: " + uri.toString(), exception);
                                }
                            })
                            .build();
                }
            }
        }
        return singleton;
    }
}
