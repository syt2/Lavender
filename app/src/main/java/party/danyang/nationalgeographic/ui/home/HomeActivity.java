package party.danyang.nationalgeographic.ui.home;

import android.app.SharedElementCallback;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.jakewharton.rxbinding.view.RxView;
import com.umeng.analytics.MobclickAgent;

import java.util.List;
import java.util.Map;

import io.realm.Realm;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.adapter.AlbumListAdapter;
import party.danyang.nationalgeographic.adapter.AlbumListUSAdapter;
import party.danyang.nationalgeographic.databinding.ActivityHomeBinding;
import party.danyang.nationalgeographic.ui.AboutActivity;
import party.danyang.nationalgeographic.ui.AlbumActivity;
import party.danyang.nationalgeographic.ui.RandomAlbumActivity;
import party.danyang.nationalgeographic.ui.SettingsActivity;
import party.danyang.nationalgeographic.utils.SettingsModel;
import party.danyang.nationalgeographic.utils.Utils;
import party.danyang.nationalgeographic.utils.singleton.PicassoHelper;
import party.danyang.nationalgeographic.widget.OnStateChangedListener;
import rx.functions.Action1;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final String TAG_TW = "tag.tw";
    private static final String TAG_US = "tag.us";

    private static final String SAVED_INSTANCE_STATE_TYPE = "savedInstanceState.type";

    private Type type = Type.TW;
    private ActivityHomeBinding binding;

    public Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        realm = Realm.getDefaultInstance();
        initViews();

        changeFragmentToType(type);
        setExitAnimator();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        PicassoHelper.getInstance(this).resumeTag(AlbumListAdapter.TAG_HOME);
        PicassoHelper.getInstance(this).resumeTag(AlbumListUSAdapter.TAG_LIST_US);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        PicassoHelper.getInstance(this).pauseTag(AlbumListAdapter.TAG_HOME);
        PicassoHelper.getInstance(this).pauseTag(AlbumListUSAdapter.TAG_LIST_US);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PicassoHelper.getInstance(this).cancelTag(AlbumListAdapter.TAG_HOME);
        PicassoHelper.getInstance(this).cancelTag(AlbumListUSAdapter.TAG_LIST_US);
        realm.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_INSTANCE_STATE_TYPE, type.ordinal());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            type = Type.valueOf(savedInstanceState.getInt(SAVED_INSTANCE_STATE_TYPE));
            changeFragmentToType(type);
        }
    }

    private void initViews() {
        setupToolbar();
        setupDrawer();
    }

    public void setupToolbar() {
        setSupportActionBar(binding.toolbarContent.toolbar);
        //双击toolbar recyclerView回滚
        RxView.clicks(binding.toolbarContent.toolbar).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                onToolbarClicked();
            }
        });
    }

    private void setupDrawer() {
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbarContent.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.setDrawerListener(toggle);
        if (type == Type.TW) {
            binding.navView.setCheckedItem(R.id.nav_group_pic);
        } else if (type == Type.US) {
            binding.navView.setCheckedItem(R.id.nav_single_pic_us);
        }
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                onNavItemSelected(item);
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        binding.toolbarContent.toolbarLayout.setOnStateChangedListener(new OnStateChangedListener() {
            @Override
            public void onExpanded() {
                toggle.setDrawerIndicatorEnabled(false);
            }

            @Override
            public void onCollapsed() {
                toggle.setDrawerIndicatorEnabled(true);
            }

            @Override
            public void onInternediate() {
                toggle.setDrawerIndicatorEnabled(false);
            }
        });
    }

    private void onNavItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_group_pic:
                if (type == Type.TW) break;
                type = Type.TW;
                changeFragmentToType(type);
                break;
            case R.id.nav_single_pic_us:
                if (type == Type.US) break;
                type = Type.US;
                changeFragmentToType(type);
                break;
            case R.id.nav_random_pic:
                Intent intent1 = new Intent(this, RandomAlbumActivity.class);
                ActivityOptionsCompat options1 = ActivityOptionsCompat
                        .makeCustomAnimation(this, R.anim.slide_right_in, R.anim.slide_right_out);
                ActivityCompat.startActivity(this, intent1, options1.toBundle());
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeCustomAnimation(this, R.anim.slide_right_in, R.anim.slide_right_out);
                ActivityCompat.startActivity(this, intent, options.toBundle());
                break;
            case R.id.nav_about:
                Intent i = new Intent(this, AboutActivity.class);
                ActivityOptionsCompat opts = ActivityOptionsCompat
                        .makeCustomAnimation(this, R.anim.slide_right_in, R.anim.slide_right_out);
                ActivityCompat.startActivity(this, i, opts.toBundle());
                break;
        }
    }

    private RecyclerViewTWFragment getTWFragment() {
        return (RecyclerViewTWFragment) getSupportFragmentManager().findFragmentByTag(TAG_TW);
    }

    private RecyclerViewUSFragment getUSFragment() {
        return (RecyclerViewUSFragment) getSupportFragmentManager().findFragmentByTag(TAG_US);
    }

    private void changeFragmentToType(Type type) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment tw = getTWFragment();
        Fragment us = getUSFragment();
        if (type == Type.TW) {
            if (tw != null) {
                //TW added
                if (us != null) {
                    //US added
                    transaction.hide(us).show(tw).commit();
                } else {
                    //US not added
                    transaction.show(tw).commit();
                }
            } else {
                //TW not added
                if (us != null) {
                    //US added
                    transaction.hide(us).add(R.id.recycler_content_fragment, RecyclerViewTWFragment.getInstance(), TAG_TW).commit();
                } else {
                    //US not added
                    transaction.add(R.id.recycler_content_fragment, RecyclerViewTWFragment.getInstance(), TAG_TW).commit();
                }
            }

        } else if (type == Type.US) {
            if (us != null) {
                if (tw != null) {
                    transaction.hide(tw).show(us).commit();
                } else {
                    transaction.show(us).commit();
                }
            } else {
                if (tw != null) {
                    transaction.hide(tw).add(R.id.recycler_content_fragment, RecyclerViewUSFragment.getInstance(), TAG_US).commit();
                } else {
                    transaction.add(R.id.recycler_content_fragment, RecyclerViewUSFragment.getInstance(), TAG_US).commit();
                }
            }
        }

    }

    //double click back to exit
    private long lastClickBackTime;

    @Override
    public void onBackPressed() {
        if (SettingsModel.getDoubleClickExit(this)) {
            if (System.currentTimeMillis() - lastClickBackTime < 500) {
                supportFinishAfterTransition();
            } else {
                Utils.makeSnackBar(binding.getRoot(), R.string.click_ono_more_to_exit, true);
                lastClickBackTime = System.currentTimeMillis();
            }
        } else {
            supportFinishAfterTransition();
        }
    }

    //click toolbar then scroll to top
    private long lastClickToolbarTime;

    private void onToolbarClicked() {
        if (System.currentTimeMillis() - lastClickToolbarTime < 400) {
            if (type == Type.TW && getTWFragment() != null) {
                getTWFragment().binding.recycler.smoothScrollToPosition(0);
            } else if (type == Type.US && getUSFragment() != null) {
                getUSFragment().binding.recycler.smoothScrollToPosition(0);
            }
        } else {
            lastClickToolbarTime = System.currentTimeMillis();
        }
    }

    //animation between activities
    //just used when type is US
    private Bundle reenterState;

    private void setExitAnimator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setExitSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    if (type == Type.US && reenterState != null) {
                        int position = reenterState.getInt(AlbumActivity.INTENT_INDEX, 0);
                        //判空
                        if (getUSFragment() == null || getUSFragment().adapter == null
                                || position >= getUSFragment().adapter.size())
                            return;
                        sharedElements.clear();
                        sharedElements.put(getUSFragment().adapter.get(position).getUrl(), getUSFragment().layoutManager.findViewByPosition(position));
                        reenterState = null;
                    }
                }
            });
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        if (type == Type.US && getUSFragment() != null) {
            reenterState = new Bundle(data.getExtras());
            int position = reenterState.getInt(AlbumActivity.INTENT_INDEX, 0);
            if (getUSFragment().binding == null || position >= getUSFragment().adapter.size())
                return;
            supportPostponeEnterTransition();
            getUSFragment().binding.recycler.scrollToPosition(position);
            getUSFragment().binding.recycler.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    getUSFragment().binding.recycler.getViewTreeObserver().removeOnPreDrawListener(this);
                    getUSFragment().binding.recycler.requestLayout();
                    supportStartPostponedEnterTransition();
                    return true;
                }
            });
        }
    }
}
