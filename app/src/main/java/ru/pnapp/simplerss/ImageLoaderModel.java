package ru.pnapp.simplerss;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

abstract public class ImageLoaderModel {
    protected static int height = 400;
    protected static int width = 200;

    /**
     * Sets boundary size of the image
     * @param width maximum allowed image width
     * @param height maximum allowed image height
     */
    public void setImageSize(int width, int height) {
        ImageLoaderModel.height = height;
        ImageLoaderModel.width = width;
    }

    /**
     * Load an image from the url and invoke the callback upon completion
     * @param urlString image url
     * @param callback callback to invoke
     */
    abstract void Load(String urlString, Callback callback);

    public interface Callback {
        /**
         * Called when image ready or error occurs
         * @param bitmap resulting bitmap or null on error
         */
        void onCompleted(@Nullable Bitmap bitmap);
    }
}
