package com.z.arc.media.bean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

/**
 * <p>
 * <p>
 * Created by Blate on 2023/12/6
 */
public interface IMediaList<M extends IMedia> {


    HashMap<String, String> getBucketIds();

    @Nullable
    M get(int index);

    void fill(int index, @NonNull M container);

    int getCount();

    void close();

}
