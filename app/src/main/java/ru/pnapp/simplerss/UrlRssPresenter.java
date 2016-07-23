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
import android.os.Handler;
import android.os.Looper;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import dagger.Module;
import dagger.Provides;
import nl.matshofman.saxrssreader.RssItem;
import nl.matshofman.saxrssreader.RssReader;

@Module
public class UrlRssPresenter implements RssPresenter {
    static final String DEFAULT_URI_SCHEME = "https";

    private HashMap<String, Bitmap> map = new HashMap<>();

    private RssViewer rssViewer;
    private String urlString;
    private ArrayList<RssItem> rssItems;

    static UrlRssPresenter instance;

    @Provides
    static RssPresenter provideRssPresenter() {
        if( instance == null ) instance = new UrlRssPresenter();
        return instance;
    }

    public UrlRssPresenter() {}

    @Override
    public synchronized void setRssViewer(RssViewer rssViewer) { this.rssViewer = rssViewer; }

    @Override
    public String getFeed(String uriString) {
        URI uri = URI.create(uriString);
        String scheme = uri.getScheme();
        if( scheme == null ) {
            String fix = DEFAULT_URI_SCHEME +':';
            if( uri.getAuthority() == null ) {
                fix += "//";
            }
            uri = URI.create(fix + uriString);
        }
        urlString = uri.toString();
        if( (uri.getPath() == null || uri.getPath().isEmpty())
                && (uri.getQuery() == null || uri.getQuery().isEmpty())
                && (uri.getFragment() == null || uri.getFragment().isEmpty()) ) urlString += '/';

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    rssItems = RssReader.read(new URL(urlString)).getRssItems();
                    callOnDataReady();
                } catch (final Exception e) {
                    callOnError(e.getMessage());
                }
            }
        };
        new Thread(runnable).start();
        return urlString;
    }

    @Override
    public String getFeed() {
        if( urlString == null ) return null;
        if( rssItems != null ) callOnDataReady();
        return urlString;
    }

    @Override
    public int getItemCount() { return rssItems == null ? 0 : rssItems.size(); }

    @Override
    public String getItemTitle(int position) {
        if( rssItems == null ) return null;
        return rssItems.get(position).getTitle();
    }

    @Override
    public String getItemDescription(int position) {
        if( rssItems == null ) return null;
        return rssItems.get(position).getDescription();
    }

    @Override
    public boolean loadImage(final Object target, final int position) {
        if( rssItems == null ) return false;
        final String urlString = rssItems.get(position).getImageUrl();
        if( urlString == null ) return false;
        if( map.containsKey(urlString) ) {
            rssViewer.displayImage(target, position, map.get(urlString) );
        }
        DaggerImageLoaderModelComponent.create().imageLoaderModel().Load(urlString, new ImageLoaderModel.Callback() {
            @Override
            public void onCompleted(Bitmap bitmap) {
                map.put(urlString, bitmap);
                if( rssViewer != null ) rssViewer.displayImage(target, position, bitmap);
            }

        });
        return true;
    }

    private void callOnDataReady() {
        if( rssViewer == null ) return;
        Runnable callback = new Runnable() {
            @Override
            public void run() {
                if( rssViewer != null ) rssViewer.onDataReady();
            }
        };
        new Handler(Looper.getMainLooper()).post( callback );
    }

    private void callOnError(final String message) {
        if( rssViewer == null ) return;
        Runnable callback = new Runnable() {
            @Override
            public void run() {
                if( rssViewer != null ) rssViewer.onError(message);
            }
        };
        new Handler(Looper.getMainLooper()).post( callback );
    }
}
