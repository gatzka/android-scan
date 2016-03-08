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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.DefaultGateway;
import com.hbm.devices.scan.announce.Device;
import com.hbm.devices.scan.announce.IPv4Entry;

import java.util.List;

public final class ConfigureActivity extends AppCompatActivity {

    private Announce announce;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configure_layout);

        announce = (Announce) getIntent().getSerializableExtra(DeviceDetailsActivity.DETAILS);
    	initToolbar(announce);

        setCurrentIp(announce);
        setCurrentGateway(announce);
    }

    private void setCurrentIp(Announce announce) {
        List<IPv4Entry> ipv4Addresses = announce.getParams().getNetSettings().getInterface().getIPv4();
        if (!ipv4Addresses.isEmpty()) {
            IPv4Entry entry = ipv4Addresses.get(0);
            TextView currentIp = (TextView) findViewById(R.id.configure_current_ip);
            currentIp.setText(entry.getAddress());
            TextView currentNetMask = (TextView) findViewById(R.id.configure_current_subnetmask);
            currentNetMask.setText(entry.getNetmask());
        }
    }

    private void setCurrentGateway(Announce announce) {
        DefaultGateway gateway = announce.getParams().getNetSettings().getDefaultGateway();
        if (gateway != null) {
            String ipv4Gateway = gateway.getIpv4Address();
            if (ipv4Gateway != null) {
                TextView currentGateway = (TextView) findViewById(R.id.configure_current_gateway);
                currentGateway.setText(ipv4Gateway);
            }
        }
    }

    private void initToolbar(Announce announce) {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle(getDisplayName(announce.getParams().getDevice()));
    }

   	private String getDisplayName(Device device) {
        StringBuilder title = new StringBuilder(getResources().getText(R.string.configure));
        String displayName = device.getName();
        if (displayName == null || displayName.length() == 0) {
            displayName = device.getUuid();
        }
        title.append(" ");
        title.append(displayName);
        return title.toString();
    }
}
