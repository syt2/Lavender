package party.danyang.nationalgeographic.widget;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yaki on 16-7-2.
 */
public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior {

    private static final android.view.animation.Interpolator INTERPOLATOR =
            new FastOutSlowInInterpolator();
    private boolean mIsAnimating = false;


    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
                ||super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyConsumed > 0 && !mIsAnimating && child.getVisibility() == View.VISIBLE) {
            hide(child);
        } else if (dyConsumed <= 0 && child.getVisibility() != View.VISIBLE) {
            show(child);
        }
    }

    private void hide(FloatingActionButton fab) {
        ViewCompat.animate(fab).scaleX(0.0F).scaleY(0.0F).alpha(0.0F)
                .translationY(fab.getHeight())
                .setInterpolator(INTERPOLATOR).withLayer()
                .setListener(new ViewPropertyAnimatorListener() {
                    public void onAnimationStart(View view) {
                        mIsAnimating = true;
                    }

                    public void onAnimationCancel(View view) {
                        mIsAnimating = false;
                    }

                    public void onAnimationEnd(View view) {
                        mIsAnimating = false;
                        view.setVisibility(View.GONE);
                    }
                }).start();
    }

    private void show(FloatingActionButton fab) {
        fab.setVisibility(View.VISIBLE);
        ViewCompat.animate(fab).scaleX(1.0F).scaleY(1.0F).alpha(1.0F)
                .translationY(0.0F)
                .setInterpolator(INTERPOLATOR).withLayer().setListener(null)
                .start();
    }
}
