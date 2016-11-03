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
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Observable;
import java.util.Observer;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {
    static Drawable imageDefault;
    static Drawable imageBroken;

    static int imageWidth;
    static int imageHeight;

    DrawerAdapter drawerAdapter = new DrawerAdapter();
    Adapter channelViewAdapter = new Adapter(this);

    ProgressDialog progressDialog;

    @Inject
    RssPresenter mRssPresenter;

    public static class ViewHolder extends RecyclerView.ViewHolder implements Callback {
        private TextView textView;
        private ImageView imageView;
        private ContentLoadingProgressBar progressBar;

        ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            progressBar = (ContentLoadingProgressBar) itemView.findViewById(R.id.progress);
            progressBar.show();
        }

        void setText(CharSequence text) { if( textView != null ) textView.setText(text); }

        void setImage(String urlString) {
            if (imageView != null) {
                if (urlString == null) {
                    imageView.setImageDrawable(imageDefault);
                } else {
                    progressBar.show();
                    Picasso.with(imageView.getContext()).load(urlString)
                            .resize(imageWidth, imageHeight)
                            .centerInside()
                            .placeholder(imageDefault)
                            .error(imageBroken)
                            .into(imageView, this);
                    return;
                }
            }
            if (progressBar != null) progressBar.hide();
        }

        @Override
        public void onSuccess() { if (progressBar != null) progressBar.hide(); }
        @Override
        public void onError() { if (progressBar != null) progressBar.hide(); }
    }

    public static class Adapter extends RecyclerView.Adapter<ViewHolder> implements Observer, View.OnClickListener {
        MainActivity activity;
        RssPresenter rssPresenter;

        Adapter(MainActivity activity) {this.activity = activity;}

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = activity.getLayoutInflater().inflate(R.layout.card, parent, false);
            view.setOnClickListener(this);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if( rssPresenter == null ) return;

            holder.setText(rssPresenter.getItemTitle(position));
            holder.setImage(rssPresenter.getItemEnclosureUrl(position));
            holder.itemView.setTag(position); // For onClick
        }

        @Override
        public int getItemCount() {
            return rssPresenter == null ? 0 : rssPresenter.getItemCount();
        }

        @Override
        public void onClick(View view) {
            if (rssPresenter != null) {
                int position = (Integer) view.getTag();
                Fragment fragment = new ItemViewFragment.Builder(activity)
                        .setTitle(rssPresenter.getItemTitle(position))
                        .setText(rssPresenter.getItemDescription(position))
                        .setImageUrl(rssPresenter.getItemEnclosureUrl(position))
                        .setLink(rssPresenter.getItemLink(position))
                        .build();
                activity.getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }

        @Override
        public void update(Observable observable, Object o) {
            activity.hideProgress();
            if (o == null) {
                rssPresenter = (RssPresenter) observable;
                activity.setTitle(rssPresenter.getTitle());
                notifyDataSetChanged();
            } else {
                Toast.makeText(activity, "Error: " + o, Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DaggerRssPresenterComponent.create().inject(this);

        if (imageDefault == null) {
            imageDefault = ContextCompat.getDrawable(this, R.drawable.ic_photo_camera_white_48dp);
            imageBroken = ContextCompat.getDrawable(this, R.drawable.ic_broken_image_white_48dp);
            imageWidth = (int)(0.4F * getResources().getDisplayMetrics().widthPixels);
            imageHeight = imageWidth / 2;
        }

        drawerAdapter.setup(this);

        mRssPresenter.addObserver(drawerAdapter);
        mRssPresenter.addObserver(channelViewAdapter);

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
        progressDialog = ProgressDialog.show(this,null,urlString,true);
        mRssPresenter.getFeed(urlString);
    }

    void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
