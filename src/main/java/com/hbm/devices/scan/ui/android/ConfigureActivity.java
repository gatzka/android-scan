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
import android.text.InputFilter;
import android.text.Spanned;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.Device;
import com.hbm.devices.scan.configure.ConfigurationParams;

import java.io.IOException;

public final class ConfigureActivity extends AppCompatActivity {

    static final int CONFIGURATION_TIMEOUT = 5000;
    private ConfigServiceThread configThread;

    private static final InputFilter[] ipAddressFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configure_layout);
        try {
            this.configThread = new ConfigServiceThread();
            configThread.start();

            final Announce announce = (Announce) getIntent().getSerializableExtra(DeviceDetailsActivity.DETAILS);
            initToolbar(announce);

            final EditText ipv4Address = (EditText) findViewById(R.id.configure_ip_address_edit);
            if (ipv4Address != null) {
                ipv4Address.setFilters(ipAddressFilter);
            }
            final EditText ipv4Mask = (EditText) findViewById(R.id.configure_subnet_edit);
            if (ipv4Mask != null) {
                ipv4Mask.setFilters(ipAddressFilter);
            }
            final EditText gateway = (EditText) findViewById(R.id.configure_gateway_ip_edit);
            if (gateway != null) {
                gateway.setFilters(ipAddressFilter);
            }

            final ConfigurationSubmitter submitter = new ConfigurationSubmitter(announce, this);

            final Switch dhcpSwitch = (Switch) findViewById(R.id.dhcp_switch);
            if (dhcpSwitch != null) {
                dhcpSwitch.setChecked(true);
                dhcpSwitch.setOnCheckedChangeListener(submitter);
            }

            final Button submit = (Button) findViewById(R.id.submit);
            if (submit != null) {
                submit.setOnClickListener(submitter);
            }
        } catch (IOException e) {
            final Toast failureToast = Toast.makeText(this,
                    "Could not start configuration service!", Toast.LENGTH_SHORT);
            failureToast.show();
        }
    }

    void sendConfiguration(ConfigurationParams params) {
        configThread.sendConfiguration(params, this);
    }

    @Override
    public void onDestroy() {
        configThread.kill();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
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
        final StringBuilder title = new StringBuilder(getResources().getText(R.string.configure));
        String displayName = device.getName();
        if (displayName == null || displayName.length() == 0) {
            displayName = device.getUuid();
        }
        title.append(' ').append(displayName);
        return title.toString();
    }

    static {
        ipAddressFilter = new InputFilter[1];
        final InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (end > start) {
                    String destTxt = dest.toString();
                    String resultingTxt = destTxt.substring(0, dstart) +
                            source.subSequence(start, end) +
                            destTxt.substring(dend);
                    
                    if (resultingTxt.matches ("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                        String[] splits = resultingTxt.split("\\.");
                        for (String split : splits) {
                            if (Integer.valueOf(split) > 255) {
                                return "";
                            }
                        }
                    } else {
                        return "";
                    }
                }
                return null;
            }
        };
        ipAddressFilter[0] = filter;
    }
}

