package party.danyang.nationalgeographic.model.album;


import java.util.List;

/**
 * Created by yaki on 16-7-7.
 */
public class AlbumItem {
    private String counttotal;
    private List<Picture> picture;

    public void setCounttotal(String counttotal) {
        this.counttotal = counttotal;
    }

    public List<Picture> getPicture() {
        return picture;
    }

    public void setPicture(List<Picture> picture) {
        this.picture = picture;
    }

    public String getCounttotal() {
        return this.counttotal;
    }
}
