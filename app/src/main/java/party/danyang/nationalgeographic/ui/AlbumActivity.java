package party.danyang.nationalgeographic.ui;

import android.annotation.SuppressLint;
import android.app.SharedElementCallback;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding.support.v4.view.RxViewPager;
import com.jakewharton.rxbinding.view.RxView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.yokeyword.swipebackfragment.SwipeBackActivity;
import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.model.album.Picture;
import rx.functions.Action1;

public class AlbumActivity extends SwipeBackActivity {
    private static final String TAG = AlbumActivity.class.getSimpleName();
    public static final String INTENT_PICTURES = "party.danyang.ng.pictures";
    public static final String INTENT_INDEX = "party.danyang.ng.index";

    private ViewPager viewPager;
    private PagerAdapter adapter;

    private TextView title;
    private TextView content;
    private TextView author;
    private View scrollContent;
    public boolean mVisible = true;

    private ArrayList<Picture> pictures;

    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        supportPostponeEnterTransition();
        Intent intent = getIntent();
        if (intent != null) {
            pictures = intent.getParcelableArrayListExtra(INTENT_PICTURES);
            index = intent.getIntExtra(INTENT_INDEX, 0);
        }
        initViews();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    String url = pictures.get(viewPager.getCurrentItem()).getUrl();
                    AlbumFragment fragment = (AlbumFragment) adapter.instantiateItem(viewPager, viewPager.getCurrentItem());
                    sharedElements.clear();
                    sharedElements.put(url, fragment.getSharedElement());
                }
            });
        }
    }

    @Override
    public void supportFinishAfterTransition() {
        Intent data = new Intent();
        data.putExtra(INTENT_INDEX, viewPager.getCurrentItem());
        setResult(RESULT_OK, data);
        super.supportFinishAfterTransition();
    }

    private void initViews() {
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        title = (TextView) findViewById(R.id.tv_title);
        content = (TextView) findViewById(R.id.tv_content);
        author = (TextView) findViewById(R.id.tv_author);
        scrollContent = findViewById(R.id.scroll_content);

        //长按图片内容复制剪切报
        RxView.longClicks(findViewById(R.id.container)).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                saveContentToClipboard();
            }
        });
        //初始化title content author
        setTextToTextViews(index);
        adapter = new PagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(index);
        //viewPage 滑动时改变title content author
        RxViewPager.pageSelections(viewPager).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer position) {
                setTextToTextViews(position);
            }
        });
    }

    private void setTextToTextViews(int position) {
        title.setText(pictures.get(position).getTitle());
        content.setText(pictures.get(position).getContent());
        if (!TextUtils.isEmpty(pictures.get(position).getAuthor())) {
            author.setText(getString(R.string.author) + "(" + pictures.get(position).getAuthor() + ")");
        } else {
            author.setText("");
        }
    }

    private void saveContentToClipboard() {
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        String s = "";
        if (title != null) {
            s = title.getText() + "\n" + content.getText() + "\n" + author.getText();
        }
        ClipData data = ClipData.newPlainText("text", s);
        cm.setPrimaryClip(data);
        if (scrollContent != null) {
            Snackbar.make(scrollContent, R.string.content_copy, Snackbar.LENGTH_SHORT).show();
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
            viewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
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
            scrollContent.setVisibility(View.VISIBLE);
        }
    };

    public void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        scrollContent.setVisibility(View.GONE);
        mVisible = false;

        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    public void show() {
        viewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }
}
