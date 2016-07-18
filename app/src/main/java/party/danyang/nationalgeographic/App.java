package party.danyang.nationalgeographic;

import android.app.Application;

import party.danyang.nationalgeographic.utils.CrashCatcher.CrashCatcher;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashCatcher.ready().toCatch(this);
    }
}
