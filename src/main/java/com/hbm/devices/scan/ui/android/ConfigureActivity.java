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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.DefaultGateway;
import com.hbm.devices.scan.announce.Device;
import com.hbm.devices.scan.announce.IPv4Entry;
import com.hbm.devices.scan.configure.ConfigurationCallback;
import com.hbm.devices.scan.configure.ConfigurationDevice;
import com.hbm.devices.scan.configure.ConfigurationInterface;
import com.hbm.devices.scan.configure.ConfigurationNetSettings;
import com.hbm.devices.scan.configure.ConfigurationDefaultGateway;
import com.hbm.devices.scan.configure.ConfigurationParams;
import com.hbm.devices.scan.configure.IPv4EntryManual;
import com.hbm.devices.scan.configure.Response;

import java.io.IOException;
import java.util.List;

public final class ConfigureActivity extends AppCompatActivity {

    private static final int CONFIGURATIION_TIMEOUT = 5000;
    private Announce announce;
    private ConfigServiceThread configThread;

    private static final InputFilter[] ipAddressFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configure_layout);
        try {
            this.configThread = new ConfigServiceThread();
            configThread.start();

            announce = (Announce) getIntent().getSerializableExtra(DeviceDetailsActivity.DETAILS);
            initToolbar(announce);

            setCurrentIp(announce);
            setCurrentGateway(announce);
            setEdit(false);

            EditText ipv4Address = (EditText) findViewById(R.id.configure_ip_address_edit);
            ipv4Address.setFilters(ipAddressFilter);
            EditText ipv4Mask = (EditText) findViewById(R.id.configure_subnet_edit);
            ipv4Mask.setFilters(ipAddressFilter);
            EditText gateway = (EditText) findViewById(R.id.configure_gateway_ip_edit);
            gateway.setFilters(ipAddressFilter);

            final Switch dhcpSwitch = (Switch) findViewById(R.id.dhcp_switch);
            dhcpSwitch.setChecked(true);
            dhcpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        setEdit(false);
                    } else {
                        setEdit(true);
                    }
                }
            });
            Button submit = (Button) findViewById(R.id.submit);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConfigurationInterface interfaceSettings;
                    String interfaceName = announce.getParams().getNetSettings().getInterface().getName();
                    if (dhcpSwitch.isChecked()) {
                        interfaceSettings = new ConfigurationInterface(interfaceName, ConfigurationInterface.Method.DHCP);
                    } else {
                        IPv4EntryManual ipv4Manual = getManualConfiguration();
                        interfaceSettings = new ConfigurationInterface(interfaceName, ConfigurationInterface.Method.MANUAL, ipv4Manual);
                    }
                    ConfigurationDevice device = new ConfigurationDevice(announce.getParams().getDevice().getUuid());

                    ConfigurationDefaultGateway gateway = getDefaultGateway();
                    ConfigurationNetSettings netSettings;
                    if (gateway != null) {
                        netSettings = new ConfigurationNetSettings(interfaceSettings, gateway);
                    } else {
                        netSettings = new ConfigurationNetSettings(interfaceSettings);
                    }
                    ConfigurationParams params = new ConfigurationParams(device, netSettings);
                    sendConfiguration(v.getContext(), params);
                }
            });

        } catch (IOException e) {
            final Toast failureToast = Toast.makeText(this,
                    "Could not start configuration service!", Toast.LENGTH_SHORT);
            failureToast.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    private void sendConfiguration(final Context context, ConfigurationParams params) {
        ConfigurationCallback callback = new ConfigurationCallback() {
            @Override
            public void onSuccess(final Response response) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "Configuration successful", Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override
            public void onTimeout(long t) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "Configuration timeout", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(final Response response) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "Error: " + response.getError().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        };
        configThread.sendConfiguration(params, callback, CONFIGURATIION_TIMEOUT);
    }

    private ConfigurationDefaultGateway getDefaultGateway() {
        EditText gateway = (EditText) findViewById(R.id.configure_gateway_ip_edit);
        if (gateway.getText() != null && gateway.getText().length() > 0) {
            return new ConfigurationDefaultGateway(gateway.getText().toString());
        } else{
            return null;
        }
    }

    private IPv4EntryManual getManualConfiguration() {
        EditText ipv4Address = (EditText) findViewById(R.id.configure_ip_address_edit);
        String ip;
        if ((ipv4Address.getText() != null) && (ipv4Address.getText().length() > 0)) {
            ip = ipv4Address.getText().toString();
        } else {
            ip = "";
        }
        EditText ipv4Mask = (EditText) findViewById(R.id.configure_subnet_edit);

        String mask;
        if ((ipv4Mask.getText() != null) && (ipv4Mask.getText().length() > 0)) {
            mask = ipv4Mask.getText().toString();
        } else {
            mask = "";
        }
        return new IPv4EntryManual(ip, mask);
    }

    private void setEdit(boolean edit) {
        EditText ipv4 = (EditText) findViewById(R.id.configure_ip_address_edit);
        ipv4.setEnabled(edit);
        if (edit) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(ipv4, InputMethodManager.SHOW_IMPLICIT);
        }
        EditText ipv4Mask = (EditText) findViewById(R.id.configure_subnet_edit);
        ipv4Mask.setEnabled(edit);
        EditText gateway = (EditText) findViewById(R.id.configure_gateway_ip_edit);
        gateway.setEnabled(edit);
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

    static {
        ipAddressFilter = new InputFilter[1];
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (end > start) {
                    String destTxt = dest.toString();
                    String resultingTxt = destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend);
                    if (!resultingTxt.matches ("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                        return "";
                    } else {
                        String[] splits = resultingTxt.split("\\.");
                        for (int i=0; i<splits.length; i++) {
                            if (Integer.valueOf(splits[i]) > 255) {
                                return "";
                            }
                        }
                    }
                }
                return null;
            }
        };
        ipAddressFilter[0] = filter;
    }
}
