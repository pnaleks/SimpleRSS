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
import java.util.Observable;

abstract class RssPresenter extends Observable {
    /** Url of the last feed */
    private String urlString;

    String getUrlString() { return urlString; }
    void setUrlString(String urlString) { this.urlString = urlString; }

    /**
     * Load RSS Feed data from an uri<br>
     * On success tt should call {@link #onDataReady(String)} or {@link #onError(String)} otherwise.
     *
     * @param uriString uri to load
     */
    abstract void getFeed(String uriString);

    /**
     * Allows to reproduce the feed if it was obtained at previous call to {@link #getFeed(String)}<br>
     * If data is available it should call {@link #onDataReady(String)}
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

    void onDataReady(final String requestUrlString) {
        setChanged();
        if(countObservers() > 0 ) {
            Runnable callback = new Runnable() {
                @Override
                public void run() {
                    if (requestUrlString.equals(urlString)) {
                        notifyObservers();
                    }
                }
            };
            new Handler(Looper.getMainLooper()).post(callback);
        }
    }

    void onError(final String message) {
        setChanged();
        if( countObservers() > 0 ) {
            Runnable callback = new Runnable() {
                @Override
                public void run() {
                    notifyObservers(message);
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
