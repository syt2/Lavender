package party.danyang.nationalgeographic.net;

import party.danyang.nationalgeographic.model.Picture_US;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by dream on 16-8-12.
 */
public class NGApi_US {
    public static final String BASE_URL = "https://natgeoapi.herokuapp.com/";

    public interface PictureApi {
        @GET("api/dailyphoto")
        Observable<Picture_US> loadPicture();
    }

    public static final Observable<Picture_US> loadPicture() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(PictureApi.class).loadPicture();
    }
}
