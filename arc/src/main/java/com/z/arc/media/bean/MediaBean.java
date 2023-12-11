package com.z.arc.media.bean;

import android.net.Uri;

/**
 * 媒体
 * <p>
 * 图片或者视频
 * <p>
 * Created by Blate on 2023/12/4
 */
public class MediaBean {

    /**
     * 媒体数据库中的id
     */
    public long id;

    /**
     * 资源的uri
     */
    public Uri uri;

    /**
     * 资源的路径
     */
    public String data;

    /**
     * 桶id
     */
    public long bucketId;

    /**
     * 类型
     */
    public String mimeType;

    /**
     * 时常
     */
    public long duration;

    /**
     * 显示标题
     */
    public String title;

    /**
     * 拍摄/创建日期
     * <p>
     * timestamp in milliseconds
     */
    public long dateTaken;

    /**
     * 修改日期
     * <p>
     * timestamp in seconds
     */
    public long dateModified;

    public MediaBean() {
    }

}
