package party.danyang.nationalgeographic.model.album_us;

import android.text.TextUtils;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by dream on 16-8-20.
 */
public class Items extends RealmObject {
    private String title;
    private String url;
    private String publishDate;
    private String pageUrl;
    private String originalUrl;
    private String caption;

    public static List<Items> all(Realm realm) {
        List<Items> items = realm.where(Items.class)
                .findAll();
        if (items.size() > 51) {
            items = items.subList(0, 50);
        }
        return items;
    }

    public static void updateRealm(Realm realm, final List<Items> items) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(items);
            }
        });
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        if (!TextUtils.isEmpty(originalUrl) && originalUrl.startsWith("/"))
            return url + originalUrl;
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
