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
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.Device;

public final class DeviceDetailsActivity extends AbstractToolbarActivity {

    public static final String DETAILS = "Details";

    private Announce announce;

    public DeviceDetailsActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_details);

        announce = (Announce) getIntent().getSerializableExtra(DeviceViewHolder.DETAILS);
        initToolbar(announce);

        final DetailsFiller filler = new DetailsFiller(announce, this);
        filler.addDeviceInformation();
        filler.addNetSettings();
        filler.addServices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.device_details_actions, menu);
        return true;
    }

    @Override
    String getDisplayName(Device device) {
        String displayName = device.getName();
        if (displayName == null || displayName.length() == 0) {
            displayName = device.getUuid();
        }
        return displayName;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_share:
                final AnnounceSharer sharer = new AnnounceSharer(this);
                sharer.handleShare(announce);
                return true;
            case R.id.action_setup:
                final Intent intent = new Intent(this, ConfigureActivity.class);
                intent.putExtra(DETAILS, announce);
                ActivityCompat.startActivity(this, intent, null);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                return true;
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
