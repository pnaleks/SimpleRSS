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
    ArrayList<RssItem> rssItems;

    @Provides
    static RssPresenter provideRssPresenter() {
        return new UrlRssPresenter();
    }

    public UrlRssPresenter() {}

    @Override
    public void setRssViewer(RssViewer rssViewer) {
        this.rssViewer = rssViewer;
    }

    @Override
    public void getFeed(final String uriString) {
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
                Runnable callback;
                try {
                    rssItems = RssReader.read(new URL(urlString)).getRssItems();
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
        return rssItems == null ? 0 : rssItems.size();
    }

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
                rssViewer.displayImage(target, position, bitmap);
            }

        });
        return true;
    }
}
