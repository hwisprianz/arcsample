package com.z.arc.media.repo;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.z.arc.media.MediaManager;
import com.z.arc.media.bean.MediaBean;
import com.z.arc.media.bean.MediaBucketBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * <p>
 * Created by Blate on 2023/12/4
 */
public class MediaRepository {

    public interface IncludeDef {
        int INCLUDE_INTERNAL_IMAGE = 1;

        int INCLUDE_EXTERNAL_IMAGE = 1 << 1;

        int INCLUDE_INTERNAL_VIDEO = 1 << 2;

        int INCLUDE_EXTERNAL_VIDEO = 1 << 3;

        int INCLUDE_ALL = INCLUDE_INTERNAL_IMAGE | INCLUDE_EXTERNAL_IMAGE | INCLUDE_INTERNAL_VIDEO | INCLUDE_EXTERNAL_VIDEO;

    }

    @IntDef(flag = true, value = {
            IncludeDef.INCLUDE_INTERNAL_IMAGE,
            IncludeDef.INCLUDE_EXTERNAL_IMAGE,
            IncludeDef.INCLUDE_INTERNAL_VIDEO,
            IncludeDef.INCLUDE_EXTERNAL_VIDEO,
    })
    public @interface IncludeAnno {
    }

    public interface SortDef {
        int SORT_ASCENDING = 1;

        int SORT_DESCENDING = 2;

    }

    @IntDef(value = {
            SortDef.SORT_ASCENDING,
            SortDef.SORT_DESCENDING,
    })
    public @interface SortAnno {
    }

    private static final String[] PROJECTION_BUCKET = new String[]{
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
    };

    private static final String[] PROJECTION_BUCKET_DETAIL = new String[]{
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.DATE_MODIFIED
    };

    @NonNull
    private final ContentResolver mContentResolver;

    @NonNull
    private final CancellationSignal mCancellationSignal = new CancellationSignal();

    @IncludeAnno
    private final int mInclude;

    @SortAnno
    private final int mSort;

    public MediaRepository(@NonNull ContentResolver contentResolver, @IncludeAnno int include, @SortAnno int sort) {
        this.mContentResolver = contentResolver;
        this.mInclude = include;
        this.mSort = sort;
    }

    @NonNull
    @WorkerThread
    public final List<MediaBucketBean> queryBuckets() {
        final Set<MediaBucketBean> bucketSet = new HashSet<>();

        if ((mInclude & IncludeDef.INCLUDE_INTERNAL_IMAGE) != 0) {
            bucketSet.addAll(queryBuckets(MediaStore.Images.Media.INTERNAL_CONTENT_URI));
        }
        if ((mInclude & IncludeDef.INCLUDE_EXTERNAL_IMAGE) != 0) {
            bucketSet.addAll(queryBuckets(MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        }
        if ((mInclude & IncludeDef.INCLUDE_INTERNAL_VIDEO) != 0) {
            bucketSet.addAll(queryBuckets(MediaStore.Video.Media.INTERNAL_CONTENT_URI));
        }
        if ((mInclude & IncludeDef.INCLUDE_EXTERNAL_VIDEO) != 0) {
            bucketSet.addAll(queryBuckets(MediaStore.Video.Media.EXTERNAL_CONTENT_URI));
        }

        final List<MediaBucketBean> bucketList = new ArrayList<>(bucketSet);
        Collections.sort(bucketList, MediaManager.createMediaBucketComparator());

        return bucketList;
    }

    @WorkerThread
    @NonNull
    public final MediaBucketBean queryBucketDetail(@Nullable Long bucketId) {
        final MediaBucketBean bucket = new MediaBucketBean(bucketId);
        final List<MediaBean> coverList = new ArrayList<>();

        if ((mInclude & IncludeDef.INCLUDE_INTERNAL_IMAGE) != 0) {
            fillMediaBucketDetailFromUri(MediaStore.Images.Media.INTERNAL_CONTENT_URI, bucketId, bucket, coverList);
        }
        if ((mInclude & IncludeDef.INCLUDE_EXTERNAL_IMAGE) != 0) {
            fillMediaBucketDetailFromUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, bucketId, bucket, coverList);
        }
        if ((mInclude & IncludeDef.INCLUDE_INTERNAL_VIDEO) != 0) {
            fillMediaBucketDetailFromUri(MediaStore.Video.Media.INTERNAL_CONTENT_URI, bucketId, bucket, coverList);
        }
        if ((mInclude & IncludeDef.INCLUDE_EXTERNAL_VIDEO) != 0) {
            fillMediaBucketDetailFromUri(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, bucketId, bucket, coverList);
        }
        if (!coverList.isEmpty()) {
            Collections.sort(coverList, MediaManager.createMediaComparator(mSort));
            bucket.cover = coverList.get(0).uri;
        }

        return bucket;
    }

    @WorkerThread
    public final void fillMediaBucketDetailFromUri(@NonNull Uri uri,
                                                   @Nullable Long bucketId,
                                                   @NonNull MediaBucketBean bucketBean,
                                                   @NonNull List<MediaBean> covers) {
        final Cursor cursor = queryBucketDetail(uri, bucketId);
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

    private Cursor queryBucketDetail(@NonNull Uri uri, @Nullable Long bucketId) {
        return mContentResolver.query(
                uri,
                PROJECTION_BUCKET_DETAIL,
                MediaStore.MediaColumns.BUCKET_ID + " = ?",
                new String[]{String.valueOf(bucketId)},
                sortOrder(),
                mCancellationSignal);
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
                (mSort == SortDef.SORT_ASCENDING)
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
