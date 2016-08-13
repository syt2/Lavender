package party.danyang.nationalgeographic.utils;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
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
    public static final String TAG_DETAIL_ACTIVITY = "1717";
    public static final String TAG_ALBUM_ACTIVITY = "171717";

    //给HomeActivity的adapter用
    @BindingAdapter({"image"})
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
    @BindingAdapter({"image"})
    public static void imageLoader(final RadioImageView imageView, String url) {
        if (SettingsModel.getAccelerate(imageView.getContext())) {
            //http://pic01.bdatu.com/Upload/picimg/1464838788.jpg
            //http://ob7lf3frj.bkt.clouddn.com/1464838788.jpg?imageMogr2/thumbnail/600x600
            url = TextUtils.concat("http://ob7lf3frj.bkt.clouddn.com/", url.replace("http://pic01.bdatu.com/Upload/picimg/", ""), "?imageMogr2/thumbnail/600x600").toString();
        }

//        Glide.with(imageView.getContext())
//                .load(url)
//                .asBitmap()
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .dontTransform()
//                .thumbnail(0.1f)
//                .listener(new RequestListener<String, Bitmap>() {
//                    @Override
//                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                        Log.e("resource size", resource.getWidth() + " " + resource.getHeight());
//                        imageView.setOriginalSize(resource.getWidth(), resource.getHeight());
//                        return false;
//                    }
//                })
//                .into(imageView);
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
    @BindingAdapter({"image"})
    public static void imageLoader(final TouchImageView imageView, String url) {
        //TouchImageView需要高质量的图源，但如果用户设置缩略图则用缩略图尺寸
        if (SettingsModel.getAccelerate(imageView.getContext())
                && SettingsModel.getAccelerateInLarge(imageView.getContext())
                && !url.startsWith("http://images.nationalgeographic.com/")) {
            url = TextUtils.concat("http://ob7lf3frj.bkt.clouddn.com/", url.replace("http://pic01.bdatu.com/Upload/picimg/", ""), "?imageMogr2/thumbnail/600x600").toString();
        }
        PicassoHelper.getInstance(imageView.getContext()).load(url)
                .config(Bitmap.Config.ARGB_8888)
                .noFade()
                .priority(Picasso.Priority.HIGH)
                .tag(TAG_ALBUM_ACTIVITY)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        imageView.setZoom(1);
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

}
