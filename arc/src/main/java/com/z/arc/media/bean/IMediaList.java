package com.z.arc.media.bean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 媒体列表抽象
 * <p>
 * Created by Blate on 2023/12/6
 */
public interface IMediaList {

    /**
     * 获取指定位置的媒体
     * <p>
     * 索引外获取是否异常由实现类决定; 不保证每次获取同一个位置的媒体都是同一个对象
     *
     * @param index 索引
     * @return 媒体
     */
    @Nullable
    MediaBean get(int index);

    /**
     * 填充指定位置的媒体
     * <p>
     * 再桶内媒体很多时,用给定的对象进行填充可以减少GC压力
     *
     * @param index     索引
     * @param container 容器
     */
    void fill(int index, @NonNull MediaBean container);

    /**
     * 获取媒体数量
     *
     * @return 媒体数量
     */
    int getCount();

    /**
     * 是否为空
     *
     * @return 是否为空
     */
    boolean isEmpty();

    /**
     * 在不需要的时候关闭列表,释放资源
     */
    void close();

}
