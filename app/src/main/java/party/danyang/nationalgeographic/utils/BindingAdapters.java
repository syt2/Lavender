package party.danyang.nationalgeographic.utils;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import party.danyang.nationalgeographic.BuildConfig;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.utils.singleton.PicassoHelper;
import party.danyang.nationalgeographic.widget.RadioImageView;
import party.danyang.nationalgeographic.widget.TouchImageView;

/**
 * Created by dream on 16-7-27.
 */
public class BindingAdapters {

    public static final String TAG_HOME_ACTIVITY = "17";
    public static final String TAG_DETAIL_ACTIVITY = "177";
    public static final String TAG_ALBUM_ACTIVITY = "1777";

    //给HomeActivity的adapter用
    @BindingAdapter({"bind:image"})
    public static void imageLoader(ImageView imageView, String url) {
        PicassoHelper.getInstance(imageView.getContext()).load(url)
                .error(R.mipmap.ic_loading)
                .noFade()
                .placeholder(R.mipmap.ic_loading)
                .tag(TAG_HOME_ACTIVITY)
                .config(Bitmap.Config.RGB_565)
                .into(imageView);
    }

    //给DetailActivity的adapter用
    @BindingAdapter({"bind:image"})
    public static void imageLoader(final RadioImageView imageView, String url) {
        PicassoHelper.getInstance(imageView.getContext()).load(url)
                .error(R.mipmap.ic_loading)
                .noFade()
                .placeholder(R.mipmap.ic_loading)
                .tag(TAG_DETAIL_ACTIVITY)
                .config(Bitmap.Config.RGB_565)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        imageView.setOriginalSize(((BitmapDrawable) imageView.getDrawable()).getBitmap().getWidth(),
                                ((BitmapDrawable) imageView.getDrawable()).getBitmap().getHeight());
                    }

                    @Override
                    public void onError() {
                        if (BuildConfig.LOG_DEBUG)
                            Log.e("Picasso load image", "why...");
                    }
                });
    }

    //给AlbumFragment的adapter用
    @BindingAdapter({"bind:image"})
    public static void imageLoader(TouchImageView imageView, String url) {
        //TouchImageView需要高质量的图源
        PicassoHelper.getInstance(imageView.getContext()).load(url)
                .config(Bitmap.Config.ARGB_8888)
                .priority(Picasso.Priority.HIGH)
                .tag(TAG_DETAIL_ACTIVITY)
                .into(imageView);
    }

}
