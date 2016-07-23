/*
 *  Copyright 2016 P.N.Alekseev <pnaleks@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.pnapp.simplerss;

import android.graphics.Bitmap;

public interface RssPresenter {
    /**
     * Initialize or free the viewer. Should be set to null when the viewer becomes unavailable such as at Activity.onDestroy()
     * @param rssViewer viewer to interact or null
     */
    void setRssViewer(RssViewer rssViewer);

    /**
     * Load RSS Feed data from an uri<br>
     * It should call {@link RssViewer#onDataReady()} upon completed
     * @param uriString uri to load
     * @return uriString of the feed, probably fixed by the presenter
     */
    String getFeed(String uriString);

    /**
     * Allows to reproduce the feed if it was obtained at previous call to {@link #getFeed(String)}<br>
     * If data is available it should call {@link RssViewer#onDataReady()}
     * @return uriString of that feed
     */
    String getFeed();

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
