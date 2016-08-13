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
    private String sort;

    public static List<AlbumRealm> all(Realm realm) {
        return realm.where(AlbumRealm.class)
                .findAllSorted("sort", Sort.DESCENDING);
    }

    public AlbumRealm() {
    }

    public AlbumRealm(Album album) {
        id = album.getId();
        title = album.getTitle();
        url = album.getUrl();
        sort = album.getSort();
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
}
