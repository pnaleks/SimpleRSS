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

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;

abstract class RssPresenter {
    /** The {@link RssViewer}s that displays contents of the feed */
    private HashSet<RssViewer> viewers = new HashSet<>();

    /** Url of last the feed */
    private String urlString;

    String getUrlString() { return urlString; }
    void setUrlString(String urlString) { this.urlString = urlString; }

    /**
     * Add the viewer. Should be removed with {@link #removeRssViewer(RssViewer)}
     * when the viewer becomes unavailable such as at Activity.onDestroy()
     *
     * @param rssViewer viewer to interact or null
     */
    synchronized void addRssViewer(RssViewer rssViewer) { viewers.add(rssViewer); }

    /**
     * Remove the viewer.
     * @param rssViewer object to remove
     */
    synchronized void removeRssViewer(RssViewer rssViewer) { viewers.remove(rssViewer); }

    /**
     * Load RSS Feed data from an uri<br>
     * It should call {@link RssViewer#onDataReady(RssPresenter)} upon completed
     * @param uriString uri to load
     */
    abstract void getFeed(String uriString);

    /**
     * Allows to reproduce the feed if it was obtained at previous call to {@link #getFeed(String)}<br>
     * If data is available it should call {@link RssViewer#onDataReady(RssPresenter)}
     */
    abstract void getFeed();


    abstract String getTitle();

    /**
     * Gets number of items in the feed
     * @return number of items
     */
    abstract int getItemCount();

    abstract String getItemTitle(int position);
    abstract String getItemDescription(int position);
    @Nullable
    abstract String getItemEnclosureUrl(int position);
    abstract String getItemLink(int position);

    void callOnDataReady(final String requestUrlString) {
        if( viewers.size() > 0 ) {
            Runnable callback = new Runnable() {
                @Override
                public void run() {
                    if (requestUrlString.equals(urlString)) {
                        for (RssViewer rssViewer : viewers)
                            rssViewer.onDataReady(RssPresenter.this);
                    }
                }
            };
            new Handler(Looper.getMainLooper()).post(callback);
        }
    }

    void callOnError(final String message) {
        if( viewers.size() > 0 ) {
            Runnable callback = new Runnable() {
                @Override
                public void run() {
                    for (RssViewer rssViewer : viewers) rssViewer.onError(message);
                }
            };
            new Handler(Looper.getMainLooper()).post(callback);
        }
    }

    static String getUrlString(String uriString) {
        URI uri = URI.create(uriString);
        String scheme = uri.getScheme();
        if( scheme == null ) {
            String fix = "http:";
            if( uri.getAuthority() == null ) {
                fix += "//";
            }
            uri = URI.create(fix + uriString);
        }
        String urlString = uri.toString();
        if( (uri.getPath() == null || uri.getPath().isEmpty())
                && (uri.getQuery() == null || uri.getQuery().isEmpty())
                && (uri.getFragment() == null || uri.getFragment().isEmpty()) ) urlString += '/';
        return urlString;
    }

    private static final int REDIRECT_LIMIT = 5;
    private static int redirectCount = 0;
    static URLConnection getConnection(String urlString) {
        URLConnection result = null;
        if (redirectCount < REDIRECT_LIMIT) {
            try {
                URLConnection connection = (new URL(urlString)).openConnection();
                String location = connection.getHeaderField("Location");
                if (location != null) {
                    redirectCount++;
                    return getConnection(URI.create(urlString).resolve(location).toString());
                }
                result = connection;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        redirectCount = 0;
        return result;
    }

}
