package party.danyang.nationalgeographic.ui;

import android.annotation.SuppressLint;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.jakewharton.rxbinding.support.v4.view.RxViewPager;
import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.yokeyword.swipebackfragment.SwipeBackActivity;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.databinding.ActivityAlbumBinding;
import party.danyang.nationalgeographic.model.album.Picture;
import rx.functions.Action1;

public class AlbumActivity extends SwipeBackActivity {
    private static final String TAG = AlbumActivity.class.getSimpleName();
    public static final String INTENT_PICTURES = "party.danyang.ng.pictures";
    public static final String INTENT_INDEX = "party.danyang.ng.index";

    private ActivityAlbumBinding binding;

    private PagerAdapter adapter;
    public boolean mVisible = true;

    private ArrayList<Picture> pictures;

    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_album);
        supportPostponeEnterTransition();
        Intent intent = getIntent();
        if (intent != null) {
            pictures = intent.getParcelableArrayListExtra(INTENT_PICTURES);
            index = intent.getIntExtra(INTENT_INDEX, 0);
        }
        initViews();

        setEnterAnimator();
    }

    private void setEnterAnimator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    String url = pictures.get(binding.viewPager.getCurrentItem()).getUrl();
                    AlbumFragment fragment = (AlbumFragment) adapter.instantiateItem(binding.viewPager, binding.viewPager.getCurrentItem());
                    sharedElements.clear();
                    sharedElements.put(url, fragment.getSharedElement());
                }
            });
        }
    }

    @Override
    public void supportFinishAfterTransition() {
        Intent data = new Intent();
        data.putExtra(INTENT_INDEX, binding.viewPager.getCurrentItem());
        setResult(RESULT_OK, data);
        super.supportFinishAfterTransition();
    }

    private void initViews() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_grey_300_24dp);
        setSupportActionBar(binding.toolbar);
        setTitle(null);
        RxToolbar.navigationClicks(binding.toolbar).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                supportFinishAfterTransition();
            }
        });
        RxToolbar.itemClicks(binding.toolbar).subscribe(new Action1<MenuItem>() {
            @Override
            public void call(MenuItem menuItem) {
                onToolbarMenuItemClicked(menuItem);
            }
        });
        adapter = new PagerAdapter();
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(index);
        //初始化title content author
        binding.setPicture(pictures.get(index));
        //初始化为可见
        binding.setFullScreen(false);
        //viewPage 滑动时改变title content author
        RxViewPager.pageSelections(binding.viewPager).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer position) {
                binding.setPicture(pictures.get(position));
            }
        });
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

    private void onToolbarMenuItemClicked(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_yourshotlink) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(pictures.get(index).getYourshotlink()));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                makeSnackBar(R.string.not_legal_yourshotlink, true);
            }
        }
    }

    private void makeSnackBar(String msg, boolean lengthShort) {
        if (binding != null && binding.getRoot() != null) {
            Snackbar.make(binding.getRoot(), msg, lengthShort ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG).show();
        }
    }

    private void makeSnackBar(int resId, boolean lengthShort) {
        if (binding != null && binding.getRoot() != null) {
            Snackbar.make(binding.getRoot(), resId, lengthShort ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG).show();
        }
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            return AlbumFragment.newInstance(pictures, position);
        }

        @Override
        public int getCount() {
            return pictures.size();
        }
    }


    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            binding.viewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            binding.setFullScreen(false);
        }
    };

    public void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        binding.setFullScreen(true);
        mVisible = false;

        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    public void show() {
        binding.viewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }
}
