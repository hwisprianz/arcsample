package com.z.arc.media.bean;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.z.arc.media.MediaManager;

/**
 * <p>
 * <p>
 * Created by Blate on 2023/12/8
 */
public class MediaList implements IMediaList {

    @NonNull
    private final Uri baseUri;

    @Nullable
    private final Cursor cursor;

    @IntRange(from = -1)
    private final int indexId;
    @IntRange(from = -1)
    private final int indexData;
    @IntRange(from = -1)
    private final int indexBucketId;
    @IntRange(from = -1)
    private final int indexMimeType;
    @IntRange(from = -1)
    private final int indexDuration;
    @IntRange(from = -1)
    private final int indexTitle;
    @IntRange(from = -1)
    private final int indexDateTaken;
    @IntRange(from = -1)
    private final int indexDateModified;

    public MediaList(@NonNull Uri baseUri, @Nullable Cursor cursor) {
        this.baseUri = baseUri;
        this.cursor = cursor;

        if (cursor != null) {
            this.indexId = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
            this.indexData = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
            this.indexBucketId = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_ID);
            this.indexMimeType = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
            this.indexDuration = cursor.getColumnIndex(MediaStore.MediaColumns.DURATION);
            this.indexTitle = cursor.getColumnIndex(MediaStore.MediaColumns.TITLE);
            this.indexDateTaken = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_TAKEN);
            this.indexDateModified = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED);
        } else {
            this.indexId = -1;
            this.indexData = -1;
            this.indexBucketId = -1;
            this.indexMimeType = -1;
            this.indexDuration = -1;
            this.indexTitle = -1;
            this.indexDateTaken = -1;
            this.indexDateModified = -1;
        }
    }

    @Nullable
    @Override
    public MediaBean get(int index) {
        final MediaBean bean = new MediaBean();
        fill(index, bean);
        return bean;
    }

    @Override
    public void fill(int index, @NonNull MediaBean container) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException(String.format("index %d out of bounds %d", index, getCount()));
        }
        assert cursor != null;
        cursor.moveToPosition(index);
        MediaManager.fillFromCursor(cursor, baseUri, container,
                indexId,
                indexData,
                indexBucketId,
                indexMimeType,
                indexDuration,
                indexTitle,
                indexDateTaken,
                indexDateModified);
    }

    @Override
    public int getCount() {
        return (cursor == null ? 0 : cursor.getCount());
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public void close() {
        if (cursor != null) {
            cursor.close();
        }
    }

}
