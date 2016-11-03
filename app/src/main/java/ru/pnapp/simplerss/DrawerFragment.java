/*
 *  Copyright (c) 2016 P.N.Alekseev <pnaleks@gmail.com>
 */
package ru.pnapp.simplerss;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class DrawerFragment extends Fragment implements FragmentManager.OnBackStackChangedListener {
    /** Show the drawer on launch until the user manually expands it */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /** Helper component that ties the action bar to the navigation drawer */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;

    private View mContainerView;
    
    public DrawerFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.drawer_fragment, container, false);
        ListView v = (ListView) view.findViewById(R.id.list);
        v.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.closeDrawer(mContainerView);
                ((MainActivity)getActivity()).onDrawerItemSelected(position);
            }
        });
        v.setAdapter( ((MainActivity)getActivity()).drawerAdapter );
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);

        setHasOptionsMenu(true);

        mContainerView = activity.findViewById(R.id.navigation_drawer);

        mDrawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        final boolean openDrawerOnStart = (savedInstanceState == null) && !prefs.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                syncIndicator();
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (openDrawerOnStart) {
                    // Пользователь впервые самостоятельно открыл панель, отключаю автопоказ
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // Демонстрирую боковую панель "необученному" пользователю
        if (openDrawerOnStart) mDrawerLayout.openDrawer(mContainerView);

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post( new Runnable() { @Override public void run() { mDrawerToggle.syncState(); } } );

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onBackStackChanged() {
        syncIndicator();
    }

    private void syncIndicator() {
        int count = getFragmentManager().getBackStackEntryCount();
        mDrawerToggle.setDrawerIndicatorEnabled(count == 0);
    }

}
