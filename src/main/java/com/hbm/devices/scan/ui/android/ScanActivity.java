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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import com.hbm.devices.scan.announce.Announce;

/**
 * Main activity for the scan app.
 */
public final class ScanActivity extends AppCompatActivity {
    public static final String LOG_TAG = "Scanner";
    
    private static final int TOAST_TIMEOUT = 2000;
    private boolean doubleBackToExitPressedOnce;
    private DeviceListFragment listFragment;
    private static final String TAG_DEVICE_LIST_FRAGMENT = "deviceListFragment";
    private RecyclerView devicesView;
    private ModuleListAdapter adapter;
	private DrawerLayout drawerLayout;
	private View content;

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
        setupDrawerLayout();

        if (!kernelSupportsMulticast()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.no_multicast)
                    .setMessage(R.string.no_multicast_msg)
                    .setNeutralButton(android.R.string.ok, null)
                    .setIcon(R.drawable.ic_alert_circle_outline_black_36dp)
                    .show();
        }

        content = findViewById(R.id.content);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.device_list_actions, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
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
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_pause_control:
                handlePause(item);
                return true;

            case R.id.action_share:
                List<Announce> announces = adapter.getFilteredAnnounces();
                handleShare(announces);
                return true;

            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
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
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
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
        devicesView = (RecyclerView) findViewById(R.id.devicesView);
        devicesView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setDeviceListAdapter() {
        adapter = new ModuleListAdapter(listFragment);
        devicesView.setAdapter(adapter);
        listFragment.setAdapter(adapter);
    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

    private void handleShare(List<Announce> announces) {
        final Uri uri = DeviceZipper.saveAnnounces(announces, this);
        if (uri != null) {
            Intent devices = new Intent();
            devices.setAction(Intent.ACTION_SEND);
            devices.putExtra(Intent.EXTRA_STREAM, uri);
            devices.setTypeAndNormalize("application/zip");
            startActivity(Intent.createChooser(devices, getResources().getText(R.string.share_devices)));
        } else {
            final Toast exitToast = Toast.makeText(this, R.string.create_devices_file_error, Toast.LENGTH_SHORT);
            exitToast.show();
        }
    }

    private void setupDrawerLayout() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
        if (view != null) {
            view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.drawer_settings:
                            startActivity(new Intent(getApplicationContext(),
                                    SettingsActivity.class));
                            return true;

                        case R.id.drawer_about:
                            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                            return true;

                        default:
                            drawerLayout.closeDrawers();
                            return true;
                    }
                }
            });

            View headerView = view.getHeaderView(0);
            if (headerView != null) {
                ImageView avatar = (ImageView) headerView.findViewById(R.id.avatar);
                if (avatar != null) {
                    avatar.setPadding(0, getStatusBarHeight(), 0, 0);
                }
            }
        }
    }

    private int getStatusBarHeight() { 
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        } 
        return result;
    }

    private boolean kernelSupportsMulticast() {
        File igmp = new File("/proc/net/", "igmp");
        return igmp.exists();
    }
}
