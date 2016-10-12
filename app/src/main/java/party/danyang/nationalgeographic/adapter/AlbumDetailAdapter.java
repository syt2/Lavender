package party.danyang.nationalgeographic.adapter;

import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.squareup.picasso.Callback;

import java.util.List;

import party.danyang.nationalgeographic.BuildConfig;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.adapter.base.BaseAdapter;
import party.danyang.nationalgeographic.databinding.ItemDetailBinding;
import party.danyang.nationalgeographic.model.album.Picture;
import party.danyang.nationalgeographic.utils.Utils;
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
        String url = Utils.convertImageUrl(bd.iv.getContext(), get(position).getUrl());
        PicassoHelper.getInstance(bd.iv.getContext()).load(url)
                .error(R.mipmap.nat_geo_480)
                .noFade()
                .placeholder(R.mipmap.nat_geo_480)
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
                        bd.iv.setVisibility(View.GONE);
                    }
                });
    }

}
