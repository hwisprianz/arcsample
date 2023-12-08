package com.z.arc.media.bean;

import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * <p>
 * <p>
 * Created by Blate on 2023/12/4
 */
public class MediaBucketBean {

    @Nullable
    public final Long id;

    @Nullable
    public String displayName;

    @Nullable
    public Uri cover = null;

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
