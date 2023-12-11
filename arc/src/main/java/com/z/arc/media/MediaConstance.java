package com.z.arc.media;

import androidx.annotation.IntDef;

/**
 * 媒体常量定义
 * <p>
 * Created by Blate on 2023/12/8
 */
public interface MediaConstance {

    /**
     * 媒体位置定义
     * <p>
     * 使用 | 任意组合位置
     */
    interface Include {

        /**
         * 内部图片
         * <p>
         * {@link android.provider.MediaStore.Images.Media#INTERNAL_CONTENT_URI}
         */
        int INCLUDE_INTERNAL_IMAGE = 1;

        /**
         * 外部图片
         * <p>
         * {@link android.provider.MediaStore.Images.Media#EXTERNAL_CONTENT_URI}
         */
        int INCLUDE_EXTERNAL_IMAGE = 1 << 1;

        /**
         * 内部视频
         * <p>
         * {@link android.provider.MediaStore.Video.Media#INTERNAL_CONTENT_URI}
         */
        int INCLUDE_INTERNAL_VIDEO = 1 << 2;

        /**
         * 外部视频
         * <p>
         * {@link android.provider.MediaStore.Video.Media#EXTERNAL_CONTENT_URI}
         */
        int INCLUDE_EXTERNAL_VIDEO = 1 << 3;

        /**
         * 所有位置的图片和媒体
         */
        int INCLUDE_ALL = INCLUDE_INTERNAL_IMAGE | INCLUDE_EXTERNAL_IMAGE | INCLUDE_INTERNAL_VIDEO | INCLUDE_EXTERNAL_VIDEO;

    }

    /**
     * 媒体位置定义注解
     */
    @IntDef(flag = true, value = {
            Include.INCLUDE_INTERNAL_IMAGE,
            Include.INCLUDE_EXTERNAL_IMAGE,
            Include.INCLUDE_INTERNAL_VIDEO,
            Include.INCLUDE_EXTERNAL_VIDEO,
    })
    @interface IncludeDef {
    }

    /**
     * 媒体排序定义
     */
    interface Sort {

        /**
         * 升序
         */
        int SORT_ASCENDING = 1;

        /**
         * 降序
         */
        int SORT_DESCENDING = 2;

    }

    /**
     * 媒体排序定义注解
     */
    @IntDef(value = {
            Sort.SORT_ASCENDING,
            Sort.SORT_DESCENDING,
    })
    @interface SortDef {
    }

}
