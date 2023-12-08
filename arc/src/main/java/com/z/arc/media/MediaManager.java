package com.z.arc.media;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.z.arc.media.bean.MediaBean;
import com.z.arc.media.bean.MediaBucketBean;
import com.z.arc.media.repo.MediaRepository;

import java.util.Comparator;

/**
 * <p>
 * <p>
 * Created by Blate on 2023/12/7
 */
public class MediaManager {

    @NonNull
    public static MediaBean crateFromCursor(@NonNull Cursor cursor, @NonNull Uri baseUri,
                                            int indexId,
                                            int indexContent,
                                            int indexBucketId,
                                            int indexMimeType,
                                            int indexTitle,
                                            int indexDateTaken,
                                            int indexDateModified) {
        if (indexId == -1) {
            throw new IllegalArgumentException("Cursor must have _ID column");
        }
        final long id = cursor.getLong(indexId);

        final MediaBean bean = new MediaBean(id);

        bean.uri = ContentUris.withAppendedId(baseUri, id);

        if (indexContent != -1) {
            bean.data = cursor.getString(indexContent);
        }

        if (indexBucketId != -1) {
            bean.bucketId = cursor.getLong(indexBucketId);
        }

        if (indexMimeType != -1) {
            bean.mimeType = cursor.getString(indexMimeType);
        }

        if (indexTitle != -1) {
            bean.title = cursor.getString(indexTitle);
        }

        if (indexDateTaken != -1) {
            bean.dateTaken = cursor.getLong(indexDateTaken);
        }

        if (indexDateModified != -1) {
            bean.dateModified = cursor.getLong(indexDateModified);
        }

        return bean;
    }

    @NonNull
    public static MediaBean createFromCursor(@NonNull Cursor cursor, @NonNull Uri baseUri) {
        return crateFromCursor(cursor, baseUri,
                cursor.getColumnIndex(MediaStore.MediaColumns._ID),
                cursor.getColumnIndex(MediaStore.MediaColumns.DATA),
                cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_ID),
                cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE),
                cursor.getColumnIndex(MediaStore.MediaColumns.TITLE),
                cursor.getColumnIndex(MediaStore.MediaColumns.DATE_TAKEN),
                cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED));
    }

    public static Comparator<MediaBean> createMediaComparator(@MediaRepository.SortAnno int sort) {
        return (o1, o2) -> {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return sort == MediaRepository.SortDef.SORT_ASCENDING ? 1 : -1;
            } else if (o2 == null) {
                return sort == MediaRepository.SortDef.SORT_ASCENDING ? 1 : -1;
            } else {
                long date1 = o1.dateTaken == 0 ? o1.dateModified : o1.dateTaken;
                long date2 = o2.dateTaken == 0 ? o2.dateModified : o2.dateTaken;
                if (date1 != date2) {
                    return sort == MediaRepository.SortDef.SORT_ASCENDING ?
                            Long.compare(date1, date2) :
                            Long.compare(date2, date1);
                } else {
                    return sort == MediaRepository.SortDef.SORT_ASCENDING ?
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
