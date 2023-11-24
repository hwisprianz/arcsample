package com.z.arc.wheel2d;

import android.graphics.PointF;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 滚轮布局管理器
 * <p>
 * 从RecyclerView的中间开始布局,在限制滚动时,以此作为滚动界限
 * <p>
 * 可以配置为循环滚动模式
 * <p>
 * Created by Blate on 2023/7/14
 */
@SuppressWarnings("unused")
public class Wheel2DLayoutManager extends RecyclerView.LayoutManager
        implements RecyclerView.SmoothScroller.ScrollVectorProvider {

    private static final String TAG = "Wheel2DLayoutManager";

    /**
     * 有界滚动
     */
    public static final int SCROLL_FEATURE_LIMITED = 0;

    /**
     * 无界滚动,循环滚动
     */
    public static final int SCROLL_FEATURE_UNLIMITED = 2;

    /**
     * 滚动特征定义
     */
    @IntDef(value = {SCROLL_FEATURE_LIMITED, SCROLL_FEATURE_UNLIMITED})
    public @interface ScrollFeature {
    }

    /**
     * 滚动特征
     */
    @ScrollFeature
    private final int mScrollFeature;

    /**
     * 待滚动到的position位置
     * <p>
     * 对于该布局管理器,“滚动到position位置”有明确的含义: 将指定position的View滚动到视图中间
     */
    private int mPendingScrollPosition = RecyclerView.NO_POSITION;

    private int mCenterYPosition = RecyclerView.NO_POSITION;

    @Nullable
    private OnCenterYPositionChangeListener mOnCenterYPositionChangeListener;

    public Wheel2DLayoutManager() {
        this(SCROLL_FEATURE_LIMITED);
    }

    public Wheel2DLayoutManager(@ScrollFeature int scrollFeature) {
        mScrollFeature = scrollFeature;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        final int centerY = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;
        final int centerViewPosition;
        final int centerViewOffsetY;
        if (mPendingScrollPosition == RecyclerView.NO_POSITION) {
            final View centerView = findCenterChild();
            final int position = centerView == null ? RecyclerView.NO_POSITION : getPosition(centerView);
            if (position == RecyclerView.NO_POSITION) {
                // no child, initialize
                centerViewPosition = 0;
                centerViewOffsetY = 0;
            } else {
                // has child, notify data set changed
                centerViewPosition = Math.min(position, getItemCount() - 1);
                centerViewOffsetY = (getDecoratedTop(centerView) + getDecoratedMeasuredHeight(centerView) / 2) - centerY;
            }
        } else {
            // scroll to pending position
            centerViewPosition = mPendingScrollPosition;
            centerViewOffsetY = 0;
        }

        if (state.isPreLayout()) {
            return;
        } else {
            detachAndScrapAttachedViews(recycler);
        }

        if (getItemCount() <= 0) {
            return;
        }

        // layout first view on center y
        final View centerView = getViewForPositionByCycle(recycler, centerViewPosition);
        addView(centerView);
        measureChildWithMargins(centerView, 0, 0);
        final int firstWidth = getDecoratedMeasuredWidth(centerView);
        final int firstHeight = getDecoratedMeasuredHeight(centerView);
        final int firstLeft = getPaddingLeft() + getLeftDecorationWidth(centerView);
        final int firstTop = centerY - firstHeight / 2 + centerViewOffsetY;
        final int firstRight = firstLeft + firstWidth;
        final int firstBottom = firstTop + firstHeight;
        layoutDecoratedWithMargins(centerView, firstLeft, firstTop, firstRight, firstBottom);

        layoutChild(getDecoratedTop(centerView), centerViewPosition - 1, LayoutStatus.LAYOUT_START, recycler);
        layoutChild(getDecoratedBottom(centerView), centerViewPosition + 1, LayoutStatus.LAYOUT_END, recycler);
        checkIfCenterYPositionChanged(findCenterChildPosition());
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.
            State state) {
        final int consumed = fill(dy, recycler);
        offsetChildrenVertical(-consumed);
        checkIfCenterYPositionChanged(findCenterChildPosition());
        return consumed;
    }

    @Override
    public void scrollToPosition(int position) {
        mPendingScrollPosition = position;
        requestLayout();
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        mPendingScrollPosition = RecyclerView.NO_POSITION;
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return true;
    }

    @Override
    public int computeVerticalScrollExtent(@NonNull RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        final View firstView = getChildAt(0);
        if (firstView == null) {
            return 0;
        }
        final View lastView = getChildAt(getChildCount() - 1);
        if (lastView == null) {
            return 0;
        }
        final int top = Math.max(getPaddingTop(), getDecoratedTop(firstView));
        final int bottom = Math.min(getHeight() - getPaddingBottom(), getDecoratedBottom(lastView));
        return bottom - top;
    }

    @Override
    public int computeVerticalScrollRange(@NonNull RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        final View firstView = getChildAt(0);
        if (firstView == null) {
            return 0;
        }
        final View lastView = getChildAt(getChildCount() - 1);
        if (lastView == null) {
            return 0;
        }
        return (getDecoratedBottom(lastView) - getDecoratedTop(firstView)) / getChildCount() * getItemCount();
    }

    @Override
    public int computeVerticalScrollOffset(@NonNull RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        final View centerYView = findCenterChild();
        if (centerYView == null) {
            return 0;
        }
        final int centerYPosition = getPosition(centerYView);
        final View firstView = getChildAt(0);
        if (firstView == null) {
            return 0;
        }
        final View lastView = getChildAt(getChildCount() - 1);
        if (lastView == null) {
            return 0;
        }
        final int averageHeight = (getDecoratedBottom(lastView) - getDecoratedTop(firstView)) / getChildCount();
        final int centerY = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;
        return centerYPosition * averageHeight - (getDecoratedBottom(centerYView) - centerY);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        final Wheel2DSmoothScroller wheel2DSmoothScroller = new Wheel2DSmoothScroller(recyclerView.getContext());
        wheel2DSmoothScroller.setTargetPosition(position);
        startSmoothScroll(wheel2DSmoothScroller);
    }

    @Nullable
    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        final View centerView = findCenterChild();
        if (centerView == null) {
            return null;
        }
        final int centerChildPos = getPosition(centerView);
        if (centerChildPos == RecyclerView.NO_POSITION) {
            return null;
        }
        if (centerChildPos == targetPosition) {
            return null;
        }
        final int direction;
        if (mScrollFeature == SCROLL_FEATURE_LIMITED) {
            direction = targetPosition > centerChildPos ? 1 : -1;
        } else if (mScrollFeature == SCROLL_FEATURE_UNLIMITED) {
            // 无界滚动,找到一个最近的方向
            final int up = (centerChildPos - targetPosition + getItemCount()) % getItemCount();
            final int down = (targetPosition - centerChildPos + getItemCount()) % getItemCount();
            if (up < down) {
                direction = -1;
            } else if (up > down) {
                direction = 1;
            } else {
                direction = targetPosition > centerChildPos ? 1 : -1;
            }
        } else {
            direction = 0;
        }
        return new PointF(0, direction);
    }

    private int fill(int dy, RecyclerView.Recycler recycler) {
        if (dy > 0) {
            // scroll up; fill bottom and recycler top
            final View anchor = getChildAt(getChildCount() - 1);
            if (anchor == null) {
                return 0;
            } else {
                final int anchorPosition = getPosition(anchor);
                int lastCenterY = anchor.getTop() + anchor.getHeight() / 2;
                int offset = getDecoratedBottom(anchor);
                for (int i = anchorPosition + 1;
                     ((mScrollFeature == SCROLL_FEATURE_UNLIMITED || i < getItemCount()) && offset < (getHeight() - getPaddingBottom() + dy));
                     i += 1) {
                    final View view = getViewForPositionByCycle(recycler, i);
                    addView(view);
                    measureChildWithMargins(view, 0, 0);
                    final int width = getDecoratedMeasuredWidth(view);
                    final int height = getDecoratedMeasuredHeight(view);
                    final int left = getPaddingLeft() + getLeftDecorationWidth(view);
                    final int top = offset + getTopDecorationHeight(view);
                    final int right = left + width;
                    final int bottom = top + height;
                    layoutDecoratedWithMargins(view, left, top, right, bottom);
                    offset += height;
                    lastCenterY = top + height / 2;
                    // check if we recycle all off-screen children
                    while (getChildCount() > 0) {
                        final View first = getChildAt(0);
                        if (first != null && first.getBottom() < getPaddingTop() + Math.min(offset - getDecoratedBottom(anchor), dy)) {
                            removeAndRecycleView(first, recycler);
                        } else {
                            break;
                        }
                    }
                }
                if (mScrollFeature == SCROLL_FEATURE_UNLIMITED) {
                    return dy;
                } else {
                    return Math.min(dy, lastCenterY - (getHeight() - getPaddingTop() - getPaddingBottom()) / 2);
                }
            }
        } else if (dy < 0) {
            // scroll down; fill top and recycler bottom
            final View anchor = getChildAt(0);
            if (anchor == null) {
                return 0;
            } else {
                final int anchorPosition = getPosition(anchor);
                int firstCenterY = anchor.getTop() + anchor.getHeight() / 2;
                int offset = getDecoratedTop(anchor);
                for (int i = anchorPosition - 1;
                     ((mScrollFeature == SCROLL_FEATURE_UNLIMITED || i >= 0) && offset > (getPaddingTop() + dy));
                     i -= 1) {
                    final View view = getViewForPositionByCycle(recycler, i);
                    addView(view, 0);
                    measureChildWithMargins(view, 0, 0);
                    final int width = getDecoratedMeasuredWidth(view);
                    final int height = getDecoratedMeasuredHeight(view);
                    final int left = getPaddingLeft() + getLeftDecorationWidth(view);
                    final int top = offset - height - getBottomDecorationHeight(view);
                    final int right = left + width;
                    final int bottom = top + height;
                    layoutDecoratedWithMargins(view, left, top, right, bottom);
                    offset -= height;
                    firstCenterY = top + height / 2;
                    // check if we recycle all off-screen children
                    while (getChildCount() > 0) {
                        final View last = getChildAt(getChildCount() - 1);
                        if (last != null && last.getTop() > getHeight() - getPaddingBottom() + Math.max(offset - getDecoratedTop(anchor), dy)) {
                            removeAndRecycleView(last, recycler);
                        } else {
                            break;
                        }
                    }
                }
                if (mScrollFeature == SCROLL_FEATURE_UNLIMITED) {
                    return dy;
                } else {
                    return Math.max(dy, firstCenterY - (getHeight() - getPaddingTop() - getPaddingBottom()) / 2);
                }
            }
        } else {
            return 0;
        }
    }

    /**
     * 布局子View
     * <p>
     * 向开始或结束方向布局子View(两个方向之一),直到:
     * <li>1. 在无界滚动下,布局空间超过了溢出空间</li>
     * <li>2. 在有界滚动下,布局空间超过了溢出空间或适配器中没有可用的View</li>
     *
     * @param baseline        布局开始的位置
     * @param position        布局开始的position
     * @param layoutDirection 布局方向
     * @param recycler        recycler
     */
    private void layoutChild(int baseline, int position, int layoutDirection, RecyclerView.Recycler recycler) {
        if (layoutDirection == LayoutStatus.LAYOUT_START) {
            int offset = baseline;
            for (int i = position;
                 (mScrollFeature == SCROLL_FEATURE_UNLIMITED || i >= 0) && offset >= getPaddingTop();
                 i -= 1
            ) {
                final View view = getViewForPositionByCycle(recycler, i);
                addView(view, 0);
                measureChildWithMargins(view, 0, 0);
                final int width = getDecoratedMeasuredWidth(view);
                final int height = getDecoratedMeasuredHeight(view);
                final int left = getPaddingLeft() + getLeftDecorationWidth(view);
                final int top = offset - height - getBottomDecorationHeight(view);
                final int right = left + width;
                final int bottom = top + height;
                layoutDecoratedWithMargins(view, left, top, right, bottom);
                offset -= height;
            }
        } else if (layoutDirection == LayoutStatus.LAYOUT_END) {
            int offset = baseline;
            for (int i = position;
                 (mScrollFeature == SCROLL_FEATURE_UNLIMITED || i < getItemCount()) && offset < getHeight() - getPaddingBottom();
                 i += 1) {
                final View view = getViewForPositionByCycle(recycler, i);
                addView(view);
                measureChildWithMargins(view, 0, 0);
                final int width = getDecoratedMeasuredWidth(view);
                final int height = getDecoratedMeasuredHeight(view);
                final int left = getPaddingLeft() + getLeftDecorationWidth(view);
                final int top = offset + getTopDecorationHeight(view);
                final int right = left + width;
                final int bottom = top + height;
                layoutDecoratedWithMargins(view, left, top, right, bottom);
                offset += height;
            }
        }
    }

    /**
     * 查找中心位置的条目
     *
     * @return 中心位置的条目, 可能为null. 这多半是出了什么意外情况
     */
    @Nullable
    public View findCenterChild() {
        if (getChildCount() == 0) {
            return null;
        } else {
            // 大部分情况下(条目充满整个屏幕),中间index的View就在屏幕中心,从此处开始查找
            final int centerIndex = getChildCount() / 2;
            final View centerView = getChildAt(centerIndex);
            if (centerView != null) {
                final int centerViewCheckResult = checkChildIfInCenterY(centerView);
                if (centerViewCheckResult < 0) {
                    View target = null;
                    for (int i = centerIndex + 1; i < getChildCount(); i += 1) {
                        final View view = getChildAt(i);
                        final int checkResult = view == null ? 1 : checkChildIfInCenterY(view);
                        if (checkResult == 0) {
                            target = view;
                            break;
                        } else if (checkResult > 0) {
                            break;
                        }
                    }
                    return target;
                } else if (centerViewCheckResult > 0) {
                    View target = null;
                    for (int i = centerIndex - 1; i >= 0; i -= 1) {
                        final View view = getChildAt(i);
                        final int checkResult = view == null ? -1 : checkChildIfInCenterY(view);
                        if (checkResult == 0) {
                            target = view;
                            break;
                        } else if (checkResult < 0) {
                            break;
                        }
                    }
                    return target;
                } else {
                    return centerView;
                }
            } else {
                return null;
            }
        }
    }

    /**
     * 获取中心位置的条目position
     *
     * @return position
     */
    public int findCenterChildPosition() {
        final View centerView = findCenterChild();
        return centerView == null ? RecyclerView.NO_POSITION : getPosition(centerView);
    }

    /**
     * 获取当前滚动特征
     *
     * @return 滚动特征
     */
    @ScrollFeature
    public int getScrollFeature() {
        return mScrollFeature;
    }

    /**
     * 环状的通过position获取View,这意味着position可以是负数或大于适配器中的条目数
     *
     * @param recycler recycler
     * @param position position
     * @return view
     */
    private View getViewForPositionByCycle(RecyclerView.Recycler recycler, int position) {
        return recycler.getViewForPosition((position % getItemCount() + getItemCount()) % getItemCount());
    }

    /**
     * 检查View是否在中心位置
     *
     * @param view view
     * @return 0:在中心位置,1:在中心位置下方,-1:在中心位置上方
     */
    private int checkChildIfInCenterY(@NonNull View view) {
        final int centerY = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;
        if (getDecoratedTop(view) > centerY) {
            return 1;
        } else if (getDecoratedBottom(view) < centerY) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * 检查中心位置的条目position是否发生变化
     *
     * @param position 中心位置的条目position
     */
    private void checkIfCenterYPositionChanged(int position) {
        if (mCenterYPosition != position) {
            mCenterYPosition = position;
            if (mOnCenterYPositionChangeListener != null) {
                mOnCenterYPositionChangeListener.onCenterYPositionChanged(position);
            }
        }
    }

    /**
     * 设置中心位置的条目position变化监听器
     *
     * @param listener 中心位置的条目position变化监听器,可空
     */
    public void setOnCenterYPositionChangeListener(@Nullable OnCenterYPositionChangeListener listener) {
        this.mOnCenterYPositionChangeListener = listener;
    }

    /**
     * 布局状态
     */
    static class LayoutStatus {

        /**
         * 向上布局
         */
        static final int LAYOUT_START = -1;

        /**
         * 向下布局
         */
        static final int LAYOUT_END = 1;

    }

    /**
     * 中心位置的条目position变化监听器
     */
    public interface OnCenterYPositionChangeListener {

        /**
         * 中心位置的条目position变化
         *
         * @param position 中心位置的条目position
         */
        void onCenterYPositionChanged(int position);

    }

}