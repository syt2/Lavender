package party.danyang.nationalgeographic.model.albumlist;

import java.util.List;

/**
 * Created by yaki on 16-7-7.
 */
public class AlbumList {
    private String total;

    private String page;

    private String pagecount;

    private List<Album> album;

    public void setTotal(String total) {
        this.total = total;
    }

    public String getTotal() {
        return this.total;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getPage() {
        return this.page;
    }

    public void setPagecount(String pagecount) {
        this.pagecount = pagecount;
    }

    public String getPagecount() {
        return this.pagecount;
    }

    public void setAlbum(List<Album> album) {
        this.album = album;
    }

    public List<Album> getAlbum() {
        return this.album;
    }
}
