package party.danyang.nationalgeographic;

import android.app.Application;

import com.umeng.analytics.MobclickAgent;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class App extends Application {

    private static RealmConfiguration config;

    @Override
    public void onCreate() {
        super.onCreate();
        MobclickAgent.setDebugMode(BuildConfig.DEBUG);
        config = new RealmConfiguration.Builder(this)
                .name("LavenderWaitCy")
                .schemaVersion(12)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
