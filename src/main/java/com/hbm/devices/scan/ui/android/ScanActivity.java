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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

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

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        doubleBackToExitPressedOnce = false;

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
    }

    @Override
    public void onDestroy() {
        listFragment = null;
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
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
        ModuleListAdapter adapter = new ModuleListAdapter();
        //adapter.setOnItemClickListener(this);
        devicesView.setAdapter(adapter);
        listFragment.setAdapter(adapter);
    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        setTitle(getString(R.string.app_name));

        // if (actionBar != null) {
        //     actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        //     actionBar.setDisplayHomeAsUpEnabled(true);
        // }
    }
}
