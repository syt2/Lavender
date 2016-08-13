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
    private String author;
    private String thumb;
    private String weburl;
    private String type;
    private String yourshotlink;
    private String copyright;

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
        author = pictureRealm.getAuthor();
        thumb = pictureRealm.getThumb();
        weburl = pictureRealm.getWeburl();
        type = pictureRealm.getType();
        yourshotlink = pictureRealm.getYourshotlink();
        copyright = pictureRealm.getCopyright();
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

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(albumid);
        parcel.writeString(title);
        parcel.writeString(content);
        parcel.writeString(url);
        parcel.writeString(author);
        parcel.writeString(thumb);
        parcel.writeString(weburl);
        parcel.writeString(type);
        parcel.writeString(yourshotlink);
        parcel.writeString(copyright);
    }

    private void readFromParcel(Parcel in) {
        id = in.readString();
        albumid = in.readString();
        title = in.readString();
        content = in.readString();
        url = in.readString();
        author = in.readString();
        thumb = in.readString();
        weburl = in.readString();
        type = in.readString();
        yourshotlink = in.readString();
        copyright = in.readString();
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
