package party.danyang.nationalgeographic.net;

import android.util.Log;

import party.danyang.nationalgeographic.model.album_us.AlbumList;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by dream on 16-8-12.
 */
public class NGApi_US {
    //    http://www.nationalgeographic.com/photography/photo-of-the-day/_jcr_content/.gallery.2013-07.json
    public static final String BASE_URL = "http://www.nationalgeographic.com/";

    public interface PictureApi {
        @GET("photography/photo-of-the-day/_jcr_content/.gallery.{time}.json")
        Observable<AlbumList> loadPictures(@Path("time") String time);
    }

    public static final Observable<AlbumList> loadPictures(int year, int month) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        String time = year + "-" + (month < 10 ? "0" + month : month);
        return retrofit.create(PictureApi.class).loadPictures(time);
    }
}
