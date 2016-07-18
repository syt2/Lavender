package party.danyang.nationalgeographic.adapter;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.model.albumlist.Album;
import party.danyang.nationalgeographic.utils.PicassoHelper;

/**
 * Created by yaki on 16-7-7.
 */
public class AlbumListAdapter extends BaseQuickAdapter<Album> {
    public AlbumListAdapter(List<Album> data) {
        super(R.layout.item_album_list, data);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, Album album) {
        baseViewHolder.setText(R.id.tv_album_list, album.getTitle());
        PicassoHelper.getInstance(mContext).load(album.getUrl())
                .error(R.mipmap.ic_loading)
                .noFade()
                .placeholder(R.mipmap.ic_loading)
                .tag("1")
                .config(Bitmap.Config.RGB_565)
                .into((ImageView) (baseViewHolder.getView(R.id.iv_album_list)));
    }
}
