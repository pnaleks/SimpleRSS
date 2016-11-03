package ru.pnapp.simplerss;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ChannelViewFragment extends Fragment {
    private static final String LAYOUT_MANAGER_STATE = "layout_manager_state";

    LinearLayoutManager layoutManager;

    public static ChannelViewFragment newInstance() {
        ChannelViewFragment fragment = new ChannelViewFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();

        layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);

        RecyclerView view = new RecyclerView(getActivity());
        view.setLayoutManager(layoutManager);
        view.setAdapter( activity.channelViewAdapter );

        if (savedInstanceState != null && !getArguments().containsKey(LAYOUT_MANAGER_STATE)) {
            getArguments().putParcelable(LAYOUT_MANAGER_STATE, savedInstanceState.getParcelable(LAYOUT_MANAGER_STATE));
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null) {
            view.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            layoutManager.onRestoreInstanceState(getArguments().getParcelable(LAYOUT_MANAGER_STATE));
                        }
                    }
            );
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getArguments().putParcelable(LAYOUT_MANAGER_STATE, layoutManager.onSaveInstanceState());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LAYOUT_MANAGER_STATE, layoutManager.onSaveInstanceState());
    }
}
