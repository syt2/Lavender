package party.danyang.nationalgeographic.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.umeng.analytics.MobclickAgent;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import party.danyang.nationalgeographic.BuildConfig;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.databinding.ActivitySettingsBinding;
import party.danyang.nationalgeographic.databinding.LayoutDialogInputBinding;
import party.danyang.nationalgeographic.ui.base.ToolbarActivity;
import party.danyang.nationalgeographic.utils.SettingsModel;
import party.danyang.nationalgeographic.utils.Utils;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class SettingsActivity extends ToolbarActivity {

    private static final String PREF_FIRST_CHANGE_ACCELERATE = "pref_first_change_accelerate";

    private ActivitySettingsBinding binding;

    private AppUpdater appUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        initViews();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (appUpdater != null) {
            appUpdater.stop();
        }
    }

    private void initViews() {
        binding.setWifiOnly(SettingsModel.getWifiOnly(this));
        binding.setCacheSize(SettingsModel.getCacheSize(this));
        binding.setAccelerate(SettingsModel.getAccelerate(this));
        binding.setCustomImageSize(SettingsModel.getAccelerateImageSize(this));
        binding.setClicks(this);
        setupToolbar(binding.toolbarContent);
    }

    //wifi only
    public void onClickWifiOnly(View view) {
        binding.setWifiOnly(!binding.getWifiOnly());
    }

    public void onCheckChangedWifiOnly(CompoundButton v, boolean checked) {
        SettingsModel.setWifiOnly(this, checked);
    }

    //七牛云加速
    public void onClickAccelerate(View view) {
        binding.setAccelerate(!binding.getAccelerate());
    }

    public void onCheckChangedAccelerate(CompoundButton v, boolean checked) {
        binding.setAccelerate(checked);
        if (!checked) {
            //恢复默认值
            SettingsModel.setAccelerateImageSize(this, 1000);
            binding.setCustomImageSize(1000);
        }
        SettingsModel.setAccelerate(this, checked);
    }

    public void onClickAccelerateCustomImageSize(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutDialogInputBinding inputBinding = DataBindingUtil
                .inflate(getLayoutInflater(), R.layout.layout_dialog_input, null, false);
        inputBinding.input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() <= 0) {
                    inputBinding.inputLayout.setErrorEnabled(false);
                    return;
                }
                int value = Integer.valueOf(charSequence.toString());
                if (value > 2000) {
                    if (value > 4000)
                        inputBinding.inputLayout.setError("太大啦，系统已自动调回默认值1000啦");
                    else
                        inputBinding.inputLayout.setError("亲确定要设成" + charSequence + "嘛? 在查看某些大图的时候可能会有卡顿哦");
                } else if (Integer.valueOf(charSequence.toString()) < 800) {
                    if (value < 500)
                        inputBinding.inputLayout.setError("太小啦，系统已自动调回默认值1000啦");
                    else
                        inputBinding.inputLayout.setError("亲确定要设成" + charSequence + "嘛? 在看图时会很模糊的哦");
                } else {
                    inputBinding.inputLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        builder.setView(inputBinding.getRoot());
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (inputBinding.input.getText().toString().length() <= 0) {
                    return;
                }
                int value = Integer.valueOf(inputBinding.input.getText().toString());
                if (value > 4000 || value < 500) {
                    value = 1000;
                }
                SettingsModel.setAccelerateImageSize(SettingsActivity.this, value);
                binding.setCustomImageSize(value);
                dialogInterface.dismiss();
            }
        });
        builder.show();
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

    public void onClickCheckUpdate(View view) {
        appUpdater = new AppUpdater(this)
                .setUpdateFrom(UpdateFrom.XML)
                .setUpdateXML("https://raw.githubusercontent.com/dreamcontinue/Lavender/master/app/update-changelog.xml")
                .setDisplay(Display.DIALOG)
                .setDialogTitleWhenUpdateAvailable("")
                .setDialogTitleWhenUpdateNotAvailable("")
                .showAppUpdated(true);
        appUpdater.start();
    }

}
