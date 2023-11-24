package com.z.arc.wheel2d;

import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Wheel滚动辅助器,在停止滚动时调整列表中部对齐
 * <p>
 * Created by Blate on 2023/8/16
 */
public class Wheel2DSnapHelper extends LinearSnapHelper {

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        if (layoutManager instanceof Wheel2DLayoutManager) {
            if (((Wheel2DLayoutManager) layoutManager).getScrollFeature() == Wheel2DLayoutManager.SCROLL_FEATURE_LIMITED) {
                return super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
            } else {
                // Wheel2DLayoutManager 在循环滚动下,向量的计算会根据最近滚动距离计算.
                // 使用RecyclerView.SmoothScroller.ScrollVectorProvider获取到的值不能用于SnapHelper
                return RecyclerView.NO_POSITION;
            }
        } else {
            return super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
        }
    }

}
