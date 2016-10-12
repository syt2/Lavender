package party.danyang.nationalgeographic.model.album;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.Sort;

/**
 * Created by yaki on 16-7-7.
 */
public class Picture extends RealmObject {
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

    public static List<Picture> all(Realm realm, String albumId) {
        return realm.where(Picture.class)
                .equalTo("albumid", albumId)
                .findAllSorted("id", Sort.DESCENDING);
    }

    public static void updateRealm(Realm realm, final List<Picture> pictures) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(pictures);
            }
        });
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
