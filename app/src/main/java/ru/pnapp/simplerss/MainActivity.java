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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements
        CommandEntry.Callback, RssViewer {
    public static final String PREF_AUTO_COMPLETE_SET = "pref_auto_complete_set";
    public static final String PREF_INPUT_TEXT        = "pref_input_text";

    private RecyclerView mRecyclerView;

    /** Элемент ввода имени или адреса для Action Bar */
    private CommandEntry mEntryView;
    /** Содержит строки для автоподстановки */
    private HashSet<String> mAutoCompleteSet;
    /** Адаптер автоподстановки строки ввода */
    private ArrayAdapter<String> mAutoCompleteAdapter;
    /** Last user input */
    private String mInputText;

    Drawable imageDefault;
    Drawable imageBroken;

    @Inject
    RssPresenter mRssPresenter;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private ImageView imageView;
        private ContentLoadingProgressBar progressBar;
        private int expectedImagePosition = -1;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            progressBar = (ContentLoadingProgressBar) itemView.findViewById(R.id.progress);
            progressBar.show();
        }

        public void setText(CharSequence text) {
            if( textView != null ) textView.setText(text);
        }

        public void setImagePosition(int position) {
            if( expectedImagePosition != position ) {
                imageView.setImageDrawable(imageDefault);
                progressBar.show();
                expectedImagePosition = position;
            }
        }

        public void setImage(Drawable image, int imagePosition) {
            if( expectedImagePosition >= 0 && imagePosition == expectedImagePosition ) {
                if (imageView != null) imageView.setImageDrawable(image);
                if (progressBar != null) progressBar.hide();
            }
        }
    }

    public class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.card, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle( mRssPresenter.getItemTitle(position) )
                            .setMessage( mRssPresenter.getItemDescription(position))
                            .show();
                }
            });
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if( mRssPresenter == null ) return;
            holder.setText( mRssPresenter.getItemTitle(position) );
            holder.setImagePosition(position);
            holder.itemView.setTag(position);

            if( !mRssPresenter.loadImage(holder, position) ) {
                holder.setImage(imageDefault, position);
            }
        }

        @Override
        public int getItemCount() {
            return mRssPresenter == null ? 0 : mRssPresenter.getItemCount();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageDefault = ContextCompat.getDrawable(this, R.drawable.ic_photo_camera_white_48dp);
        imageBroken = ContextCompat.getDrawable(this, R.drawable.ic_broken_image_white_48dp);

        mAutoCompleteAdapter = new ArrayAdapter<>(this, R.layout.list_item);

        mEntryView = new CommandEntry(this);
        mEntryView.setAdapter(mAutoCompleteAdapter);

        DaggerRssPresenterComponent.create().inject(this);

        float width = 0.4F * getResources().getDisplayMetrics().widthPixels;

        DaggerImageLoaderModelComponent.create().imageLoaderModel().setImageSize((int)width, (int)width * 2);

        mRssPresenter.setRssViewer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mInputText = sharedPreferences.getString(PREF_INPUT_TEXT, "");

        mAutoCompleteSet = (HashSet<String>) sharedPreferences.getStringSet(PREF_AUTO_COMPLETE_SET, new HashSet<String>());
        mAutoCompleteAdapter.addAll(mAutoCompleteSet);

        mEntryView.setAdapter(mAutoCompleteAdapter);
        mEntryView.setText(mInputText);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(new Adapter());
    }

    @Override
    protected void onPause() {
        super.onPause();

        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putStringSet(PREF_AUTO_COMPLETE_SET, mAutoCompleteSet)
                .putString(PREF_INPUT_TEXT, mInputText)
                .apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(mEntryView, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
        }
        return true;
    }

    @Override
    public void onActionPlay(final String text) {
        mEntryView.toggleAction();
        mInputText = text;
        if( mAutoCompleteSet.add(text) ) mAutoCompleteAdapter.add(text);
        mRssPresenter.getFeed(text);
    }

    @Override
    public void onActionStop() {
        Toast.makeText(this, "Stopping not yet implemented", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDataReady() {
        mEntryView.toggleAction();
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void displayImage(Object target, int position, Bitmap bitmap) {
        ViewHolder holder = (ViewHolder) target;
        if( bitmap == null ) {
            holder.setImage(imageBroken, position);
        } else {
            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
            bitmapDrawable.setTargetDensity(getResources().getDisplayMetrics());
            holder.setImage(bitmapDrawable, position);
        }

    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
        mEntryView.toggleAction();
    }
}
