package com.z.arc.media.bean;

import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * 媒体桶
 * <p>
 * Created by Blate on 2023/12/4
 */
public class MediaBucketBean {

    /**
     * 桶id
     * <p>
     * 如果没有id,则表示所有桶
     */
    @Nullable
    public final Long id;

    /**
     * 桶的显示名字
     */
    @Nullable
    public String displayName;

    /**
     * 桶的封面
     */
    @Nullable
    public Uri cover = null;

    /**
     * 桶内媒体数量
     */
    @Nullable
    public Integer count = null;

    public MediaBucketBean(@Nullable Long id, @Nullable String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public MediaBucketBean(@Nullable Long id) {
        this(id, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaBucketBean that = (MediaBucketBean) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
