package com.z.arc.media.bean;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.HashMap;

/**
 * <p>
 * <p>
 * Created by Blate on 2023/12/6
 */
public class ImageList extends BaseMediaList<ImageBean> {

    public ImageList(ContentResolver contentResolver, Uri uri) {
        super(contentResolver, uri);
    }

    @Override
    protected void query() {
//        mContentResolver.query(mUri,
//                , , );
    }

    @Override
    protected ImageBean createFromCursor() {
        final ImageBean bean = new ImageBean();
        fill(bean);
        return bean;
    }

    @Override
    public HashMap<String, String> getBucketIds() {
        return null;
    }

    @Override
    public void fill(int index, @NonNull ImageBean container) {
        synchronized (this) {
            if (mCursor != null && mCursor.moveToPosition(index)) {
                fill(container);
            }
        }
    }

    private void fill(@NonNull ImageBean container) {

    }

}
