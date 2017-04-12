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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.Set;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {
    DrawerAdapter drawerAdapter = new DrawerAdapter();
    ChannelViewAdapter channelViewAdapter = new ChannelViewAdapter(this);

    @Inject
    RssPresenter mRssPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DaggerRssPresenterComponent.create().inject(this);

        ViewHolder.init(this);

        drawerAdapter.setup(this);

        mRssPresenter.addObserver(channelViewAdapter);
        mRssPresenter.addObserver(drawerAdapter);

        String feed;
        if (savedInstanceState != null) {
            feed = savedInstanceState.getString(DrawerAdapter.PREF_FEED);
            if (feed != null) {
                if (feed.equals(mRssPresenter.getUrlString())) {
                    mRssPresenter.getFeed();
                } else {
                    mRssPresenter.getFeed(feed);
                }
                return;
            }
        }

        feed = PreferenceManager.getDefaultSharedPreferences(this).getString(DrawerAdapter.PREF_FEED, null);
        if (feed != null) getFeed(feed);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, ChannelViewFragment.newInstance())
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DrawerFragment drawerFragment = (DrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        drawerFragment.syncIndicator();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DrawerAdapter.PREF_FEED, mRssPresenter.getUrlString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRssPresenter.deleteObservers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.delete_feed) {
            final String url = mRssPresenter.getUrlString();
            if (url != null) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_feed)
                        .setMessage(getString(R.string.format_delete_feed_alert, url))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                Set<String> feedSet = preferences.getStringSet(DrawerAdapter.PREF_FEED_SET, null);
                                if (feedSet != null && feedSet.remove(url)) {
                                    String feed = preferences.getString(DrawerAdapter.PREF_FEED, null);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.remove(url);
                                    editor.putStringSet(DrawerAdapter.PREF_FEED_SET, feedSet);
                                    if (url.equals(feed)) editor.putString(DrawerAdapter.PREF_FEED, null);
                                    editor.apply();
                                    drawerAdapter.setup(MainActivity.this);
                                    mRssPresenter.clear();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }

            return true;
        }
        if( id == android.R.id.home ) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
                return true;
            }
        }



        return super.onOptionsItemSelected(item);
    }

    public void onDrawerItemSelected(int position) {
        DrawerAdapter.Item item = (DrawerAdapter.Item) drawerAdapter.getItem(position);

        if (item.type != DrawerAdapter.ITEM_TYPE.ITEM) return;

        getFragmentManager().popBackStack();

        if (item.titleId == R.string.action_add_feed) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final EditText editText = new EditText(this);
            builder.setView(editText)
                    .setTitle("Enter URL")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getFeed(editText.getText().toString());
                        }
                    })
                    .show();
            return;
        }
        getFeed(item.text);
    }

    void getFeed(String urlString) {
        channelViewAdapter.setRefreshing(true);
        mRssPresenter.getFeed(urlString);
    }

}
