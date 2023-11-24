package com.z.arc.recyclerview.listener;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


/**
 * 处理Recyclerview 条目的事件.
 *
 * <li>附加条目单击事件监听: {@link #attachOnItemClickListener(RecyclerView, OnItemClickListener)}</li>
 * <li>附加条目长按事件监听: {@link #attachOnItemLongClickListener(RecyclerView, OnItemLongClickListener)}</li>
 * <li>移除条目事件监听: {@link #detachItemListener(RecyclerView, RecyclerView.OnItemTouchListener)}</li>
 * <p>
 * 这些监听器可以处理 translation 和 scale 后的视图,<b>但是不能处理 rotation 后的视图</b>.如果视图经过了 rotation, 那么请考虑其他方法
 * <p>
 * 一个可行的方法是在{@link RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)} 中为视图设置监听器, 然后回调到外部
 * <p>
 * 使用 {@link RecyclerView.OnItemTouchListener} 进行无侵入的监听. 但是这要求所有添加到Recyclerview上的 {@link RecyclerView.OnItemTouchListener} 都不能消费事件,否则会导致某些事件处理异常
 * <p>
 * Created by Blate on 2023/6/15
 */
@SuppressWarnings("unused")
public class RecyclerViewListenerTools {

    /**
     * 附加一个条目单击事件监听器.
     * <p>
     * 可以附加多个监听器, 每一个监听器都会被调用.
     *
     * @param recyclerview 要为哪一个监听器附加条目监听器
     * @param listener     监听器.当发生点击事件是,可以告知在哪一个Recyclerview的哪个position的哪个View上发生了事件.如果找不到符合要求的事件,事件将会指派到根视图(也就是整个ItemView)
     * @return {@link RecyclerView.OnItemTouchListener}; 如果不再需要监听,可以通过{@link RecyclerView#removeOnItemTouchListener(RecyclerView.OnItemTouchListener)}移除监听器
     */
    @NonNull
    public static RecyclerView.OnItemTouchListener attachOnItemClickListener(@NonNull RecyclerView recyclerview, @NonNull OnItemClickListener listener) {
        final RecyclerView.OnItemTouchListener onItemTouchListener = new OnItemTouchListenerWithGestureDetector(recyclerview.getContext(), new OnGestureListenerForSingleTapUp(recyclerview, listener));
        recyclerview.addOnItemTouchListener(onItemTouchListener);
        return onItemTouchListener;
    }

    /**
     * 附加一个条目长按事件监听器.
     * <p>
     * 可以附加多个监听器, 每一个监听器都会被调用.
     *
     * @param recyclerView 要为哪一个监听器附加条目监听器
     * @param listener     监听器.当发生长按事件是,可以告知在哪一个Recyclerview的哪个position的哪个View上发生了事件.如果找不到符合要求的事件,事件将会指派到根视图(也就是整个ItemView)
     * @return {@link RecyclerView.OnItemTouchListener}; 如果不再需要监听,可以通过{@link RecyclerView#removeOnItemTouchListener(RecyclerView.OnItemTouchListener)}移除监听器
     */
    @NonNull
    public static RecyclerView.OnItemTouchListener attachOnItemLongClickListener(RecyclerView recyclerView, OnItemLongClickListener listener) {
        final RecyclerView.OnItemTouchListener onItemTouchListener = new OnItemTouchListenerWithGestureDetector(recyclerView.getContext(), new OnGestureListenerForLongPress(recyclerView, listener));
        recyclerView.addOnItemTouchListener(onItemTouchListener);
        return onItemTouchListener;
    }

    /**
     * 移除一个条目事件监听器.
     *
     * @param recyclerview 要移除哪一个监听器的条目监听器
     * @param listener     要移除的监听器
     */
    public static void detachItemListener(@NonNull RecyclerView recyclerview, @NonNull RecyclerView.OnItemTouchListener listener) {
        recyclerview.removeOnItemTouchListener(listener);
    }

    /**
     * 使用GestureDetector处理触摸事件的Recyclerview条目监听器.
     */
    private static class OnItemTouchListenerWithGestureDetector
            implements RecyclerView.OnItemTouchListener {

        @NonNull
        private final GestureDetector mGestureDetector;

        private OnItemTouchListenerWithGestureDetector(Context context, @NonNull GestureDetector.OnGestureListener onGestureListener) {
            this.mGestureDetector = new GestureDetector(context, onGestureListener);
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            return mGestureDetector.onTouchEvent(e);
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

    }

    /**
     * 可以寻找事件发生的View的手势监听器
     */
    private static class OnGestureListenerForFindInTouch
            implements GestureDetector.OnGestureListener {

        /**
         * View匹配器
         * <p>
         * 在查找事件发生的View时,只考虑匹配器匹配的View
         */
        private final ViewMatcher mViewMatcher;

        private OnGestureListenerForFindInTouch(@NonNull ViewMatcher viewMatcher) {
            this.mViewMatcher = viewMatcher;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(@NonNull MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {

        }

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        /**
         * 查找触摸事件坐标所在的View.
         * <p>
         * 优先考虑最后添加的深度更大的View. 也就是最后添加在视图上层的View.
         *
         * @param root  事件发生的根视图
         * @param event 事件
         * @return 事件发生的View. 如果找不到,返回null
         */
        @Nullable
        protected View findViewInTouch(@NonNull View root, @NonNull MotionEvent event) {
            View farthestChild = null;
            View nearestChild = null;
            if (root instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) root;
                for (int i = viewGroup.getChildCount() - 1; i >= 0; i -= 1) {
                    final View child = viewGroup.getChildAt(i);
                    if (isViewInTouch(child, event)) {
                        if (child instanceof ViewGroup) {
                            farthestChild = findViewInTouch(child, event);
                            if (farthestChild != null) {
                                break;
                            }
                        } else {
                            if (nearestChild == null && mViewMatcher.match(child) && isViewInTouch(child, event)) {
                                nearestChild = child;
                            }
                        }
                    }
                }
                if (farthestChild != null) {
                    return farthestChild;
                } else if (nearestChild != null) {
                    return nearestChild;
                } else {
                    return mViewMatcher.match(root) && isViewInTouch(root, event) ? root : null;
                }
            } else {
                return mViewMatcher.match(root) && isViewInTouch(root, event) ? root : null;
            }
        }

        /**
         * 判断一个事件是否在View的可视范围内
         * <p>
         * 判断时会考虑View的translation 和 scale,<b>但不会考虑View的rotation</b>
         *
         * @param view  view
         * @param event 事件
         * @return 如果事件在View的可视范围内, 返回true;否则返回false
         */
        private boolean isViewInTouch(@NonNull View view, @NonNull MotionEvent event) {
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            float left = location[0];
            float top = location[1];
            float right = left + view.getWidth();
            float bottom = top + view.getHeight();
            return event.getRawX() >= left && event.getRawX() <= right && event.getRawY() >= top && event.getRawY() <= bottom;
        }

    }

    /**
     * 处理单击事件的手势监听器
     * <p>
     * 除了根视图外,只考虑 clickable = true 的View
     */
    private static class OnGestureListenerForSingleTapUp
            extends OnGestureListenerForFindInTouch {

        @NonNull
        private final RecyclerView mRecyclerview;

        @NonNull
        private final OnItemClickListener mListener;

        private OnGestureListenerForSingleTapUp(@NonNull RecyclerView recyclerview, @NonNull OnItemClickListener listener) {
            super(View::isClickable);
            this.mRecyclerview = recyclerview;
            this.mListener = listener;
        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            final View itemView = mRecyclerview.findChildViewUnder(e.getX(), e.getY());
            if (itemView != null) {
                final View inTouchView = findViewInTouch(itemView, e);
                final int position = mRecyclerview.getChildAdapterPosition(itemView);
                mListener.onItemClick(mRecyclerview, position, inTouchView == null ? itemView : inTouchView);
            }
            // 不要消费事件.否则可能会影响其他的Listener.
            // 如果消费了事件,其他的Listener可能只能收到DOWN事件, 收不到UP事件, 这会导致其他的Listener错误的认为发生了长按事件
            return false;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {
            super.onLongPress(e);
        }
    }

    /**
     * 处理长按事件的手势监听器
     * <p>
     * 除了根视图外,只考虑 longClickable = true 的View
     */
    private static class OnGestureListenerForLongPress
            extends OnGestureListenerForFindInTouch {

        @NonNull
        private final RecyclerView mRecyclerview;

        @NonNull
        private final OnItemLongClickListener mListener;


        private OnGestureListenerForLongPress(@NonNull RecyclerView recyclerview, @NonNull OnItemLongClickListener listener) {
            super(View::isLongClickable);
            this.mRecyclerview = recyclerview;
            this.mListener = listener;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {
            final View itemView = mRecyclerview.findChildViewUnder(e.getX(), e.getY());
            if (itemView != null) {
                final View inTouchView = findViewInTouch(itemView, e);
                final int position = mRecyclerview.getChildAdapterPosition(itemView);
                mListener.onItemLongClick(mRecyclerview, position, inTouchView == null ? itemView : inTouchView);
            }
        }
    }

    /**
     * View匹配器
     */
    private interface ViewMatcher {

        boolean match(@NonNull View view);

    }

    /**
     * 点击事件监听器
     */
    public interface OnItemClickListener {

        /**
         * 发生点击事件时回调
         *
         * @param recyclerView 事件发生在哪一个RecyclerView上
         * @param position     事件发生的条目位置
         * @param view         事件发生的View. 只有{@link View#isClickable()} = true 的View会被考虑.如果找不到符合条件的View, 那么这个View将会是条目根视图
         */
        void onItemClick(@NonNull RecyclerView recyclerView, int position, @NonNull View view);

    }

    /**
     * 长按事件监听器
     */
    public interface OnItemLongClickListener {

        /**
         * 发生长按事件时回调
         *
         * @param recyclerView 事件发生在哪一个RecyclerView上
         * @param position     事件发生的条目位置
         * @param view         事件发生的View. 只有{@link View#isLongClickable()} = true 的View会被考虑.如果找不到符合条件的View, 那么这个View将会是条目根视图
         */
        void onItemLongClick(@NonNull RecyclerView recyclerView, int position, @NonNull View view);

    }

}
