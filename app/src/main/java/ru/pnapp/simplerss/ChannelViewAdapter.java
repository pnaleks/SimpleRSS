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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;

class ChannelViewAdapter extends RecyclerView.Adapter<ViewHolder> implements
        Observer,
        View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private MainActivity activity;
    private RssPresenter rssPresenter;
    private SwipeRefreshLayout swipeRefreshLayout;

    ChannelViewAdapter(MainActivity activity) {this.activity = activity;}

    void setSwipeRefreshLayout(SwipeRefreshLayout layout) {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setOnRefreshListener(null);
        if (layout != null) layout.setOnRefreshListener(this);
        swipeRefreshLayout = layout;
    }

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
        if (o == null) {
            rssPresenter = (RssPresenter) observable;
            activity.setTitle(rssPresenter.getTitle());
            notifyDataSetChanged();
        } else {
            Toast.makeText(activity, "Error: " + o, Toast.LENGTH_LONG).show();
        }
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        rssPresenter.getFeed(rssPresenter.getUrlString());
    }

    void setRefreshing(boolean refreshing) {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(refreshing);
    }
}
