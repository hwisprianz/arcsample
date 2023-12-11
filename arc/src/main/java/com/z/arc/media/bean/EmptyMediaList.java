package com.z.arc.media.bean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 空的媒体列表
 * <p>
 * Created by Blate on 2023/12/11
 */
public class EmptyMediaList implements IMediaList {

    @Nullable
    @Override
    public MediaBean get(int index) {
        throw new UnsupportedOperationException("EmptyMediaList can not get");
    }

    @Override
    public void fill(int index, @NonNull MediaBean container) {
        throw new UnsupportedOperationException("EmptyMediaList can not fill");
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void close() {

    }

}
