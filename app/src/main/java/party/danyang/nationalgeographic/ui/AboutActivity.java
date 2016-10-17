package party.danyang.nationalgeographic.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;
import com.umeng.analytics.MobclickAgent;

import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.databinding.ActivityAboutBinding;
import party.danyang.nationalgeographic.ui.base.ToolbarActivity;
import party.danyang.nationalgeographic.utils.Utils;
import party.danyang.nationalgeographic.widget.OnStateChangedListener;
import rx.functions.Action1;

public class AboutActivity extends ToolbarActivity {

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about);
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

    private void initViews() {
        binding.setClicks(this);
        setSupportActionBar(binding.toolbar);
        setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        RxToolbar.navigationClicks(binding.toolbar).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                supportFinishAfterTransition();
            }
        });
        binding.toolbarLayout.setOnStateChangedListener(new OnStateChangedListener() {
            @Override
            public void onExpanded() {
                setTitle(null);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                binding.toolbarLayout.setTitle(null);
            }

            @Override
            public void onCollapsed() {
                setTitle(getString(R.string.settings_about));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            @Override
            public void onInternediate() {
                setTitle(null);
                binding.toolbarLayout.setTitle(null);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        });
    }

    public void onClickUseAttention(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setMessage(R.string.about_attention_content);
        builder.setPositiveButton(R.string.ok, null);
        builder.show();
    }

    public void onClickRating(View view) {
        goToMarket();
    }

    private void goToMarket() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
        startIntent(intent);
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
        startIntent(intent);
    }

    private long lastClickTime;
    private int clickTime;

    public void onClickIcon(View view) {
        if (System.currentTimeMillis() - lastClickTime < 800) {
            if (System.currentTimeMillis() - lastClickTime < 400) {
                return;
            }
            clickTime++;
            lastClickTime = System.currentTimeMillis();
            if (clickTime == 5) {
                Utils.makeSnackBar(binding.getRoot(), "妳说最爱薰衣草", false);
            } else if (clickTime == 17) {
                Utils.makeSnackBar(binding.getRoot(), "薰衣草永远等待着所爱之人，等待着爱情", false);
            } else if (clickTime == 34) {
                Utils.makeSnackBar(binding.getRoot(), "和妳一起的时光如此美妙而短暂", false);
            } else if (clickTime == 71) {
                Utils.makeSnackBar(binding.getRoot(), "我愿倾一生守护这段回忆", false);
            } else if (clickTime == 100) {
                Utils.makeSnackBar(binding.getRoot(), "愿做一株为妳开放的薰衣草", false);
            }
        } else {
            clickTime = 0;
            lastClickTime = System.currentTimeMillis();
        }
    }

    private void startIntent(Intent intent) {
        if (Utils.isIntentSafe(this, intent)) {
            startActivity(intent);
        } else {
            Utils.makeSnackBar(binding.getRoot(), R.string.settings_no_activity_handle, true);
        }
    }
}
