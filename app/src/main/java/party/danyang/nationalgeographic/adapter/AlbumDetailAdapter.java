package party.danyang.nationalgeographic.adapter;

import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.picasso.Callback;

import java.util.List;

import party.danyang.nationalgeographic.BuildConfig;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.databinding.ItemDetailBinding;
import party.danyang.nationalgeographic.model.album.Picture;
import party.danyang.nationalgeographic.utils.SettingsModel;
import party.danyang.nationalgeographic.utils.singleton.PicassoHelper;

/**
 * Created by yaki on 16-7-7.
 */
public class AlbumDetailAdapter extends BaseAdapter<Picture> {

    public static final String TAG_DETAIL = "tag.detail";

    public AlbumDetailAdapter(List<Picture> data) {
        super(R.layout.item_detail, data);
        setHasStableIds(true);
    }


    @Override
    public long getItemId(int position) {
        if (get(position) != null && !TextUtils.isEmpty(get(position).getUrl())) {
            return get(position).getUrl().hashCode();
        }
        return 0;
    }

    @Override
    public void setBingVariables(ViewDataBinding binding, int position) {
        final ItemDetailBinding bd = (ItemDetailBinding) binding;
        String url = get(position).getUrl();
        if (SettingsModel.getAccelerate(bd.iv.getContext())) {
            if (url.startsWith("http://pic01.bdatu.com/Upload/picimg/")) {
                int length = SettingsModel.getAccelerateImageSize(bd.iv.getContext());
                url = url.replace("http://pic01.bdatu.com/Upload/picimg/", "https://ocgasl9gh.qnssl.com/") + "?imageMogr2/thumbnail/" + length + "x" + length;
            }
        }
        PicassoHelper.getInstance(bd.iv.getContext()).load(url)
                .error(R.mipmap.ic_loading)
                .noFade()
                .placeholder(R.mipmap.ic_loading)
                .tag(TAG_DETAIL)
                .config(Bitmap.Config.RGB_565)
                .into(bd.iv, new Callback() {
                    @Override
                    public void onSuccess() {
                        bd.iv.setOriginalSize(((BitmapDrawable) bd.iv.getDrawable()).getBitmap().getWidth(),
                                ((BitmapDrawable) bd.iv.getDrawable()).getBitmap().getHeight());
                    }

                    @Override
                    public void onError() {
                        if (BuildConfig.LOG_DEBUG)
                            Log.e("Picasso load image", "why...");
                    }
                });
    }

}
