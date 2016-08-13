package party.danyang.nationalgeographic.ui;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;
import com.umeng.analytics.MobclickAgent;

import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.databinding.ActivityAboutBinding;
import party.danyang.nationalgeographic.ui.base.ToolbarActivity;
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
        binding.appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (verticalOffset == 0) {
                    if (state != CollapsingToolbarLayoutState.EXPANDED) {
                        state = CollapsingToolbarLayoutState.EXPANDED;//修改状态标记为展开
                        setTitle(null);
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        binding.toolbarLayout.setTitle(null);
                    }
                } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
                    if (state != CollapsingToolbarLayoutState.COLLAPSED) {
                        //折叠
                        setTitle(getString(R.string.settings_about));
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        state = CollapsingToolbarLayoutState.COLLAPSED;//修改状态标记为折叠
                    }
                } else {
                    if (state != CollapsingToolbarLayoutState.INTERNEDIATE) {
                        if (state == CollapsingToolbarLayoutState.COLLAPSED) {
                            //由折叠变为中间状态时
                            setTitle(null);
                            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        }
                        binding.toolbarLayout.setTitle(null);
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        state = CollapsingToolbarLayoutState.INTERNEDIATE;//修改状态标记为中间
                    }
                }
            }
        });
    }

    public void onClickUseAttention(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.about_attention_content);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private long lastClickTime;
    private int clickTime;

    private void makeSnackBar(String s) {
        if (binding != null && binding.getRoot() != null) {
            Snackbar snackbar = Snackbar.make(binding.getRoot(), s, Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundResource(R.color.colorPrimary);
            snackbar.show();
        }
    }

    public void onClickIcon(View view) {
        Log.e("click", clickTime + "  ");
        if (System.currentTimeMillis() - lastClickTime < 800) {
            if (System.currentTimeMillis() - lastClickTime < 400) {
                return;
            }
            Log.e("click", clickTime + "  ");
            clickTime++;
            lastClickTime = System.currentTimeMillis();
            if (clickTime == 5) {
                makeSnackBar("妳说最爱薰衣草");
            } else if (clickTime == 17) {
                makeSnackBar("薰衣草永远等待着所爱之人，等待着爱情");
            } else if (clickTime == 34) {
                makeSnackBar("和妳一起的时光如此美妙而短暂");
            } else if (clickTime == 71) {
                makeSnackBar("我愿倾一生守护这段回忆");
            } else if (clickTime == 100) {
                makeSnackBar("愿做一株为妳开放的薰衣草");
            }
        } else {
            clickTime = 0;
            lastClickTime = System.currentTimeMillis();
        }
    }

}
