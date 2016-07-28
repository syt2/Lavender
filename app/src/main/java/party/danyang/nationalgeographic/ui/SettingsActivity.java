package party.danyang.nationalgeographic.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.jakewharton.rxbinding.view.RxView;
import com.umeng.analytics.MobclickAgent;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import me.yokeyword.swipebackfragment.SwipeBackActivity;
import party.danyang.nationalgeographic.BuildConfig;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.databinding.ActivitySettingsBinding;
import party.danyang.nationalgeographic.utils.SettingsModel;
import party.danyang.nationalgeographic.utils.Utils;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class SettingsActivity extends SwipeBackActivity {

    private static final int SEND_EMAIL_TO_ME_REQUEST_CODE = 17;

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        initViews();
    }

    private void initViews() {
        binding.setWifiOnly(SettingsModel.getWifiOnly(this));
        binding.setIcon(SettingsModel.getIcon(this));
        binding.setCacheSize(SettingsModel.getCacheSize(this));
        binding.setClicks(this);
        setSupportActionBar(binding.toolbar);
        binding.toolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.md_grey_100));
        binding.toolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.md_grey_100));
    }

    //wifi only
    public void onClickWifiOnly(View view) {
        binding.setWifiOnly(!binding.getWifiOnly());
    }

    public void onCheckChangedWifiOnly(CompoundButton v, boolean checked) {
        SettingsModel.setWifiOnly(v.getContext(), checked);
    }

    //launcher icon
    public void onClickIcon(View view) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_choose_icon, null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        RxView.clicks(v.findViewById(R.id.icon1)).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                dialog.dismiss();
                SettingsModel.setIcon(SettingsActivity.this, 1);
                setIcon(R.mipmap.ic_launcher_1);
            }
        });
        RxView.clicks(v.findViewById(R.id.icon2)).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                dialog.dismiss();
                SettingsModel.setIcon(SettingsActivity.this, 2);
                setIcon(R.mipmap.ic_launcher_2);
            }
        });
    }

    private void setIcon(int iconId) {
        MobclickAgent.onEvent(this, iconId == R.mipmap.ic_launcher_1 ? "icon_1" : "icon_2");
        binding.setIcon(SettingsModel.getIcon(this));
        PackageManager pm = getPackageManager();
        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);

        pm.setComponentEnabledSetting(
                new ComponentName(this, "party.danyang.nationalgeographic.ui.HomeActivity-icon1"),
                iconId == R.mipmap.ic_launcher_1 ?
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(
                new ComponentName(this, "party.danyang.nationalgeographic.ui.HomeActivity-icon2"),
                iconId == R.mipmap.ic_launcher_2 ?
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    //clear cache
    public void onClickClearCache(View view) {
        clearCache();
    }

    private void clearCache() {
        Utils.deleteFileObservable(getString(R.string.dir_picasso_cache))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        binding.setCacheSize(SettingsModel.getCacheSize(SettingsActivity.this));
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.LOG_DEBUG)
                            Log.e("clear cache", e.toString());
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(SettingsActivity.this).build());
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        realm.deleteAll();
                        realm.commitTransaction();
                        realm.close();
                    }
                });
    }

    //report
    public void onClickReport(View view) {
        sendEmailToMe();
    }

    private void sendEmailToMe() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.my_email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_email_title));
        intent.setType("message/rfc822");
        startActivity(intent);
    }

    //about
    public void onClickAbout(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeCustomAnimation(this, R.anim.slide_bottom_in, R.anim.slide_bottom_out);
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    public void onClickCheckUpdate(View view) {
        new AppUpdater(this)
                .setUpdateFrom(UpdateFrom.XML)
                .setUpdateXML("https://raw.githubusercontent.com/dreamcontinue/Lavender/master/app/update-changelog.xml")
                .setDisplay(Display.DIALOG)
                .setDialogTitleWhenUpdateAvailable("")
                .setDialogTitleWhenUpdateNotAvailable("")
                .showAppUpdated(true)
                .start();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
