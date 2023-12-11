package com.z.arc.media;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.z.arc.media.bean.MediaBean;
import com.z.arc.media.bean.MediaBucketBean;

import java.util.Comparator;

/**
 * <p>
 * <p>
 * Created by Blate on 2023/12/7
 */
public class MediaManager {


    public static void fillFromCursor(@NonNull Cursor cursor, @NonNull Uri baseUri, @NonNull MediaBean container,
                                      @IntRange(from = -1) int indexId,
                                      @IntRange(from = -1) int indexData,
                                      @IntRange(from = -1) int indexBucketId,
                                      @IntRange(from = -1) int indexMimeType,
                                      @IntRange(from = -1) int indexDuration,
                                      @IntRange(from = -1) int indexTitle,
                                      @IntRange(from = -1) int indexDateTaken,
                                      @IntRange(from = -1) int indexDateModified

    ) {
        if (indexId == -1) {
            throw new IllegalArgumentException("Cursor must have _ID column");
        }

        @SuppressLint("Range") final long id = cursor.getLong(indexId);

        container.id = id;
        container.uri = ContentUris.withAppendedId(baseUri, id);

        if (indexData != -1) {
            container.data = cursor.getString(indexData);
        }

        if (indexBucketId != -1) {
            container.bucketId = cursor.getLong(indexBucketId);
        }

        if (indexMimeType != -1) {
            container.mimeType = cursor.getString(indexMimeType);
        }

        if (indexDuration != -1) {
            container.duration = cursor.getInt(indexDuration);
        }

        if (indexTitle != -1) {
            container.title = cursor.getString(indexTitle);
        }

        if (indexDateTaken != -1) {
            container.dateTaken = cursor.getLong(indexDateTaken);
        }

        if (indexDateModified != -1) {
            container.dateModified = cursor.getLong(indexDateModified);
        }
    }

    @NonNull
    public static MediaBean createFromCursor(@NonNull Cursor cursor, @NonNull Uri baseUri,
                                             @IntRange(from = -1) int indexId,
                                             @IntRange(from = -1) int indexData,
                                             @IntRange(from = -1) int indexBucketId,
                                             @IntRange(from = -1) int indexMimeType,
                                             @IntRange(from = -1) int indexDuration,
                                             @IntRange(from = -1) int indexTitle,
                                             @IntRange(from = -1) int indexDateTaken,
                                             @IntRange(from = -1) int indexDateModified) {
        final MediaBean bean = new MediaBean();
        fillFromCursor(cursor, baseUri, bean,
                indexId,
                indexData,
                indexBucketId,
                indexMimeType,
                indexDuration,
                indexTitle,
                indexDateTaken,
                indexDateModified);
        return bean;
    }

    @NonNull
    public static MediaBean createFromCursor(@NonNull Cursor cursor, @NonNull Uri baseUri) {
        return createFromCursor(cursor, baseUri,
                cursor.getColumnIndex(MediaStore.MediaColumns._ID),
                cursor.getColumnIndex(MediaStore.MediaColumns.DATA),
                cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_ID),
                cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE),
                cursor.getColumnIndex(MediaStore.MediaColumns.DURATION),
                cursor.getColumnIndex(MediaStore.MediaColumns.TITLE),
                cursor.getColumnIndex(MediaStore.MediaColumns.DATE_TAKEN),
                cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED));
    }

    public static Comparator<MediaBean> createMediaComparator(@MediaConstance.SortDef int sort) {
        return (o1, o2) -> {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return sort == MediaConstance.Sort.SORT_ASCENDING ? 1 : -1;
            } else if (o2 == null) {
                return sort == MediaConstance.Sort.SORT_ASCENDING ? 1 : -1;
            } else {
                long date1 = o1.dateTaken == 0 ? o1.dateModified : o1.dateTaken;
                long date2 = o2.dateTaken == 0 ? o2.dateModified : o2.dateTaken;
                if (date1 != date2) {
                    return sort == MediaConstance.Sort.SORT_ASCENDING ?
                            Long.compare(date1, date2) :
                            Long.compare(date2, date1);
                } else {
                    return sort == MediaConstance.Sort.SORT_ASCENDING ?
                            Long.compare(o1.id, o2.id) :
                            Long.compare(o2.id, o1.id);
                }
            }
        };
    }

    public static Comparator<MediaBucketBean> createMediaBucketComparator() {
        return (o1, o2) -> {
            final String s1 = o1 == null ? null : o1.displayName;
            final String s2 = o2 == null ? null : o2.displayName;
            if (s1 == null && s2 == null) {
                return 0;
            } else if (s1 == null) {
                return 1;
            } else if (s2 == null) {
                return -1;
            } else {
                return s1.compareTo(s2);
            }
        };
    }

}
