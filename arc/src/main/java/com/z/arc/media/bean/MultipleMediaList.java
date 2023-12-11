package com.z.arc.media.bean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.z.arc.media.MediaConstance;

/**
 * 多个媒体列表的组合列表
 * <p>
 * Created by Blate on 2023/12/8
 */
public class MultipleMediaList implements IMediaList {

    /**
     * 子列表
     */
    private final IMediaList[] mMediaLists;

    public MultipleMediaList(@NonNull IMediaList[] mediaLists, @MediaConstance.SortDef int sort) {
        mMediaLists = mediaLists.clone();
    }

    @Nullable
    @Override
    public MediaBean get(int index) {
        final MediaBean mediaBean = new MediaBean();
        fill(index, mediaBean);
        return mediaBean;
    }

    @Override
    public void fill(int index, @NonNull MediaBean container) {
        // TODO: 2023/12/11 考虑多个子列表的排序,应该使用一个低时间成本的综合排序实现
        int skip = 0;
        for (IMediaList mediaList : mMediaLists) {
            if (index < skip + mediaList.getCount()) {
                mediaList.fill(index - skip, container);
                return;
            }
            skip += mediaList.getCount();
        }
    }

    @Override
    public int getCount() {
        int count = 0;
        for (IMediaList mediaList : mMediaLists) {
            count += mediaList.getCount();
        }
        return count;
    }

    @Override
    public boolean isEmpty() {
        for (IMediaList mediaList : mMediaLists) {
            if (!mediaList.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void close() {
        for (IMediaList mediaList : mMediaLists) {
            mediaList.close();
        }
    }

}
