package party.danyang.nationalgeographic.model.albumlist;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yaki on 16-7-7.
 */
public class Album implements Parcelable {
    private String id;
    private String title;
    private String url;
    private String sort;

    public Album(AlbumRealm albumRealm) {
        id = albumRealm.getId();
        title = albumRealm.getTitle();
        url = albumRealm.getUrl();
        sort = albumRealm.getSort();
    }

    public Album(Parcel in) {
        readFromParcel(in);
    }

    public Album() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
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
        parcel.writeString(title);
        parcel.writeString(url);
        parcel.writeString(sort);
    }

    private void readFromParcel(Parcel in) {
        id = in.readString();
        title = in.readString();
        url = in.readString();
        sort = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel parcel) {
            return new Album(parcel);
        }

        @Override
        public Album[] newArray(int i) {
            return new Album[i];
        }
    };
}
