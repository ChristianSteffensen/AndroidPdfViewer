package com.github.barteksc.pdfviewer.listener;

import android.graphics.Bitmap;

/**
 * Created by christian on 28/06/16.
 */
public interface ThumbnailListener {
    void thumbnailReady(Bitmap bitmap);
}
