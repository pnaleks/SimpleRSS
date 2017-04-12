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
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

public class ChannelViewFragment extends Fragment {
    private static final String LAYOUT_MANAGER_STATE = "layout_manager_state";

    LinearLayoutManager layoutManager;
    Parcelable layoutManagerState;

    public static ChannelViewFragment newInstance() {
        ChannelViewFragment fragment = new ChannelViewFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(LAYOUT_MANAGER_STATE)) {
            layoutManagerState = savedInstanceState.getParcelable(LAYOUT_MANAGER_STATE);
        }
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();

        layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);

        RecyclerView view = new RecyclerView(getActivity());
        view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey_500));
        view.setLayoutManager(layoutManager);
        view.setAdapter(activity.channelViewAdapter);

        SwipeRefreshLayout swipeRefreshLayout = new SwipeRefreshLayout(getActivity());
        swipeRefreshLayout.addView(view);



        activity.channelViewAdapter.setSwipeRefreshLayout(swipeRefreshLayout);

        return swipeRefreshLayout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.channel_view_fragment, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (layoutManager != null && layoutManagerState != null) {
            View view = getView();
            if (view != null) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        layoutManager.onRestoreInstanceState(layoutManagerState);
                    }
                });
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (layoutManager != null) layoutManagerState = layoutManager.onSaveInstanceState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainActivity activity = (MainActivity) getActivity();
        activity.channelViewAdapter.setSwipeRefreshLayout(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LAYOUT_MANAGER_STATE, layoutManagerState);
    }
}
