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
import android.text.Editable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.configure.ConfigurationDefaultGateway;
import com.hbm.devices.scan.configure.ConfigurationDevice;
import com.hbm.devices.scan.configure.ConfigurationInterface;
import com.hbm.devices.scan.configure.ConfigurationNetSettings;
import com.hbm.devices.scan.configure.ConfigurationParams;
import com.hbm.devices.scan.configure.IPv4EntryManual;

class ConfigurationSubmitter implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private boolean dhcpEnabled;
    private final Announce announce;
    private final ConfigureActivity activity;

    ConfigurationSubmitter(Announce a, ConfigureActivity act) {
        announce = a;
        activity = act;

        setEdit(false);
    }

    @Override
    public void onClick(View v) {
        ConfigurationInterface interfaceSettings;
        final String interfaceName = announce.getParams().getNetSettings().getInterface().getName();
        if (dhcpEnabled) {
            interfaceSettings = new ConfigurationInterface(interfaceName, ConfigurationInterface.Method.DHCP);
        } else {
            final IPv4EntryManual ipv4Manual = getManualConfiguration();
            if (ipv4Manual == null) {
                return;
            }
            interfaceSettings = new ConfigurationInterface(interfaceName,
                    ConfigurationInterface.Method.MANUAL, ipv4Manual);
        }
        final ConfigurationDevice device = new ConfigurationDevice(announce.getParams().getDevice().getUuid());

        final ConfigurationDefaultGateway gateway = getDefaultGateway();
        ConfigurationNetSettings netSettings;
        if (gateway == null) {
            netSettings = new ConfigurationNetSettings(interfaceSettings);
        } else {
            netSettings = new ConfigurationNetSettings(interfaceSettings, gateway);
        }
        final ConfigurationParams params = new ConfigurationParams(device, netSettings);
        activity.sendConfiguration(params);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            dhcpEnabled = true;
            setEdit(false);
        } else {
            dhcpEnabled = false;
            setEdit(true);
        }
    }

    private IPv4EntryManual getManualConfiguration() {
        final EditText ipv4Address = (EditText) activity.findViewById(R.id.configure_ip_address_edit);
        String ip = null;
        if (ipv4Address != null) {
            final Editable ipv4Text = ipv4Address.getText();
            if (ipv4Text.length() > 0) {
                ip = ipv4Text.toString();
            }
        }

        final EditText ipv4Mask = (EditText) activity.findViewById(R.id.configure_subnet_edit);
        String mask = null;
        if (ipv4Mask != null) {
            final Editable ipv4MaskText = ipv4Mask.getText();
            if (ipv4MaskText.length() > 0) {
                mask = ipv4MaskText.toString();
            }
        }
        if ((ip == null) || (mask == null)) {
            return null;
        } else {
            return new IPv4EntryManual(ip, mask);
        }
    }

    private ConfigurationDefaultGateway getDefaultGateway() {
        final EditText gateway = (EditText) activity.findViewById(R.id.configure_gateway_ip_edit);
        if (gateway == null) {
            return null;
        } else {
            final Editable gatewayText = gateway.getText();
            if (gatewayText.length() > 0) {
                return new ConfigurationDefaultGateway(gatewayText.toString());
            } else {
                return null;
            }
        }
    }

    private void setEdit(boolean edit) {
        final EditText ipv4 = (EditText) activity.findViewById(R.id.configure_ip_address_edit);
        if (ipv4 != null) {
            ipv4.setEnabled(edit);
            if (edit) {
                final InputMethodManager imm =
                        (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(ipv4, InputMethodManager.SHOW_IMPLICIT);
            }
            final EditText ipv4Mask = (EditText) activity.findViewById(R.id.configure_subnet_edit);
            if (ipv4Mask != null) {
                ipv4Mask.setEnabled(edit);
            }
            final EditText gateway = (EditText) activity.findViewById(R.id.configure_gateway_ip_edit);
            if (gateway != null) {
                gateway.setEnabled(edit);
            }
        }
    }
}
