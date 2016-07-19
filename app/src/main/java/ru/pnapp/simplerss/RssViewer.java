package ru.pnapp.simplerss;

import android.graphics.Bitmap;

public interface RssViewer {
    /**
     * This function should be called from {@link RssPresenter} as a response on {@link RssPresenter#getFeed(String)}
     * when data loading has been completed
     */
    void onDataReady();

    /**
     * This function should be called from {@link RssPresenter} as a response on {@link RssPresenter#loadImage(Object, int)} when image loading has been completed
     *
     * @param target object passed with {@link RssPresenter#loadImage(Object, int)}
     * @param position integer passed with {@link RssPresenter#loadImage(Object, int)}
     * @param bitmap image data obtained
     */
    void displayImage(Object target, int position, Bitmap bitmap);

    /**
     * Called if error occurs
     * @param message the error message
     */
    void onError(String message);
}
