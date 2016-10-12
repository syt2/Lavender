package party.danyang.nationalgeographic.widget;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewParent;

/**
 * Created by dream on 16-8-17.
 */
public class StateCollapsingToolbarLayout extends CollapsingToolbarLayout {
    private static final String TAG = "StateCollapsingToolbarLayout";
    public CollapsingToolbarLayoutState state;

    public enum CollapsingToolbarLayoutState {
        EXPANDED,
        COLLAPSED,
        INTERNEDIATE
    }

    private OnStateChangedListener mOnStateChangedListener;

    public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        if (!(getParent() instanceof AppBarLayout)) {
            Log.e(TAG, "onStateChangeListener need AppBarLayout as a parent view");
            return;
        }
        this.mOnStateChangedListener = onStateChangedListener;
    }


    public StateCollapsingToolbarLayout(Context context) {
        super(context);
    }

    public StateCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StateCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent parent = getParent();
        if (mOnStateChangedListener != null) {
            setStateChangedListener();
        }
    }

    private void setStateChangedListener() {
        AppBarLayout appBarLayout = (AppBarLayout) getParent();
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == 0) {
                    if (state != CollapsingToolbarLayoutState.EXPANDED) {
                        //修改状态标记为展开
                        state = CollapsingToolbarLayoutState.EXPANDED;
                        mOnStateChangedListener.onExpanded();
                    }
                } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
                    if (state != CollapsingToolbarLayoutState.COLLAPSED) {
                        //修改状态标记为折叠
                        state = CollapsingToolbarLayoutState.COLLAPSED;
                        mOnStateChangedListener.onCollapsed();
                    }
                } else {
                    if (state != CollapsingToolbarLayoutState.INTERNEDIATE) {
                        state = CollapsingToolbarLayoutState.INTERNEDIATE;
                    }
                    mOnStateChangedListener.onInternediate();
                }
            }
        });
    }


}