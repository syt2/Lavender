package party.danyang.nationalgeographic.model.albumlist;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;

/**
 * Created by yaki on 16-7-7.
 */
public class Album extends RealmObject {
    @PrimaryKey
    private String id;
    private String title;
    private String url;
    private String sort;

    public static List<Album> all(Realm realm) {
        return realm.where(Album.class)
                .findAllSorted("sort", Sort.DESCENDING);
    }

    public static void updateRealm(Realm realm, final List<Album> albums) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(albums);
            }
        });
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
