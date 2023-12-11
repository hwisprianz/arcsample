package com.z.arc.media.repo;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.z.arc.media.MediaConstance;
import com.z.arc.media.MediaManager;
import com.z.arc.media.bean.EmptyMediaList;
import com.z.arc.media.bean.IMediaList;
import com.z.arc.media.bean.MediaBean;
import com.z.arc.media.bean.MediaBucketBean;
import com.z.arc.media.bean.MediaList;
import com.z.arc.media.bean.MultipleMediaList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 媒体仓库
 * <p>
 * Created by Blate on 2023/12/4
 */
public class MediaRepository {

    private static final String[] PROJECTION_BUCKET = new String[]{
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
    };

    private static final String[] PROJECTION_BUCKET_DETAIL = new String[]{
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.DATE_MODIFIED
    };

    private static final String[] PROJECTION_MEDIA = new String[]{
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.TITLE,
            MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.DATE_MODIFIED
    };

    @NonNull
    private final ContentResolver mContentResolver;

    @NonNull
    private final CancellationSignal mCancellationSignal = new CancellationSignal();

    /**
     * 需要引入的媒体位置
     *
     * <li>{@link MediaConstance.Include#INCLUDE_INTERNAL_IMAGE} 引入 {@link MediaStore.Images.Media#INTERNAL_CONTENT_URI}</li>
     * <li>{@link MediaConstance.Include#INCLUDE_EXTERNAL_IMAGE} 引入 {@link MediaStore.Images.Media#EXTERNAL_CONTENT_URI}</li>
     * <li>{@link MediaConstance.Include#INCLUDE_INTERNAL_VIDEO} 引入 {@link MediaStore.Video.Media#INTERNAL_CONTENT_URI}</li>
     * <li>{@link MediaConstance.Include#INCLUDE_EXTERNAL_VIDEO} 引入 {@link MediaStore.Video.Media#EXTERNAL_CONTENT_URI}</li>
     * <li>{@link MediaConstance.Include#INCLUDE_ALL} 引入所有</li>
     * <p>
     * 可以使用 | 引入任意组合
     *
     * @see MediaConstance.Include
     */
    @MediaConstance.IncludeDef
    private final int mInclude;

    /**
     * 排序方式
     *
     * <li>{@link MediaConstance.Sort#SORT_ASCENDING} 按照时间升序</li>
     * <li>{@link MediaConstance.Sort#SORT_DESCENDING} 按照时间降序</li>
     *
     * <b>排序只针对媒体资源不针对桶. 桶默认按照显示名称字典序</b>
     */
    @MediaConstance.SortDef
    private final int mSort;

    public MediaRepository(@NonNull ContentResolver contentResolver,
                           @MediaConstance.IncludeDef int include,
                           @MediaConstance.SortDef int sort) {
        this.mContentResolver = contentResolver;
        this.mInclude = include;
        this.mSort = sort;
    }

    /**
     * 查询所有的桶
     * <p>
     * 只查询桶的基本信息(id 和 显示名称); 详细信息使用 {@link #queryBucketDetail(Long)}查询
     *
     * @return 桶列表
     */
    @NonNull
    @WorkerThread
    public final List<MediaBucketBean> queryBuckets() {
        final Set<MediaBucketBean> bucketSet = new HashSet<>();

        if ((mInclude & MediaConstance.Include.INCLUDE_INTERNAL_IMAGE) != 0) {
            bucketSet.addAll(queryBuckets(MediaStore.Images.Media.INTERNAL_CONTENT_URI));
        }
        if ((mInclude & MediaConstance.Include.INCLUDE_EXTERNAL_IMAGE) != 0) {
            bucketSet.addAll(queryBuckets(MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        }
        if ((mInclude & MediaConstance.Include.INCLUDE_INTERNAL_VIDEO) != 0) {
            bucketSet.addAll(queryBuckets(MediaStore.Video.Media.INTERNAL_CONTENT_URI));
        }
        if ((mInclude & MediaConstance.Include.INCLUDE_EXTERNAL_VIDEO) != 0) {
            bucketSet.addAll(queryBuckets(MediaStore.Video.Media.EXTERNAL_CONTENT_URI));
        }

        final List<MediaBucketBean> bucketList = new ArrayList<>(bucketSet);
        Collections.sort(bucketList, MediaManager.createMediaBucketComparator());

        return bucketList;
    }

    /**
     * 查询指定桶的详细信息
     * <p>
     * 桶的详细信息包含桶中的媒体数量,桶的封面
     *
     * @param bucketId 桶id
     * @return 桶详细信息
     */
    @WorkerThread
    @NonNull
    public final MediaBucketBean queryBucketDetail(@Nullable Long bucketId) {
        final MediaBucketBean bucket = new MediaBucketBean(bucketId);
        final List<MediaBean> coverList = new ArrayList<>();

        if ((mInclude & MediaConstance.Include.INCLUDE_INTERNAL_IMAGE) != 0) {
            fillMediaBucketDetailFromUri(MediaStore.Images.Media.INTERNAL_CONTENT_URI, bucketId, bucket, coverList);
        }
        if ((mInclude & MediaConstance.Include.INCLUDE_EXTERNAL_IMAGE) != 0) {
            fillMediaBucketDetailFromUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, bucketId, bucket, coverList);
        }
        if ((mInclude & MediaConstance.Include.INCLUDE_INTERNAL_VIDEO) != 0) {
            fillMediaBucketDetailFromUri(MediaStore.Video.Media.INTERNAL_CONTENT_URI, bucketId, bucket, coverList);
        }
        if ((mInclude & MediaConstance.Include.INCLUDE_EXTERNAL_VIDEO) != 0) {
            fillMediaBucketDetailFromUri(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, bucketId, bucket, coverList);
        }
        if (!coverList.isEmpty()) {
            Collections.sort(coverList, MediaManager.createMediaComparator(mSort));
            bucket.cover = coverList.get(0).uri;
        }

        return bucket;
    }

    /**
     * 查询指定桶的媒体列表
     *
     * @param bucketId 桶id;如果指定的id为null, 则会查询所有桶
     * @return 桶媒体列表
     */
    @WorkerThread
    @NonNull
    public final IMediaList queryMediaList(@Nullable Long bucketId) {
        final List<IMediaList> mediaLists = new ArrayList<>();
        if ((mInclude & MediaConstance.Include.INCLUDE_INTERNAL_IMAGE) != 0) {
            final IMediaList mediaList = queryMediaList(MediaStore.Images.Media.INTERNAL_CONTENT_URI, bucketId);
            if (!mediaList.isEmpty()) {
                mediaLists.add(queryMediaList(MediaStore.Images.Media.INTERNAL_CONTENT_URI, bucketId));
            }
        }
        if ((mInclude & MediaConstance.Include.INCLUDE_EXTERNAL_IMAGE) != 0) {
            final IMediaList mediaList = queryMediaList(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, bucketId);
            if (!mediaList.isEmpty()) {
                mediaLists.add(queryMediaList(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, bucketId));
            }
        }
        if ((mInclude & MediaConstance.Include.INCLUDE_INTERNAL_VIDEO) != 0) {
            final IMediaList mediaList = queryMediaList(MediaStore.Video.Media.INTERNAL_CONTENT_URI, bucketId);
            if (!mediaList.isEmpty()) {
                mediaLists.add(queryMediaList(MediaStore.Video.Media.INTERNAL_CONTENT_URI, bucketId));
            }
        }
        if ((mInclude & MediaConstance.Include.INCLUDE_EXTERNAL_VIDEO) != 0) {
            final IMediaList mediaList = queryMediaList(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, bucketId);
            if (!mediaList.isEmpty()) {
                mediaLists.add(queryMediaList(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, bucketId));
            }
        }
        if (mediaLists.isEmpty()) {
            return new EmptyMediaList();
        } else if (mediaLists.size() == 1) {
            return mediaLists.get(0);
        } else {
            // merge list
            return new MultipleMediaList(mediaLists.toArray(new IMediaList[0]), mSort);
        }
    }

    @WorkerThread
    private void fillMediaBucketDetailFromUri(@NonNull Uri uri,
                                              @Nullable Long bucketId,
                                              @NonNull MediaBucketBean bucketBean,
                                              @NonNull List<MediaBean> covers) {
        final Cursor cursor = queryBucketDetail(uri, bucketId);
        if (cursor == null) {
            return;
        }
        if (cursor.moveToNext()) {
            bucketBean.cover = MediaManager.createFromCursor(cursor, uri).uri;
            if (TextUtils.isEmpty(bucketBean.displayName)) {
                final int bucketDisplayNameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME);
                if (bucketDisplayNameIndex != -1) {
                    bucketBean.displayName = cursor.getString(bucketDisplayNameIndex);
                }
            }
            covers.add(MediaManager.createFromCursor(cursor, uri));
        }
        if (bucketBean.count == null) {
            bucketBean.count = cursor.getCount();
        } else {
            bucketBean.count += cursor.getCount();
        }
        cursor.close();
    }

    @NonNull
    @WorkerThread
    private List<MediaBucketBean> queryBuckets(@NonNull Uri uri) {
        final List<MediaBucketBean> bucketBeans = new ArrayList<>();

        @Nullable final Cursor cursor = mContentResolver.query(
                uri.buildUpon().appendQueryParameter("distinct", "ture").build(),
                PROJECTION_BUCKET,
                null,
                null,
                null,
                mCancellationSignal);

        if (cursor == null) {
            return bucketBeans;
        }

        final int bucketIdIndex = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_ID);
        if (bucketIdIndex == -1) {
            return bucketBeans;
        }

        final int bucketDisplayNameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME);
        if (bucketDisplayNameIndex == -1) {
            return bucketBeans;
        }

        while (cursor.moveToNext()) {
            final long id = cursor.getLong(bucketIdIndex);
            final String displayName = cursor.getString(bucketDisplayNameIndex);

            final MediaBucketBean bucket = new MediaBucketBean(id, displayName);

            bucketBeans.add(bucket);
        }
        cursor.close();

        return bucketBeans;
    }

    @Nullable
    private Cursor queryBucketDetail(@NonNull Uri uri, @Nullable Long bucketId) {
        return mContentResolver.query(
                uri,
                PROJECTION_BUCKET_DETAIL,
                bucketId == null ? null : MediaStore.MediaColumns.BUCKET_ID + " = ?",
                bucketId == null ? null : new String[]{String.valueOf(bucketId)},
                sortOrder(),
                mCancellationSignal);
    }

    @NonNull
    private IMediaList queryMediaList(@NonNull Uri uri, @Nullable Long bucketId) {
        Cursor cursor = mContentResolver.query(uri,
                PROJECTION_MEDIA,
                bucketId == null ? null : MediaStore.MediaColumns.BUCKET_ID + " = ?",
                bucketId == null ? null : new String[]{String.valueOf(bucketId)},
                sortOrder(),
                mCancellationSignal);
        return new MediaList(uri, cursor);

    }

    /**
     * This provides a default sorting order string for subclasses.
     * The list is first sorted by date, then by id. The order can be ascending
     * or descending, depending on the mSort variable.
     * The date is obtained from the "date_taken" column. But if it is null,
     * the "date_modified" column is used instead.
     *
     * @return the sorting order string, used to query order
     */
    private String sortOrder() {
        String ascending =
                (mSort == MediaConstance.Sort.SORT_ASCENDING)
                        ? "ASC"
                        : "DESC";

        // Use DATE_TAKEN if it's non-null, otherwise use DATE_MODIFIED.
        // DATE_TAKEN is in milliseconds, but DATE_MODIFIED is in seconds.
        // Add id to the end so that we don't ever get random sorting
        // which could happen, I suppose, if the date values are the same.
        return String.format("case ifnull(%s,0) when 0 then %s*1000 else %s end %s, %s %s",
                MediaStore.MediaColumns.DATE_TAKEN,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.DATE_TAKEN,
                ascending,
                MediaStore.MediaColumns._ID,
                ascending);
    }

}
