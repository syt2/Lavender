package party.danyang.nationalgeographic.model.album;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;

/**
 * Created by yaki on 16-7-7.
 */
public class PictureRealm extends RealmObject {
    @PrimaryKey
    private String id;
    private String albumid;
    private String title;
    private String content;
    private String url;
    private String author;
    private String thumb;
    private String weburl;
    private String type;
    private String yourshotlink;
    private String copyright;

    public static List<PictureRealm> all(Realm realm, String albumId) {
        return realm.where(PictureRealm.class)
                .equalTo("albumid", albumId)
                .findAllSorted("id", Sort.DESCENDING);
    }

    public PictureRealm(Picture pictureRealm) {
        id = pictureRealm.getId();
        albumid = pictureRealm.getAlbumid();
        title = pictureRealm.getTitle();
        content = pictureRealm.getContent();
        url = pictureRealm.getUrl();
        author = pictureRealm.getAuthor();
        thumb = pictureRealm.getThumb();
        weburl = pictureRealm.getWeburl();
        type = pictureRealm.getType();
        yourshotlink = pictureRealm.getYourshotlink();
        copyright = pictureRealm.getCopyright();
    }

    public PictureRealm() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setAlbumid(String albumid) {
        this.albumid = albumid;
    }

    public String getAlbumid() {
        return this.albumid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getThumb() {
        return this.thumb;
    }

    public void setWeburl(String weburl) {
        this.weburl = weburl;
    }

    public String getWeburl() {
        return this.weburl;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public void setYourshotlink(String yourshotlink) {
        this.yourshotlink = yourshotlink;
    }

    public String getYourshotlink() {
        return this.yourshotlink;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getCopyright() {
        return this.copyright;
    }
}
