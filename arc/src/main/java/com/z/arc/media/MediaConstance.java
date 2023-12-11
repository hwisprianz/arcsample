package com.z.arc.media;

import androidx.annotation.IntDef;

/**
 * <p>
 * <p>
 * Created by Blate on 2023/12/8
 */
public interface MediaConstance {

    interface Include {
        int INCLUDE_INTERNAL_IMAGE = 1;

        int INCLUDE_EXTERNAL_IMAGE = 1 << 1;

        int INCLUDE_INTERNAL_VIDEO = 1 << 2;

        int INCLUDE_EXTERNAL_VIDEO = 1 << 3;

        int INCLUDE_ALL = INCLUDE_INTERNAL_IMAGE | INCLUDE_EXTERNAL_IMAGE | INCLUDE_INTERNAL_VIDEO | INCLUDE_EXTERNAL_VIDEO;

    }

    @IntDef(flag = true, value = {
            Include.INCLUDE_INTERNAL_IMAGE,
            Include.INCLUDE_EXTERNAL_IMAGE,
            Include.INCLUDE_INTERNAL_VIDEO,
            Include.INCLUDE_EXTERNAL_VIDEO,
    })
    @interface IncludeDef {
    }

    interface Sort {
        int SORT_ASCENDING = 1;

        int SORT_DESCENDING = 2;

    }

    @IntDef(value = {
            Sort.SORT_ASCENDING,
            Sort.SORT_DESCENDING,
    })
    @interface SortDef {
    }

}
