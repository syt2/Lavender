package party.danyang.nationalgeographic.ui;

import android.annotation.SuppressLint;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import party.danyang.nationalgeographic.utils.Utils;
import party.danyang.nationalgeographic.utils.singleton.PicassoHelper;
import rx.functions.Action1;

public class AlbumActivity extends SwipeBackActivity {
    private static final String TAG = AlbumActivity.class.getSimpleName();
    public static final String INTENT_INDEX = "party.danyang.ng.index";
    public static final String INTENT_TITLES = "party.danyang.ng.titles";
    public static final String INTENT_CONTENTS = "party.danyang.ng.contents";
    public static final String INTENT_AUTHORS = "party.danyang.ng.authors";
    public static final String INTENT_URLS = "party.danyang.ng.urls";
    public static final String INTENT_PAGE_URLS = "party.danyang.ng.page_urls";

    private ActivityAlbumBinding binding;

    private PagerAdapter adapter;
    public boolean mVisible = true;

    private ArrayList<String> titles;
    private ArrayList<String> contents;
    private ArrayList<String> authors;
    private ArrayList<String> urls;
    private ArrayList<String> pageUrls;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_album);
        supportPostponeEnterTransition();
        Intent intent = getIntent();
        if (intent != null) {
            titles = intent.getStringArrayListExtra(INTENT_TITLES);
            contents = intent.getStringArrayListExtra(INTENT_CONTENTS);
            authors = intent.getStringArrayListExtra(INTENT_AUTHORS);
            urls = intent.getStringArrayListExtra(INTENT_URLS);
            pageUrls = intent.getStringArrayListExtra(INTENT_PAGE_URLS);
            index = intent.getIntExtra(INTENT_INDEX, 0);
        }
        initViews();

        setEnterAnimator();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        PicassoHelper.getInstance(this).resumeTag(AlbumFragment.TAG_ALBUM_FRAGMENT);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        PicassoHelper.getInstance(this).pauseTag(AlbumFragment.TAG_ALBUM_FRAGMENT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PicassoHelper.getInstance(this).cancelTag(AlbumFragment.TAG_ALBUM_FRAGMENT);
    }

    private void setEnterAnimator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    String url = urls.get(binding.viewPager.getCurrentItem());
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
        binding.setTitle(titles.get(index));
        binding.setContent(contents.get(index));
        binding.setAuthor(authors.get(index));
        //初始化为可见
        binding.setFullScreen(false);
        //viewPage 滑动时改变title content author
        RxViewPager.pageSelections(binding.viewPager).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer position) {
                binding.setTitle(titles.get(position));
                binding.setContent(contents.get(position));
                binding.setAuthor(authors.get(position));
            }
        });
    }

    private void onToolbarMenuItemClicked(MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.action_yourshotlink) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(pageUrls.get(binding.viewPager.getCurrentItem())));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Utils.makeSnackBar(binding.getRoot(), R.string.not_legal_yourshotlink, true);
            }
        } else if (id == R.id.action_share) {
            int currentPosition = binding.viewPager.getCurrentItem();
            Utils.shareItem(this, urls.get(currentPosition), titles.get(currentPosition),
                    contents.get(currentPosition) + "\n" + authors.get(currentPosition), binding.getRoot());
        }
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            return AlbumFragment.newInstance(urls, position);
        }

        @Override
        public int getCount() {
            return urls.size();
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
