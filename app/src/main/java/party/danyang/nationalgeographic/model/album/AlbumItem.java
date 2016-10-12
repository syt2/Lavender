package party.danyang.nationalgeographic.model.album;


import java.util.List;

/**
 * Created by yaki on 16-7-7.
 */
public class AlbumItem {
    private List<Picture> picture;

    public List<Picture> getPicture() {
        return picture;
    }

    public void setPicture(List<Picture> picture) {
        this.picture = picture;
    }
}
