package com.z.arc.recyclerview.decoration;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 条目间分割线装饰器
 * <p>
 * 条目间根据分割线尺寸预留空间,在条目绘制之后绘制分割线.
 * <p>
 * <li>交叉轴的分割线是针对recyclerview贯穿的,按照一行/一列绘制(将会在行/列开始时考虑是否绘制交叉轴分割线).受设置的分割线开始结束边距和recyclerview的padding影响</li>
 * <li>主轴的分割线是针对条目的(并非贯穿的),按照条目绘制.受设置的分割线开始结束边距影响</li>
 * <p>
 * 所有的分割线都是在条目绘制后考虑是否绘制,例如:
 * <li>在绘制交叉轴第一行/列时不考虑主轴分割线,绘制交叉轴第二行/列时考虑是否绘制一条当前条目尺寸的主轴分割线</li>
 * <li>绘制主轴第一行/列时,不考虑交叉轴分割线.绘制主轴第二行/列时(行/列开始的第一个条目),考虑是否需要绘制一条贯穿的分割线</li>
 * Created by Blate on 2023/6/20
 */
@SuppressWarnings("unused")
public class ItemDividerDecoration extends ItemSpacingDecoration {

    /**
     * 主轴分割线开始边距
     */
    @Px
    private final int mMainAxisDividerMarginStart;

    /**
     * 主轴分割线结束边距
     */
    @Px
    private final int mMainAxisDividerMarginEnd;

    /**
     * 交叉轴分割线开始边距
     */
    @Px
    private final int mCrossAxisDividerMarginStart;

    /**
     * 交叉轴分割线结束边距
     */
    @Px
    private final int mCrossAxisDividerMarginEnd;

    /**
     * 主轴分割线
     */
    @Nullable
    private final Drawable mMainAxisDivider;

    /**
     * 交叉轴分割线
     */
    @Nullable
    private final Drawable mCrossAxisDivider;

    /**
     * 完成的构造方法.可以独立设置主轴交叉轴的分割线尺寸,边距,分割线
     *
     * @param mainAxisDividerSize         主轴分割线尺寸
     * @param crossAxisDividerSize        交叉轴分割线尺寸
     * @param mainAxisDividerMarginStart  主轴分割线开始边距
     * @param mainAxisDividerMarginEnd    主轴分割线结束边距
     * @param crossAxisDividerMarginStart 交叉轴分割线开始边距
     * @param crossAxisDividerMarginEnd   交叉轴分割线结束边距
     * @param mainAxisDivider             主轴分割线
     * @param crossAxisDivider            交叉轴分割线
     */
    public ItemDividerDecoration(@Px int mainAxisDividerSize,
                                 @Px int crossAxisDividerSize,
                                 @Px int mainAxisDividerMarginStart,
                                 @Px int mainAxisDividerMarginEnd,
                                 @Px int crossAxisDividerMarginStart,
                                 @Px int crossAxisDividerMarginEnd,
                                 @Nullable Drawable mainAxisDivider,
                                 @Nullable Drawable crossAxisDivider) {
        super(mainAxisDividerSize, crossAxisDividerSize);
        this.mMainAxisDividerMarginStart = mainAxisDividerMarginStart;
        this.mMainAxisDividerMarginEnd = mainAxisDividerMarginEnd;
        this.mCrossAxisDividerMarginStart = crossAxisDividerMarginStart;
        this.mCrossAxisDividerMarginEnd = crossAxisDividerMarginEnd;
        this.mMainAxisDivider = mainAxisDivider;
        this.mCrossAxisDivider = crossAxisDivider;

    }

    /**
     * 交叉轴分割线和主轴分割线尺寸相同,边距相同,分割线相同
     *
     * @param dividerSize   分割线尺寸
     * @param dividerMargin 分割线边距
     * @param divider       分割线
     */
    public ItemDividerDecoration(@Px int dividerSize,
                                 @Px int dividerMargin,
                                 @Nullable Drawable divider) {
        this(dividerSize, dividerSize, dividerMargin, dividerMargin, dividerMargin, dividerMargin, divider, divider);
    }

    /**
     * 交叉轴分割线和主轴分割线尺寸相同,边距相同,分割线相同.指定颜色的纯色矩形分割线
     *
     * @param dividerSize   分割线尺寸
     * @param dividerMargin 分割线边距
     * @param dividerColor  分割线颜色
     */
    public ItemDividerDecoration(@Px int dividerSize,
                                 @Px int dividerMargin,
                                 @ColorInt int dividerColor) {
        this(dividerSize, dividerSize, dividerMargin, dividerMargin, dividerMargin, dividerMargin, new ColorDrawable(dividerColor), new ColorDrawable(dividerColor));
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        final RecyclerView.Adapter<?> adapter = parent.getAdapter();
        if (layoutManager instanceof GridLayoutManager && adapter != null) {
            onDrawOverForGrid(c, parent, (GridLayoutManager) layoutManager, adapter);
        } else if (layoutManager instanceof LinearLayoutManager && adapter != null) {
            onDrawOverForLinear(c, parent, (LinearLayoutManager) layoutManager, adapter);
        }
    }

    /**
     * 网格布局分割线
     *
     * @param c             画布
     * @param parent        RecyclerView
     * @param layoutManager 网格布局管理器
     * @param adapter       适配器
     */
    private void onDrawOverForGrid(@NonNull Canvas c,
                                   @NonNull RecyclerView parent,
                                   @NonNull GridLayoutManager layoutManager,
                                   @NonNull RecyclerView.Adapter<?> adapter) {
        if (mMainAxisDivider == null && mCrossAxisDivider == null) {
            return;
        }
        final int spaceCount = layoutManager.getSpanCount();
        final GridLayoutManager.SpanSizeLookup lookup = layoutManager.getSpanSizeLookup();
        int lastSpanGroupIndex = -1;
        for (int i = 0; i < parent.getChildCount(); i += 1) {
            final View child = parent.getChildAt(i);
            final int position = parent.getChildAdapterPosition(child);
            final int spanGroupIndex = lookup.getSpanGroupIndex(position, spaceCount);
            if (layoutManager.getOrientation() == RecyclerView.HORIZONTAL) {
                // 主轴分割线
                if (mMainAxisDivider != null && spanGroupIndex > 0 && spanGroupIndex != lastSpanGroupIndex) {
                    lastSpanGroupIndex = spanGroupIndex;
                    final int height = parent.getHeight() - parent.getPaddingTop() - parent.getPaddingBottom() - mMainAxisDividerMarginStart - mMainAxisDividerMarginEnd;
                    mMainAxisDivider.setBounds(0, 0, mMainAxisSpace, height);
                    c.save();
                    if (layoutManager.getReverseLayout()) {
                        c.translate(child.getRight(), parent.getTop() + parent.getPaddingTop() + mMainAxisDividerMarginStart);
                    } else {
                        c.translate(child.getLeft() - mMainAxisSpace, parent.getTop() + parent.getPaddingTop() + mMainAxisDividerMarginStart);
                    }
                    mMainAxisDivider.draw(c);
                    c.restore();
                }
                if (mCrossAxisDivider != null) {
                    // 交叉轴分割线
                    final int width = child.getWidth() - mCrossAxisDividerMarginStart - mCrossAxisDividerMarginEnd;
                    mCrossAxisDivider.setBounds(0, 0, width, mCrossAxisSpace);
                    c.save();
                    if (layoutManager.getReverseLayout()) {
                        c.translate(child.getRight() - width - mCrossAxisDividerMarginStart, child.getTop() - mCrossAxisSpace);
                    } else {
                        c.translate(child.getLeft() + mCrossAxisDividerMarginStart, child.getTop() - mCrossAxisSpace);
                    }
                    mCrossAxisDivider.draw(c);
                    c.restore();
                }
            } else if (layoutManager.getOrientation() == RecyclerView.VERTICAL) {
                // 主轴分割线
                if (mMainAxisDivider != null && spanGroupIndex > 0 && spanGroupIndex != lastSpanGroupIndex) {
                    lastSpanGroupIndex = spanGroupIndex;
                    final int width = parent.getWidth() - parent.getPaddingStart() - parent.getPaddingEnd() - mMainAxisDividerMarginStart - mMainAxisDividerMarginEnd;
                    mMainAxisDivider.setBounds(0, 0, width, mMainAxisSpace);
                    c.save();
                    if (layoutManager.getReverseLayout()) {
                        c.translate(parent.getLeft() + parent.getPaddingStart() + mMainAxisDividerMarginStart, child.getBottom());
                    } else {
                        c.translate(parent.getLeft() + parent.getPaddingStart() + mMainAxisDividerMarginStart, child.getTop() - mMainAxisSpace);
                    }
                    mMainAxisDivider.draw(c);
                    c.restore();
                }
                if (mCrossAxisDivider != null) {
                    // 交叉轴分割线
                    final int height = child.getHeight() - mCrossAxisDividerMarginStart - mCrossAxisDividerMarginEnd;
                    mCrossAxisDivider.setBounds(0, 0, mCrossAxisSpace, height);
                    c.save();
                    if (layoutManager.getReverseLayout()) {
                        c.translate(child.getLeft() - mCrossAxisSpace, child.getBottom() - height - mCrossAxisDividerMarginStart);
                    } else {
                        c.translate(child.getLeft() - mCrossAxisSpace, child.getTop() + mCrossAxisDividerMarginStart);
                    }
                    mCrossAxisDivider.draw(c);
                    c.restore();
                }
            }
        }


    }

    /**
     * 线性布局分割线
     *
     * @param c             画布
     * @param parent        RecyclerView
     * @param layoutManager 布局管理器
     * @param adapter       适配器
     */
    private void onDrawOverForLinear(@NonNull Canvas c,
                                     @NonNull RecyclerView parent,
                                     @NonNull LinearLayoutManager layoutManager,
                                     @NonNull RecyclerView.Adapter<?> adapter) {
        if (mMainAxisDivider == null) {
            return;
        }
        for (int i = 0; i < parent.getChildCount(); i += 1) {
            final View child = parent.getChildAt(i);
            final int position = parent.getChildAdapterPosition(child);
            final boolean isMainAxisEnd = position == adapter.getItemCount() - 1;
            if (isMainAxisEnd) {
                continue;
            }
            if (layoutManager.getOrientation() == RecyclerView.HORIZONTAL) {
                final int height = parent.getHeight() - mMainAxisDividerMarginStart - mMainAxisDividerMarginEnd;
                mMainAxisDivider.setBounds(0, 0, mMainAxisSpace, height);
                c.save();
                c.translate(child.getRight(), mMainAxisDividerMarginStart);
                mMainAxisDivider.draw(c);
                c.restore();
            } else if (layoutManager.getOrientation() == RecyclerView.VERTICAL) {
                final int width = parent.getWidth() - mMainAxisDividerMarginStart - mMainAxisDividerMarginEnd;
                mMainAxisDivider.setBounds(0, 0, width, mMainAxisSpace);
                c.save();
                c.translate(mMainAxisDividerMarginStart, child.getBottom());
                mMainAxisDivider.draw(c);
                c.restore();
            }
        }
    }


}
