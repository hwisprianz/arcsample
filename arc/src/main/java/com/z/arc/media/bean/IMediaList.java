package com.z.arc.media.bean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <p>
 * <p>
 * Created by Blate on 2023/12/6
 */
public interface IMediaList {

    @Nullable
    MediaBean get(int index);

    void fill(int index, @NonNull MediaBean container);

    int getCount();

    boolean isEmpty();

    void close();

}
