package party.danyang.nationalgeographic.ui.base;

import android.view.View;

import me.yokeyword.swipebackfragment.SwipeBackActivity;
import party.danyang.nationalgeographic.databinding.LayoutToolbarBinding;
import party.danyang.nationalgeographic.widget.OnStateChangedListener;

/**
 * Created by dream on 16-8-12.
 */
public class ToolbarActivity extends SwipeBackActivity {

    public void setupToolbar(LayoutToolbarBinding toolbarBinding) {
        setSupportActionBar(toolbarBinding.toolbar);
        toolbarBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
