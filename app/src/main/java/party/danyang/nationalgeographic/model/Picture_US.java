package party.danyang.nationalgeographic.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by dream on 16-8-12.
 */
public class Picture_US implements Parcelable {

    private String src;

    private String alt;

    private String credit;

    public Picture_US(Parcel in) {
        readFromParcel(in);
    }

    public Picture_US() {
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getSrc() {
        return TextUtils.concat("http:", src).toString();
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getAlt() {
        return this.alt;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getCredit() {
        return this.credit;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(src);
        parcel.writeString(alt);
        parcel.writeString(credit);
    }

    private void readFromParcel(Parcel in) {
        src = in.readString();
        alt = in.readString();
        credit = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Picture_US> CREATOR = new Creator<Picture_US>() {
        @Override
        public Picture_US createFromParcel(Parcel parcel) {
            return new Picture_US(parcel);
        }

        @Override
        public Picture_US[] newArray(int i) {
            return new Picture_US[i];
        }
    };
}
