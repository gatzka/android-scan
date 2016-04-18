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
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

class ScanDrawer {
    private final DrawerLayout drawerLayout;

    ScanDrawer(final AppCompatActivity act) {

        drawerLayout = (DrawerLayout) act.findViewById(R.id.drawer_layout);

        final NavigationView view = (NavigationView) act.findViewById(R.id.navigation_view);
        if (view != null) {
            view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.drawer_settings:
                            act.startActivity(new Intent(act.getApplicationContext(),
                                    SettingsActivity.class));
                            return true;

                        case R.id.drawer_about:
                            act.startActivity(new Intent(act.getApplicationContext(), AboutActivity.class));
                            return true;

                        default:
                            drawerLayout.closeDrawers();
                            return true;
                    }
                }
            });

            final View headerView = view.getHeaderView(0);
            if (headerView != null) {
                final ImageView avatar = (ImageView) headerView.findViewById(R.id.avatar);
                if (avatar != null) {
                    avatar.setPadding(0, getStatusBarHeight(act), 0, 0);
                }
            }
        }
    }

    boolean isOpen() {
        return drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    void open() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    void close() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private int getStatusBarHeight(final AppCompatActivity activity) {
        int result = 0;
        final int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
