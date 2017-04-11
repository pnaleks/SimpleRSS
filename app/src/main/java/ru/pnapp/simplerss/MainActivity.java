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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

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

        if (savedInstanceState == null) {
            String feed = PreferenceManager.getDefaultSharedPreferences(this).getString(DrawerAdapter.PREF_FEED, null);
            if (feed != null) getFeed(feed);

            getFragmentManager().beginTransaction()
                    .replace(R.id.container, ChannelViewFragment.newInstance())
                    .commit();
        } else {
            mRssPresenter.getFeed();
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
