package party.danyang.nationalgeographic.net;

import party.danyang.nationalgeographic.model.album.AlbumItem;
import party.danyang.nationalgeographic.model.albumlist.AlbumList;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by yaki on 16-7-7.
 */
public class NGApi {
    public static final String BASE_URL = "http://ng.bdatu.com/";

    public interface AlbumListApi {
        @GET("jiekou/main/p{page}.html")
        Observable<AlbumList> loadAlbumListApi(@Path("page") String page);
    }

    public interface AlbumApi {
        @GET("jiekou/albums/a{id}.html")
        Observable<AlbumItem> loadAlbumApi(@Path("id") String page);
    }

    public static final Observable<AlbumList> loadAlbumList(int page) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(AlbumListApi.class).loadAlbumListApi(String.valueOf(page));
    }

    public static final Observable<AlbumItem> loadAlbum(String id){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(AlbumApi.class).loadAlbumApi(id);
    }
}
