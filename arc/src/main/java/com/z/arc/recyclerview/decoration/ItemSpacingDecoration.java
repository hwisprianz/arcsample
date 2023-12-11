package com.z.arc.recyclerview.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 条目间距装饰器.
 * <p>
 * 设置条目间距,提供交叉轴条目间距,主轴条目间距,主轴条目距离容器间距,<b>不提供交叉轴条目与容器边界的间距设置(这会导致条目不能等大)</b>.
 * <p>
 * <b>提供所有间距准确的保证.不提供条目尺寸在交叉轴准确的保证,涉及到无法整除的问题,但是保证条目尺寸的误差最多一个像素且对每个交叉轴的修正在相同的位置(累积修正)</b>
 * <p>
 * Created by Blate on 2023/6/19
 */
@SuppressWarnings("unused")
public class ItemSpacingDecoration
        extends RecyclerView.ItemDecoration {

    /**
     * 主轴条目间距
     */
    @Px
    protected final int mMainAxisSpace;

    /**
     * 交叉轴条目间距
     */
    @Px
    protected final int mCrossAxisSpace;

    /**
     * 主轴开始间距
     */
    @Px
    private final int mStartSpace;

    /**
     * 主轴结束间距
     */
    @Px
    private final int mEndSpace;

    /**
     * 完整的构造方法,设置主轴条目间距,交叉轴条目间距,条目在主轴与容器开始位置的间距,条目在主轴与容器结束位置的间距
     *
     * @param mainAxisSpace  主轴条目间距
     * @param crossAxisSpace 交叉轴条目间距
     * @param startSpace     开始间距
     * @param endSpace       结束间距
     */
    public ItemSpacingDecoration(@Px int mainAxisSpace, @Px int crossAxisSpace, @Px int startSpace, @Px int endSpace) {
        this.mMainAxisSpace = mainAxisSpace;
        this.mCrossAxisSpace = crossAxisSpace;
        this.mStartSpace = startSpace;
        this.mEndSpace = endSpace;

    }

    /**
     * 分别设置主轴和交叉轴条目间距
     *
     * @param mainAxisSpace  主轴间距
     * @param crossAxisSpace 交叉轴间距
     */
    public ItemSpacingDecoration(@Px int mainAxisSpace, @Px int crossAxisSpace) {
        this(mainAxisSpace, crossAxisSpace, 0, 0);
    }

    /**
     * 进设置条目间间距,主轴和交叉轴条目间的间距相同
     *
     * @param space 间距
     */
    public ItemSpacingDecoration(@Px int space) {
        this(space, space, 0, 0);
    }


    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        final RecyclerView.Adapter<?> adapter = parent.getAdapter();
        if (layoutManager instanceof GridLayoutManager && adapter != null) {
            getItemOffsetForGrid((GridLayoutManager) layoutManager, adapter, outRect, view, parent);
        } else if (layoutManager instanceof LinearLayoutManager && adapter != null) {
            getItemOffsetForLinear((LinearLayoutManager) layoutManager, adapter, outRect, view, parent);
        }
    }

    /**
     * 获取网格布局条目间距
     *
     * @param layoutManager 布局管理器
     * @param adapter       适配器
     * @param outRect       间距矩形
     * @param view          条目
     * @param parent        容器
     */
    public void getItemOffsetForGrid(@NonNull GridLayoutManager layoutManager,
                                     @NonNull RecyclerView.Adapter<?> adapter,
                                     @NonNull Rect outRect,
                                     @NonNull View view,
                                     @NonNull RecyclerView parent) {
        final int spaceCount = layoutManager.getSpanCount();
        final GridLayoutManager.SpanSizeLookup lookup = layoutManager.getSpanSizeLookup();
        final int position = parent.getChildAdapterPosition(view);

        final int spanGroupIndex = lookup.getSpanGroupIndex(position, spaceCount);
        final boolean isMainAxisStart = spanGroupIndex == 0;
        final boolean isMainAxisEnd = spanGroupIndex == lookup.getSpanGroupIndex(adapter.getItemCount() - 1, spaceCount);
        final int mainStart = isMainAxisStart ? mStartSpace : mMainAxisSpace / 2;
        final int mainEnd = isMainAxisEnd ? mEndSpace : mMainAxisSpace - mMainAxisSpace / 2;

        // 交叉轴间距对于不同的space是等差数列,计算首项和公差
        final double first = (spaceCount - 1) * mCrossAxisSpace / 1.0f / spaceCount;
        final double tail = 0;
        final double commonDifference = spaceCount <= 1 ? 0 : (tail - first) / 1.0f / (spaceCount - 1);

        // 计算交叉轴每个条目结束位置的间距,同一个位置的条目间距相同. 条目开始位置的间距等于总的间距减去上一个条目结束位置的间距
        final int spanIndex = lookup.getSpanIndex(position, spaceCount);
        final int start = (spanIndex == 0 || position == 0) ? 0 : mCrossAxisSpace - (int) Math.floor(first + commonDifference * (lookup.getSpanIndex(position - 1, spaceCount) + lookup.getSpanSize(position - 1) - 1));
        final int end = (int) Math.floor(first + commonDifference * (spanIndex + lookup.getSpanSize(position) - 1));

        if (layoutManager.getOrientation() == RecyclerView.HORIZONTAL) {
            outRect.left = layoutManager.getReverseLayout() ? mainEnd : mainStart;
            outRect.top = start;
            outRect.right = layoutManager.getReverseLayout() ? mainStart : mainEnd;
            outRect.bottom = end;
        } else if (layoutManager.getOrientation() == RecyclerView.VERTICAL) {
            outRect.left = start;
            outRect.top = layoutManager.getReverseLayout() ? mainEnd : mainStart;
            outRect.right = end;
            outRect.bottom = layoutManager.getReverseLayout() ? mainStart : mainEnd;
        }
    }

    /**
     * 获取线性布局条目间距
     *
     * @param layoutManager 布局管理器
     * @param adapter       适配器
     * @param outRect       间距矩形
     * @param view          条目
     * @param parent        容器
     */
    public void getItemOffsetForLinear(@NonNull LinearLayoutManager layoutManager,
                                       @NonNull RecyclerView.Adapter<?> adapter,
                                       @NonNull Rect outRect,
                                       @NonNull View view,
                                       @NonNull RecyclerView parent) {
        final int position = parent.getChildAdapterPosition(view);
        final boolean isMainAxisStart = position == 0;
        final boolean isMainAxisEnd = position == adapter.getItemCount() - 1;
        final int mainStart = isMainAxisStart ? mStartSpace : mMainAxisSpace / 2;
        final int mainEnd = isMainAxisEnd ? mEndSpace : mMainAxisSpace - mMainAxisSpace / 2;
        if (layoutManager.getOrientation() == RecyclerView.HORIZONTAL) {
            outRect.left = mainStart;
            outRect.top = 0;
            outRect.right = mainEnd;
            outRect.bottom = 0;
        } else if (layoutManager.getOrientation() == RecyclerView.VERTICAL) {
            outRect.left = 0;
            outRect.top = mainStart;
            outRect.right = 0;
            outRect.bottom = mainEnd;
        }
    }

}
