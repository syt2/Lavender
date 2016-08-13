package party.danyang.nationalgeographic.ui.base;

import android.support.design.widget.AppBarLayout;
import android.view.View;

import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;

import me.yokeyword.swipebackfragment.SwipeBackActivity;
import party.danyang.nationalgeographic.databinding.LayoutToolbarBinding;
import rx.functions.Action1;

/**
 * Created by dream on 16-8-12.
 */
public class ToolbarActivity extends SwipeBackActivity {

    public CollapsingToolbarLayoutState state;

    public enum CollapsingToolbarLayoutState {
        EXPANDED,
        COLLAPSED,
        INTERNEDIATE
    }

    public void setupToolbar(LayoutToolbarBinding toolbarBinding) {
        setSupportActionBar(toolbarBinding.toolbar);
        RxToolbar.navigationClicks(toolbarBinding.toolbar).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                supportFinishAfterTransition();
            }
        });
        toolbarBinding.appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (verticalOffset == 0) {
                    if (state != CollapsingToolbarLayoutState.EXPANDED) {
                        state = CollapsingToolbarLayoutState.EXPANDED;//修改状态标记为展开
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    }
                } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
                    if (state != CollapsingToolbarLayoutState.COLLAPSED) {
                        //折叠
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        state = CollapsingToolbarLayoutState.COLLAPSED;//修改状态标记为折叠
                    }
                } else {
                    if (state != CollapsingToolbarLayoutState.INTERNEDIATE) {
                        if (state == CollapsingToolbarLayoutState.COLLAPSED) {
                            //由折叠变为中间状态时
                            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        }
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        state = CollapsingToolbarLayoutState.INTERNEDIATE;//修改状态标记为中间
                    }
                }
            }
        });
    }
}
