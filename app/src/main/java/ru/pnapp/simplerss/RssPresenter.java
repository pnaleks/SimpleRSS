package ru.pnapp.simplerss;

import android.graphics.Bitmap;

public interface RssPresenter {
    /**
     * Initialize viewer
     * @param rssViewer viewer to interact
     */
    void setRssViewer(RssViewer rssViewer);

    /**
     * Load RSS Feed data from an uri<br>
     * It should call {@link RssViewer#onDataReady()} upon completed
     * @param uriString uri to load
     */
    void getFeed(String uriString);

    /**
     * Gets number of items in the feed
     * @return number of items
     */
    int getItemCount();

    String getItemTitle(int position);

    String getItemDescription(int position);

    /**
     * Load an image if any is pointed by the item at the position
     * @param target some object that will handle this image
     * @param position item position in the feed
     * @return true if loading is initiated and {@link RssViewer#displayImage(Object, int, Bitmap)} will be called upon completion
     */
    boolean loadImage(Object target, int position);
}
