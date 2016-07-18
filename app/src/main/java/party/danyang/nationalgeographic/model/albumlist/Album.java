package party.danyang.nationalgeographic.model.albumlist;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yaki on 16-7-7.
 */
public class Album implements Parcelable{
    private String id;
    private String title;
    private String url;
    private String addtime;
    private String adshow;
    private String fabu;
    private String amd5;
    private String sort;
    private String ds;
    private String timing;
    private String timingpublish;

    public Album(AlbumRealm albumRealm){
        id=albumRealm.getId();
        title=albumRealm.getTitle();
        url=albumRealm.getUrl();
        addtime=albumRealm.getAddtime();
        adshow=albumRealm.getAdshow();
        fabu=albumRealm.getFabu();
        amd5=albumRealm.getAmd5();
        sort=albumRealm.getSort();
        ds=albumRealm.getDs();
        timing=albumRealm.getTiming();
        timingpublish=albumRealm.getTimingpublish();
    }

    public Album(Parcel in){
        readFromParcel(in);
    }

    public Album(){}

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

    public void setAddtime(String addtime) {
        this.addtime = addtime;
    }

    public String getAddtime() {
        return this.addtime;
    }

    public void setAdshow(String adshow) {
        this.adshow = adshow;
    }

    public String getAdshow() {
        return this.adshow;
    }

    public void setFabu(String fabu) {
        this.fabu = fabu;
    }

    public String getFabu() {
        return this.fabu;
    }

    public void setAmd5(String amd5) {
        this.amd5 = amd5;
    }

    public String getAmd5() {
        return this.amd5;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getSort() {
        return this.sort;
    }

    public void setDs(String ds) {
        this.ds = ds;
    }

    public String getDs() {
        return this.ds;
    }

    public void setTiming(String timing) {
        this.timing = timing;
    }

    public String getTiming() {
        return this.timing;
    }

    public void setTimingpublish(String timingpublish) {
        this.timingpublish = timingpublish;
    }

    public String getTimingpublish() {
        return this.timingpublish;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(url);
        parcel.writeString(addtime);
        parcel.writeString(adshow);
        parcel.writeString(fabu);
        parcel.writeString(amd5);
        parcel.writeString(sort);
        parcel.writeString(ds);
        parcel.writeString(timing);
        parcel.writeString(timingpublish);
    }

    private void readFromParcel(Parcel in) {
        id = in.readString();
        title = in.readString();
        url = in.readString();
        addtime = in.readString();
        adshow = in.readString();
        fabu = in.readString();
        amd5 = in.readString();
        sort = in.readString();
        ds = in.readString();
        timing = in.readString();
        timingpublish = in.readString();
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
