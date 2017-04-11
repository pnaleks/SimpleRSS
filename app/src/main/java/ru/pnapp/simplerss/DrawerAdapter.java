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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

class DrawerAdapter extends BaseAdapter implements Observer {
    private static final String PREF_FEED_SET = "pref_feed_set";
    static final String PREF_FEED = "pref_feed";

    private Item headerItem;
    private Item addFeedItem;

    private SharedPreferences sharedPreferences;

    private ArrayList<Item> mItems = new ArrayList<>();

    private Item checkedItem;

    @Override
    public void update(Observable observable, Object o) {
        if (o == null) { // No errors
            RssPresenter rssPresenter = (RssPresenter) observable;
            updateItems(rssPresenter.getTitle(), rssPresenter.getUrlString());
        }
    }

    enum ITEM_TYPE {
        HEADER(R.layout.drawer_header),
        ITEM(R.layout.drawer_item);

        private int layoutResId;

        ITEM_TYPE(int layoutResId) { this.layoutResId = layoutResId; }
    }
	
	class Item {
		ITEM_TYPE type;

        int iconId;
        int titleId;
        int textId;

        Drawable icon;
        String title;
        String text;

        Item(ITEM_TYPE type) { this.type = type; }

        void bind(View view) {
            TextView titleView = (TextView) view.findViewById(R.id.title);
            if (titleView == null) return;

            if (icon == null && iconId > 0) {
                icon = ContextCompat.getDrawable(view.getContext(), iconId);
            }
            if (icon != null) {
                ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
                if (imageView == null)
                    titleView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                else
                    imageView.setImageDrawable(icon);
            }

            if (title == null && titleId > 0) {
                title = view.getResources().getString(titleId);
            }
            if (title != null) titleView.setText(title);

            TextView descriptionView = (TextView) view.findViewById(R.id.text);
            if (descriptionView != null) {
                if (text == null && textId > 0) {
                    text = view.getResources().getString(textId);
                }

                if (text == null) {
                    descriptionView.setVisibility(View.GONE);
                } else {
                    descriptionView.setText(text);
                    descriptionView.setVisibility(View.VISIBLE);
                }
            }

            if (type == ITEM_TYPE.ITEM) {
                if (checkedItem == this) {
                    view.setBackgroundColor(0x7fffffff);
                } else {
                    view.setBackgroundColor(0x0);
                }
            }
        }

	}

	@Override
    public int getItemViewType(int position) { return mItems.get(position).type.ordinal(); }
	@Override
    public int getViewTypeCount() { return ITEM_TYPE.values().length; }
	@Override
    public int getCount() { return mItems.size(); }
	@Override
    public boolean isEmpty() { return mItems.isEmpty(); }
	@Override
    public Object getItem(int position) { return mItems.get(position); }
	@Override
    public long getItemId(int position) { return 0; }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Item item = mItems.get(position);
		
		View view = convertView;
		if ( view == null ) view = inflater.inflate(item.type.layoutResId, parent, false);

        item.bind(view);

		return view;
	}

    private void updateItems(@NonNull String title, @NonNull String text) {
        for (Item old : mItems) {
            if (old.type == ITEM_TYPE.ITEM && text.equals(old.text) ) {
                checkedItem = old;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREF_FEED, text);
                if (!title.equals(old.title)) {
                    old.title = title;
                    editor.putString(text, title);
                }
                editor.apply();
                notifyDataSetChanged();
                return;
            }
        }


        Item item = new Item(ITEM_TYPE.ITEM);
        item.iconId = R.drawable.ic_rss_feed_white_24dp;
        item.title = title;
        item.text = text;
        mItems.add(mItems.size() - 1, item);
        checkedItem = item;
        notifyDataSetChanged();

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor
                .putString(PREF_FEED, text)
                .putString(text, title);

        Set<String> feedSet = sharedPreferences.getStringSet(PREF_FEED_SET, null);
        if (feedSet == null) feedSet = new HashSet<>();
        if (feedSet.add(text)) editor.putStringSet(PREF_FEED_SET, feedSet);

        editor.apply();
    }

    void setup(Context context) {
        if (headerItem == null) {
            headerItem = new Item(ITEM_TYPE.HEADER);
            headerItem.iconId = R.drawable.ic_launcher;
            headerItem.titleId = R.string.app_name;
            headerItem.textId = R.string.app_about;

            addFeedItem = new Item(ITEM_TYPE.ITEM);
            addFeedItem.iconId = R.drawable.ic_add_white_24dp;
            addFeedItem.titleId = R.string.action_add_feed;
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        mItems.clear();
        mItems.add(headerItem);

        Set<String> feedSet = sharedPreferences.getStringSet(PREF_FEED_SET, null);
        if (feedSet != null) {
            for (String feed : feedSet) {
                String title = sharedPreferences.getString(feed, "");
                Item item = new Item(ITEM_TYPE.ITEM);
                item.iconId = R.drawable.ic_rss_feed_white_24dp;
                item.title = title;
                item.text = feed;

                mItems.add(item);
            }
        }

        mItems.add(addFeedItem);

        notifyDataSetChanged();
    }

}
