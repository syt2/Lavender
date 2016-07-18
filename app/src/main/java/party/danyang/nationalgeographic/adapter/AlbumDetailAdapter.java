package party.danyang.nationalgeographic.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.model.album.Picture;
import party.danyang.nationalgeographic.utils.PicassoHelper;
import party.danyang.nationalgeographic.widget.RadioImageView;

/**
 * Created by yaki on 16-7-7.
 */
public class AlbumDetailAdapter extends BaseQuickAdapter<Picture> {
    public AlbumDetailAdapter(List<Picture> data) {
        super(R.layout.item_detail, data);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, final Picture picture) {
        final RadioImageView imageView = baseViewHolder.getView(R.id.iv_album_detail);
        PicassoHelper.getInstance(mContext).load(picture.getUrl())
                .error(R.mipmap.ic_loading)
                .noFade()
                .placeholder(R.mipmap.ic_loading)
                .tag("1")
                .config(Bitmap.Config.RGB_565)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        imageView.setOriginalSize(((BitmapDrawable) imageView.getDrawable()).getBitmap().getWidth(),
                                ((BitmapDrawable) imageView.getDrawable()).getBitmap().getHeight());
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

}
