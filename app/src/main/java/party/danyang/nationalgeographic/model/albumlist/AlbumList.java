package party.danyang.nationalgeographic.model.albumlist;

import java.util.List;

/**
 * Created by yaki on 16-7-7.
 */
public class AlbumList {
    private List<Album> album;

    public void setAlbum(List<Album> album) {
        this.album = album;
    }

    public List<Album> getAlbum() {
        return this.album;
    }
}
