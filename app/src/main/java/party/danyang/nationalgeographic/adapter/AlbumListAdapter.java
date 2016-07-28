package party.danyang.nationalgeographic.adapter;

import android.databinding.ViewDataBinding;
import android.text.TextUtils;

import java.util.List;

import party.danyang.nationalgeographic .BR;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.model.albumlist.Album;

/**
 * Created by yaki on 16-7-7.
 */
public class AlbumListAdapter extends BaseAdapter<Album> {

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
        binding.setVariable(BR.album, get(position));
    }
}
