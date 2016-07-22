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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;

import dagger.Module;
import dagger.Provides;

@Module
public class SpringRssPresenter implements RssPresenter {
    private RssViewer rssViewer;
    private ArrayList<Item> items;
    private HashMap<String, Bitmap> map = new HashMap<>();

    @Provides
    public static RssPresenter provideRssPresenter() {
        return new SpringRssPresenter();
    }

    @Override
    public void setRssViewer(RssViewer rssViewer) {
        this.rssViewer = rssViewer;
    }

    @Override
    public void getFeed(final String uriString) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Runnable callback;
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    Rss rss = restTemplate.getForObject(uriString, Rss.class);
                    items = rss.channel == null ? null : rss.channel.items;
                    if( items == null ) throw new Exception("Cant't get rss from " + uriString);
                    callback = new Runnable() {
                        @Override
                        public void run() {
                            rssViewer.onDataReady();
                        }
                    };

                } catch (final Exception e) {
                    callback = new Runnable() {
                        @Override
                        public void run() {
                            rssViewer.onError(e.getMessage());
                        }
                    };
                }
                new Handler(Looper.getMainLooper()).post( callback );
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public String getItemTitle(int position) {
        return items == null ? null : items.get(position).title;
    }

    @Override
    public String getItemDescription(int position) {
        return items == null ? null : items.get(position).description;
    }

    @Override
    public boolean loadImage(final Object target, final int position) {
        if( items == null ) return false;
        Enclosure enclosure = items.get(position).enclosure;
        if( enclosure == null || !enclosure.type.startsWith("image/") ) return false;
        final String urlString = items.get(position).enclosure.url;
        if( urlString == null ) return false;
        if( map.containsKey(urlString) ) {
            rssViewer.displayImage(target, position, map.get(urlString) );
        }
        DaggerImageLoaderModelComponent.create().imageLoaderModel().Load(urlString, new ImageLoaderModel.Callback() {
            @Override
            public void onCompleted(Bitmap bitmap) {
                map.put(urlString, bitmap);
                rssViewer.displayImage(target, position, bitmap);
            }

        });
        return true;
    }

    @Root
    public static class Rss {
        @Attribute public float version;

        @Element public Channel channel;
    }

    @Root(strict = false)
    public static class Channel {
        @ElementList(inline = true) public ArrayList<Item> items;
    }

    @Root(strict = false)
    public static class Item {
        @Element(required = false) public String title;
        @Element(required = false) public String description;
        @Element(required = false) public Enclosure enclosure;
    }

    @Root(strict = false)
    public static class Enclosure {
        @Attribute public String url;
        @Attribute public String type;
    }
}
