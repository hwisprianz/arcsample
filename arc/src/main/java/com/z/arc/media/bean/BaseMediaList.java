package com.z.arc.media.bean;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

/**
 * <p>
 * <p>
 * Created by Blate on 2023/12/6
 */
public abstract class BaseMediaList<M extends IMedia> implements IMediaList<M> {


    private static final String[] IMAGE_PROJECTION = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.MINI_THUMB_MAGIC,
            MediaStore.Images.Media.ORIENTATION,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_MODIFIED
    };

    private static final String[] VIDE_PROJECTION = new String[]{
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.MINI_THUMB_MAGIC,
            MediaStore.Video.Media.ORIENTATION,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_MODIFIED
    };

    protected final ContentResolver mContentResolver;

    protected final Uri mUri;

    @Nullable
    protected Cursor mCursor;

    public BaseMediaList(ContentResolver contentResolver, Uri uri) {
        this.mContentResolver = contentResolver;
        this.mUri = uri;
    }

    @WorkerThread
    protected abstract void query();

    @Override
    @Nullable
    public M get(int index) {
        synchronized (this) {
            if (mCursor != null) {
                return mCursor.moveToPosition(index) ? createFromCursor() : null;
            } else {
                return null;
            }
        }
    }

    @Override
    public int getCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            if (mCursor != null) {
                mCursor.close();
            }
        }
    }

    protected abstract M createFromCursor();

}
