package com.z.arc.wheel2d;

import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * WheelLayoutManager 的平滑滚动器
 * <p>
 * 和{@link androidx.recyclerview.widget.LinearSmoothScroller}很相似
 * <p>
 * Created by Blate on 2023/8/16
 */
public class Wheel2DSmoothScroller extends RecyclerView.SmoothScroller {

    private static final float MILLISECONDS_PER_INCH = 25f;

    private static final int TARGET_SEEK_SCROLL_DISTANCE_PX = 10000;

    private static final float TARGET_SEEK_EXTRA_SCROLL_RATIO = 1.2f;

    private final DisplayMetrics mDisplayMetrics;

    private boolean mHasCalculatedMillisPerPixel = false;

    private float mMillisPerPixel;

    protected final LinearInterpolator mLinearInterpolator = new LinearInterpolator();
    protected final DecelerateInterpolator mDecelerateInterpolator = new DecelerateInterpolator();

    // Temporary variables to keep track of the interim scroll target. These values do not
    // point to a real item position, rather point to an estimated location pixels.
    protected int mInterimTargetDy = 0;

    public Wheel2DSmoothScroller(Context context) {
        mDisplayMetrics = context.getResources().getDisplayMetrics();
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void onSeekTargetStep(int dx, int dy, @NonNull RecyclerView.State state, @NonNull Action action) {
        if (getChildCount() == 0) {
            stop();
            return;
        }
        mInterimTargetDy = clampApplyScroll(mInterimTargetDy, dy);
        if (mInterimTargetDy == 0) {
            // everything is valid, keep going
            updateActionForInterimTarget(action);
        }
    }

    @Override
    protected void onTargetFound(@NonNull View targetView, @NonNull RecyclerView.State state, @NonNull Action action) {
        final RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null || !layoutManager.canScrollVertically()) {
            return;
        }
        final int targetViewY = layoutManager.getDecoratedTop(targetView) + layoutManager.getDecoratedMeasuredHeight(targetView) / 2;
        final int parentY = layoutManager.getPaddingTop() + layoutManager.getHeight() / 2;
        final int dy = parentY - targetViewY;
        final int time = calculateTimeForDeceleration(dy);
        if (time > 0) {
            action.update(0, -dy, time, mDecelerateInterpolator);
        }

    }

    private int clampApplyScroll(int tmpDt, int dt) {
        final int before = tmpDt;
        tmpDt -= dt;
        if (before * tmpDt <= 0) { // changed sign, reached 0 or was 0, reset
            return 0;
        }
        return tmpDt;
    }

    /**
     * When the target scroll position is not a child of the RecyclerView, this method calculates
     * a direction vector towards that child and triggers a smooth scroll.
     *
     * @see #computeScrollVectorForPosition(int)
     */
    protected void updateActionForInterimTarget(Action action) {
        // find an interim target position
        PointF scrollVector = computeScrollVectorForPosition(getTargetPosition());
        if (scrollVector == null || (scrollVector.x == 0 && scrollVector.y == 0)) {
            final int target = getTargetPosition();
            action.jumpTo(target);
            stop();
            return;
        }
        normalize(scrollVector);

        mInterimTargetDy = (int) (TARGET_SEEK_SCROLL_DISTANCE_PX * scrollVector.y);
        final int time = calculateTimeForScrolling(TARGET_SEEK_SCROLL_DISTANCE_PX);
        // To avoid UI hiccups, trigger a smooth scroll to a distance little further than the
        // interim target. Since we track the distance travelled in onSeekTargetStep callback, it
        // won't actually scroll more than what we need.
        action.update(0,
                (int) (mInterimTargetDy * TARGET_SEEK_EXTRA_SCROLL_RATIO),
                (int) (time * TARGET_SEEK_EXTRA_SCROLL_RATIO), mLinearInterpolator);
    }

    /**
     * <p>Calculates the time for deceleration so that transition from LinearInterpolator to
     * DecelerateInterpolator looks smooth.</p>
     *
     * @param dx Distance to scroll
     * @return Time for DecelerateInterpolator to smoothly traverse the distance when transitioning
     * from LinearInterpolation
     */
    protected int calculateTimeForDeceleration(int dx) {
        // we want to cover same area with the linear interpolator for the first 10% of the
        // interpolation. After that, deceleration will take control.
        // area under curve (1-(1-x)^2) can be calculated as (1 - x/3) * x * x
        // which gives 0.100028 when x = .3356
        // this is why we divide linear scrolling time with .3356
        return (int) Math.ceil(calculateTimeForScrolling(dx) / .3356);
    }

    /**
     * Calculates the time it should take to scroll the given distance (in pixels)
     *
     * @param dx Distance in pixels that we want to scroll
     * @return Time in milliseconds
     * @see #calculateSpeedPerPixel(DisplayMetrics)
     */
    protected int calculateTimeForScrolling(int dx) {
        // In a case where dx is very small, rounding may return 0 although dx > 0.
        // To avoid that issue, ceil the result so that if dx > 0, we'll always return positive
        // time.
        return (int) Math.ceil(Math.abs(dx) * getSpeedPerPixel());
    }

    private float getSpeedPerPixel() {
        if (!mHasCalculatedMillisPerPixel) {
            mMillisPerPixel = calculateSpeedPerPixel(mDisplayMetrics);
            mHasCalculatedMillisPerPixel = true;
        }
        return mMillisPerPixel;
    }

    /**
     * Calculates the scroll speed.
     *
     * <p>By default, LinearSmoothScroller assumes this method always returns the same value and
     * caches the result of calling it.
     *
     * @param displayMetrics DisplayMetrics to be used for real dimension calculations
     * @return The time (in ms) it should take for each pixel. For instance, if returned value is
     * 2 ms, it means scrolling 1000 pixels with LinearInterpolation should take 2 seconds.
     */
    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
    }

}
