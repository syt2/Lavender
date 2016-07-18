package party.danyang.nationalgeographic.model.albumlist;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;

/**
 * Created by yaki on 16-7-7.
 */
public class AlbumRealm extends RealmObject {
    @PrimaryKey
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

    public static List<AlbumRealm> all(Realm realm) {
        return realm.where(AlbumRealm.class)
                .findAllSorted("sort", Sort.DESCENDING);
    }

    public AlbumRealm(){}

    public AlbumRealm(Album album){
        id=album.getId();
        title=album.getTitle();
        url=album.getUrl();
        addtime=album.getAddtime();
        adshow=album.getAdshow();
        fabu=album.getFabu();
        amd5=album.getAmd5();
        sort=album.getSort();
        ds=album.getDs();
        timing=album.getTiming();
        timingpublish=album.getTimingpublish();
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
}
