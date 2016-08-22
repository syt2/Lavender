package party.danyang.nationalgeographic.net.random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by dream on 16-8-22.
 */
public class RamdomImgParser {
    public static String parserImg(String html) {
        Document document = Jsoup.parse(html);
        Elements divs = document.select("div");
        for (Element div : divs) {
            if (!div.attr("id").equals("photo-detail-wrapper")) {
                continue;
            }
            return div.select("img").first().attr("src");
        }
        return null;
    }
}
