/**
 * Copyright 2016 Bartosz Schiller
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.pdfviewer;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;

import com.github.barteksc.pdfviewer.listener.CustomLoadCompleteListener;
import com.github.barteksc.pdfviewer.util.FileUtils;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.URI;

class DecodingAsyncTask extends AsyncTask<Void, Void, Throwable> {

    private boolean cancelled;

    private String path;

    private boolean isAsset;

    private PDFView pdfView;

    private Context context;
    private PdfiumCore pdfiumCore;
    private PdfDocument pdfDocument;
    private CustomLoadCompleteListener customLoadCompleteListener;

    public DecodingAsyncTask(String path, boolean isAsset, PDFView pdfView, PdfiumCore pdfiumCore, @Nullable CustomLoadCompleteListener listener) {
        this.cancelled = false;
        this.pdfView = pdfView;
        this.isAsset = isAsset;
        this.pdfiumCore = pdfiumCore;
        this.path = path;
        this.customLoadCompleteListener = listener;
        context = pdfView.getContext();
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        try {
            if(isAsset) {
                path = FileUtils.fileFromAsset(context, path).getAbsolutePath();
            }
            pdfDocument = pdfiumCore.newDocument(getSeekableFileDescriptor(path));
            return null;
        } catch (Throwable t) {
            return t;
        }
    }

    protected FileDescriptor getSeekableFileDescriptor(String path) throws IOException {
        ParcelFileDescriptor pfd;

        File pdfCopy = new File(path);
        if (pdfCopy.exists()) {
            pfd = ParcelFileDescriptor.open(pdfCopy, ParcelFileDescriptor.MODE_READ_ONLY);
            return pfd.getFileDescriptor();
        }

        URI uri = URI.create(String.format("file://%s", path));
        pfd = context.getContentResolver().openFileDescriptor(Uri.parse(uri.toString()), "rw");

        if (pfd == null) {
            throw new IOException("Cannot get FileDescriptor for " + path);
        }

        return pfd.getFileDescriptor();
    }

    @Override
    protected void onPostExecute(Throwable t) {
        if (t != null) {
            pdfView.loadError(t);
            return;
        }
        if (!cancelled) {
            if(customLoadCompleteListener == null){
                pdfView.loadComplete(pdfDocument);
            }else{
                customLoadCompleteListener.loadComplete(pdfDocument);
            }

        }
    }

    @Override
    protected void onCancelled() {
        cancelled = true;
    }
}
