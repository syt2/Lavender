package party.danyang.nationalgeographic.net.random;

import java.util.HashMap;
import java.util.Map;

import party.danyang.nationalgeographic.model.random.Random;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by dream on 16-8-22.
 */
public class NGApi_random {
    public static final String BASE_URL = "http://yourshot.nationalgeographic.com/";

    public interface JsonApi {
        //http://yourshot.nationalgeographic.com/oembed?url=http://yourshot.nationalgeographic.com/photos/8839992/&format=json
        @GET("oembed")
        Observable<Random> loadJson(@QueryMap Map<String, String> query);
    }

    public interface ImgApi {
        //用jsoup解析
        @GET("photos/{id}")
        Observable<String> loadImgHtml(@Path("id") String id);
    }

    public static final Observable<Random> loadRandomJson(int id) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        Map<String, String> query = new HashMap<>();
        query.put("url", BASE_URL + "photos/" + id + "/");
        query.put("format", "json");
        return retrofit.create(JsonApi.class).loadJson(query);
    }

    public static final Observable<String> loadRandomHtml(int id) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(ImgApi.class).loadImgHtml(String.valueOf(id));
    }
}
