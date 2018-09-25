/*
 * Android Scan, an app for scanning and configuring HBM devices.
 *
 * The MIT License (MIT)
 *
 * Copyright (C) Stephan Gatzka
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hbm.devices.scan.ui.android;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.hbm.devices.scan.announce.Announce;

import java.io.File;
import java.util.List;

/**
 * Main activity for the scan app.
 */
public final class ScanActivity extends AppCompatActivity {

    private static final int TOAST_TIMEOUT = 2000;
    protected boolean doubleBackToExitPressedOnce;
    private DeviceListFragment listFragment;
    private static final String TAG_DEVICE_LIST_FRAGMENT = "deviceListFragment";
    private RecyclerView devicesView;
    protected ModuleListAdapter adapter;
    private ScanDrawer drawer;

    /**
     * Default constructor fot this Activity. This constructor shall never be called directly from code. Activities
     * are instantiated by the Android runtime..
     */
    public ScanActivity() {
        super();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        doubleBackToExitPressedOnce = false;
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.device_scan);

        final FragmentManager manager = getSupportFragmentManager();
        listFragment = (DeviceListFragment) manager.findFragmentByTag(TAG_DEVICE_LIST_FRAGMENT);
        if (listFragment == null) {
            final FragmentTransaction transaction = manager.beginTransaction();
            listFragment = new DeviceListFragment();
            transaction.add(listFragment, TAG_DEVICE_LIST_FRAGMENT);
            transaction.commit();
        }

        initDevicesView();
        initToolbar();
        setDeviceListAdapter();
        drawer = new ScanDrawer(this);

        if (!kernelSupportsMulticast()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.no_multicast)
                    .setMessage(R.string.no_multicast_msg)
                    .setNeutralButton(android.R.string.ok, null)
                    .setIcon(R.drawable.ic_alert_circle_outline_black_36dp)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.device_list_actions, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                searchView.clearFocus();
                adapter.setFilterString(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                adapter.setFilterString(newText);
                return true;
            }
        });
        adapter.setFilterString(null);

        final MenuItem pauseItem = menu.findItem(R.id.action_pause_control);
        if (adapter.isPaused()) {
            pauseItem.setIcon(R.drawable.ic_action_play);
        } else {
            pauseItem.setIcon(R.drawable.ic_action_pause);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_pause_control:
                handlePause(item);
                return true;

            case R.id.action_share:
                final List<Announce> announces = adapter.getFilteredAnnounces();
                final AnnounceSharer sharer = new AnnounceSharer(this);
                sharer.handleShare(announces);
                return true;

            case android.R.id.home:
                drawer.open();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        listFragment = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isOpen()) {
            drawer.close();
            return;
        }
        if (doubleBackToExitPressedOnce) {
            if (listFragment != null) {
                final FragmentManager manager = getSupportFragmentManager();
                final FragmentTransaction transaction = manager.beginTransaction();
                transaction.remove(listFragment);
                transaction.commit();
            }
            super.onBackPressed();
            return;
        }

        doubleBackToExitPressedOnce = true;
        final Toast exitToast = Toast.makeText(this, R.string.toast_exit, Toast.LENGTH_SHORT);
        exitToast.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, TOAST_TIMEOUT);
    }

    private void initDevicesView() {
        devicesView = findViewById(R.id.devicesView);
        devicesView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setDeviceListAdapter() {
        adapter = new ModuleListAdapter(listFragment);
        devicesView.setAdapter(adapter);
        listFragment.setAdapter(adapter);
    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        setTitle(getString(R.string.app_name));

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void handlePause(MenuItem item) {
        if (adapter.isPaused()) {
            item.setIcon(R.drawable.ic_action_pause);
            adapter.resumeDeviceUpdates();
        } else {
            item.setIcon(R.drawable.ic_action_play);
            adapter.pauseDeviceUpdates();
        }
    }

    private static boolean kernelSupportsMulticast() {
        final File igmp = new File("/proc/net/", "igmp");
        return igmp.exists();
    }
}
