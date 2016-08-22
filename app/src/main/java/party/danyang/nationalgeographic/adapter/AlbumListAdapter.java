package party.danyang.nationalgeographic.adapter;

import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.text.TextUtils;

import java.util.List;

import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.databinding.ItemAlbumListBinding;
import party.danyang.nationalgeographic.model.albumlist.Album;
import party.danyang.nationalgeographic.utils.singleton.PicassoHelper;

/**
 * Created by yaki on 16-7-7.
 */
public class AlbumListAdapter extends BaseAdapter<Album> {

    public static final String TAG_HOME = "tag.home";

    public AlbumListAdapter(List<Album> data) {
        super(R.layout.item_album_list, data);
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
        ItemAlbumListBinding bd = (ItemAlbumListBinding) binding;
        bd.setAlbum(get(position));
        String url = get(position).getUrl();
        PicassoHelper.getInstance(bd.iv.getContext()).load(url)
                .error(R.mipmap.ic_loading)
                .noFade()
                .placeholder(R.mipmap.ic_loading)
                .tag(TAG_HOME)
                .config(Bitmap.Config.RGB_565)
                .into(bd.iv);
    }
}
