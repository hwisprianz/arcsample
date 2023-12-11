package com.z.arc.media.bean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <p>
 * <p>
 * Created by Blate on 2023/12/11
 */
public class EmptyMediaList implements IMediaList {

    @Nullable
    @Override
    public MediaBean get(int index) {
        return null;
    }

    @Override
    public void fill(int index, @NonNull MediaBean container) {

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
