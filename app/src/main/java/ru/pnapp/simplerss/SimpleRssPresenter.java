package ru.pnapp.simplerss;

import android.util.Log;

import java.net.URLConnection;

import dagger.Module;
import dagger.Provides;
import ru.pnapp.simple_rss.RssFeed;
import ru.pnapp.simple_rss.RssItem;

@Module
class SimpleRssPresenter extends RssPresenter {
    private RssFeed rssFeed;
    private RssItem cacheRssItem;
    private int cachePosition = -1;

    private static SimpleRssPresenter instance;

    @Provides
    static RssPresenter provideRssPresenter() {
        if( instance == null ) instance = new SimpleRssPresenter();
        return instance;
    }

    SimpleRssPresenter() {}

    @Override
    public void getFeed(final String uriString) {
        cacheRssItem = null;
        cachePosition = -1;
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("PNApp", "WORKING on " + uriString);
                    URLConnection connection = getConnection(getUrlString(uriString));
                    if (connection != null) {
                        String requestUrlString = connection.getURL().toString();
                        setUrlString(requestUrlString);
                        rssFeed = RssFeed.get(connection.getInputStream());
                        callOnDataReady(requestUrlString);
                    }
                    Log.i("PNApp", "DONE with " + uriString);
                } catch (Exception e) {
                    callOnError(e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        new Thread(task).start();
    }

    @Override
    public void getFeed() {
        String urlString = getUrlString();
        if( urlString != null && rssFeed != null ) callOnDataReady(urlString);
    }

    @Override
    String getTitle() { return rssFeed.channel.title; }

    @Override
    public int getItemCount() {
        return rssFeed == null ? 0 : rssFeed.channel.itemList.size();
    }

    @Override
    public String getItemTitle(int position) {
        return getItem(position).title;
    }

    @Override
    public String getItemDescription(int position) {
        return getItem(position).description;
    }

    @Override
    String getItemEnclosureUrl(int position) {
        RssItem.Enclosure enclosure = getItem(position).enclosure;
        return enclosure == null ? null : enclosure.url;
    }

    @Override
    String getItemLink(int position) {
        return getItem(position).link;
    }

    private RssItem getItem(int position) {
        if (cachePosition == position) return cacheRssItem;
        cacheRssItem = rssFeed.channel.itemList.get(position);
        cachePosition = position;
        return cacheRssItem;
    }
}