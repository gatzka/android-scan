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

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Main activity for the scan app.
 */
public final class ScanActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {
    public static final String LOG_TAG = "Scanner";
    
    private static final int TOAST_TIMEOUT = 2000;
    private boolean doubleBackToExitPressedOnce;
    private DeviceListFragment listFragment;
    private static final String TAG_DEVICE_LIST_FRAGMENT = "deviceListFragment";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        doubleBackToExitPressedOnce = false;

        setContentView(R.layout.device_scan);
        final FragmentManager manager = getSupportFragmentManager();
        manager.addOnBackStackChangedListener(this);
        getActionBar().setDisplayShowTitleEnabled(true);
        shouldDisplayHomeUp();

        listFragment = (DeviceListFragment) manager.findFragmentByTag(TAG_DEVICE_LIST_FRAGMENT);
        if (listFragment == null) {
            final FragmentTransaction transaction = manager.beginTransaction();
            listFragment = new DeviceListFragment();
            transaction.add(listFragment, TAG_DEVICE_LIST_FRAGMENT);
            transaction.commit();
        }

        if (savedInstanceState == null) {
            /*
             * Activity was created for the first time. So we need to
             * instantiate all fragments. If the activity was restarted
             * due to a reconfiguration, savedInstanceState != null.
             */
            if (findViewById(R.id.fragment_container) != null) {
                final DevicesFragment devicesFragment = new DevicesFragment();
                manager.beginTransaction().replace(R.id.fragment_container, devicesFragment).commit();
            }
        }
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    @Override
    public void onDestroy() {
        listFragment = null;
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
        final FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            manager.popBackStack();
        } else {
            if (doubleBackToExitPressedOnce) {
                if (listFragment != null) {
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            final FragmentManager manager = getSupportFragmentManager();
            manager.popBackStack();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void shouldDisplayHomeUp() {
        final boolean canback = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getActionBar().setDisplayHomeAsUpEnabled(canback);
    }
}
