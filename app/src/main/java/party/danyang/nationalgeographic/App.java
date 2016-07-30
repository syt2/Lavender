package party.danyang.nationalgeographic;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import com.umeng.analytics.MobclickAgent;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MobclickAgent.setDebugMode(BuildConfig.LOG_DEBUG);
    }
}
