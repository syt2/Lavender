package party.danyang.nationalgeographic.ui.base;

import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;

import me.yokeyword.swipebackfragment.SwipeBackActivity;
import party.danyang.nationalgeographic.databinding.LayoutToolbarBinding;
import party.danyang.nationalgeographic.widget.OnStateChangedListener;
import rx.functions.Action1;

/**
 * Created by dream on 16-8-12.
 */
public class ToolbarActivity extends SwipeBackActivity {

    public void setupToolbar(LayoutToolbarBinding toolbarBinding) {
        setSupportActionBar(toolbarBinding.toolbar);
        RxToolbar.navigationClicks(toolbarBinding.toolbar).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                supportFinishAfterTransition();
            }
        });
        toolbarBinding.toolbarLayout.setOnStateChangedListener(new OnStateChangedListener() {
            @Override
            public void onExpanded() {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }

            @Override
            public void onCollapsed() {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            @Override
            public void onInternediate() {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        });
    }
}
