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

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ItemViewFragment extends Fragment {
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_IMAGE_URL = "arg_image_url";
    private static final String ARG_TEXT = "arg_text";
    private static final String ARG_LINK_URL = "arg_link_url";

    private Intent shareIntent;
    private Intent openInBrowserIntent;

    static class Builder {
        Context context;
        Bundle arguments = new Bundle();

        Builder(Context context) { this.context = context; }

        Builder setTitle(String title) {
            arguments.putString(ARG_TITLE, title);
            return this;
        }

        Builder setImageUrl(String urlString) {
            arguments.putString(ARG_IMAGE_URL, urlString);
            return this;
        }

        Builder setText(String text) {
            arguments.putString(ARG_TEXT, text);
            return this;
        }

        Builder setLink(String urlString) {
            arguments.putString(ARG_LINK_URL, urlString);
            return this;
        }

        ItemViewFragment build() {
            ItemViewFragment fragment = new ItemViewFragment();
            fragment.setArguments(arguments);
            return fragment;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_view_fragment, container, false);
        Bundle arguments = getArguments();

        String title = arguments.getString(ARG_TITLE);

        if (title != null) {
            ((TextView)view.findViewById(R.id.title)).setText(title);
        }

        if (arguments.containsKey(ARG_TEXT)) {
            ((TextView)view.findViewById(R.id.text)).setText(arguments.getString(ARG_TEXT));
        }

        if (arguments.containsKey(ARG_IMAGE_URL)) {
            ImageView imageView = (ImageView) view.findViewById(R.id.image);
            String urlString = arguments.getString(ARG_IMAGE_URL);
            if (imageView != null && urlString != null) {
                Picasso.with(imageView.getContext()).load(urlString).into(imageView);
            }
        }

        if (arguments.containsKey(ARG_LINK_URL)) {
            Uri uri = Uri.parse(arguments.getString(ARG_LINK_URL));
            shareIntent = new Intent(Intent.ACTION_SEND).setType("text/plain");
            if (title != null) shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, uri.toString());
            openInBrowserIntent = new Intent(Intent.ACTION_VIEW, uri);
        }

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.item_view_fragment, menu);

        MenuItem item = menu.findItem(R.id.share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

       if (shareIntent != null) shareActionProvider.setShareIntent(shareIntent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.open_in_browser) {
            if (openInBrowserIntent != null) startActivity(openInBrowserIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
