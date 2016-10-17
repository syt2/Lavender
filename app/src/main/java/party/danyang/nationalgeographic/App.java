package party.danyang.nationalgeographic;

import android.app.Application;

import com.umeng.analytics.MobclickAgent;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MobclickAgent.setDebugMode(BuildConfig.DEBUG);
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("LavenderWaitCy")
                .schemaVersion(24)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
