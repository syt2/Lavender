package party.danyang.nationalgeographic.model.album;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yaki on 16-7-7.
 */
public class Picture implements Parcelable {
    private String id;
    private String albumid;
    private String title;
    private String content;
    private String url;
    private String size;
    private String addtime;
    private String author;
    private String thumb;
    private String weburl;
    private String type;
    private String yourshotlink;
    private String copyright;
    private String pmd5;
    private String sort;

    public Picture(Parcel in) {
        readFromParcel(in);
    }

    public Picture() {
    }

    public Picture(PictureRealm pictureRealm) {
        id = pictureRealm.getId();
        albumid = pictureRealm.getAlbumid();
        title = pictureRealm.getTitle();
        content = pictureRealm.getContent();
        url = pictureRealm.getUrl();
        size = pictureRealm.getSize();
        addtime = pictureRealm.getAddtime();
        author = pictureRealm.getAuthor();
        thumb = pictureRealm.getThumb();
        weburl = pictureRealm.getWeburl();
        type = pictureRealm.getType();
        yourshotlink = pictureRealm.getYourshotlink();
        copyright = pictureRealm.getCopyright();
        pmd5 = pictureRealm.getPmd5();
        sort = pictureRealm.getSort();
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

    public void setSize(String size) {
        this.size = size;
    }

    public String getSize() {
        return this.size;
    }

    public void setAddtime(String addtime) {
        this.addtime = addtime;
    }

    public String getAddtime() {
        return this.addtime;
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

    public void setPmd5(String pmd5) {
        this.pmd5 = pmd5;
    }

    public String getPmd5() {
        return this.pmd5;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getSort() {
        return this.sort;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(albumid);
        parcel.writeString(title);
        parcel.writeString(content);
        parcel.writeString(url);
        parcel.writeString(size);
        parcel.writeString(addtime);
        parcel.writeString(author);
        parcel.writeString(thumb);
        parcel.writeString(weburl);
        parcel.writeString(type);
        parcel.writeString(yourshotlink);
        parcel.writeString(copyright);
        parcel.writeString(pmd5);
        parcel.writeString(sort);
    }

    private void readFromParcel(Parcel in) {
        id = in.readString();
        albumid = in.readString();
        title = in.readString();
        content = in.readString();
        url = in.readString();
        size = in.readString();
        addtime = in.readString();
        author = in.readString();
        thumb = in.readString();
        weburl = in.readString();
        type = in.readString();
        yourshotlink = in.readString();
        copyright = in.readString();
        pmd5 = in.readString();
        sort = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Picture> CREATOR = new Creator<Picture>() {
        @Override
        public Picture createFromParcel(Parcel parcel) {
            return new Picture(parcel);
        }

        @Override
        public Picture[] newArray(int i) {
            return new Picture[i];
        }
    };
}
