package party.danyang.nationalgeographic.model.album_us;

/**
 * Created by dream on 16-8-20.
 */
public class Items {
    private String title;
    private String url;
    private String publishDate;
    private String pageUrl;
    private String caption;

    public Items() {
    }

    public Items(ItemsRealm items) {
        this.title = items.getTitle();
        this.url = items.getUrl();
        this.publishDate = items.getPublishDate();
        this.pageUrl = items.getPageUrl();
        this.caption = items.getCaption();
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
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
