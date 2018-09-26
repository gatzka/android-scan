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

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.LinearLayout;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.Device;
import com.hbm.devices.scan.announce.IPEntry;
import com.hbm.devices.scan.announce.Interface;
import com.hbm.devices.scan.announce.ServiceEntry;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.LinkedList;
import java.util.List;

class DetailsFiller {

    private final DeviceDetailsActivity activity;
    private final Announce announce;
    private final LinearLayout deviceLayout;
    private final LinearLayout networkLayout;
    private final LinearLayout serviceLayout;

    DetailsFiller(Announce a, DeviceDetailsActivity act) {
        announce = a;
        activity = act;
        deviceLayout = activity.findViewById(R.id.device_container);
        networkLayout = activity.findViewById(R.id.network_container);
        serviceLayout = activity.findViewById(R.id.service_container);
    }

    final void addDeviceInformation() {
        final Device device = announce.getParams().getDevice();

        addDeviceUuid(device);
        addDeviceName(device);
        addDeviceType(device);
        addFamilyType(device);
        addHardwareId(device);
        addFirmwareVersion(device);
        addRouterInfo(device);

        addBottomMargin(deviceLayout);
    }

    final void addNetSettings() {
        final Interface anInterface = announce.getParams().getNetSettings().getInterface();

        addTextWithLabelNoSeparator(networkLayout, anInterface.getName(), activity.getString(R.string.interface_name));

        final String interfaceType = anInterface.getType();
        if (interfaceType != null && interfaceType.length() > 0) {
            addTextWithLabelTopSeparator(networkLayout, interfaceType, activity.getString(R.string.interface_type));
        }

        final String interfaceDescription = anInterface.getDescription();
        if (interfaceDescription != null && interfaceDescription.length() > 0) {
            addTextWithLabelTopSeparator(networkLayout, interfaceDescription,
                    activity.getString(R.string.interface_description));
        }

        addIPv4Entries(anInterface);
        addIPv6Entries(anInterface);

        addBottomMargin(networkLayout);
    }

    void addServices() {
        final List<ServiceEntry> services = announce.getParams().getServices();
        if (!services.isEmpty()) {
            for (final ServiceEntry entry : services) {
                addTextNoSeparator(serviceLayout, entry.getType() + ": " + entry.getPort());
            }
            addBottomMargin(serviceLayout);
        }
    }

    @NonNull
    private static List<IPEntry> getIPv6Adresses(List<IPEntry> list, @NonNull Class<?> cls) {
        List<IPEntry> adresses = new LinkedList<>();
        for (IPEntry entry : list) {
            if (cls.isInstance(entry.getAddress())) {
                adresses.add(entry);
            }
        }

        return adresses;
    }

    private void addIPv4Entries(final Interface anInterface) {
        final List<IPEntry> addresses = anInterface.getIPList();
        final List<IPEntry> ipv4Address = getIPv6Adresses(addresses, Inet4Address.class);
        if (!ipv4Address.isEmpty()) {
            addRule(networkLayout);
            for (final IPEntry entry : ipv4Address) {
                addTextNoSeparator(networkLayout, entry.getAddress().getHostAddress() + '/' + entry.getPrefix());
            }
            addLabel(networkLayout, activity.getString(R.string.ipv4_addresses));
        }
    }

    private void addIPv6Entries(final Interface anInterface) {
        final List<IPEntry> addresses = anInterface.getIPList();
        final List<IPEntry> ipv6Address = getIPv6Adresses(addresses, Inet6Address.class);
        if (!ipv6Address.isEmpty()) {
            addRule(networkLayout);
            for (final IPEntry entry : ipv6Address) {
                addTextNoSeparator(networkLayout, entry.getAddress().getHostAddress().replaceFirst("(^|:)(0+(:|$)){2,8}", "::") + '/' +
                        entry.getPrefix());
            }
            addLabel(networkLayout, activity.getString(R.string.ipv6_addresses));
        }
    }

    private void addDeviceUuid(Device device) {
        addTextWithLabelNoSeparator(deviceLayout, device.getUuid(), activity.getString(R.string.device_uuid));
    }

    private void addDeviceName(Device device) {
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0) {
            addTextWithLabelTopSeparator(deviceLayout, deviceName, activity.getString(R.string.device_name));
        }
    }

    private void addDeviceType(Device device) {
        final String type = device.getType();
        if (type != null && type.length() > 0) {
            addTextWithLabelTopSeparator(deviceLayout, type, activity.getString(R.string.device_type));
        }
    }

    private void addFamilyType(Device device) {
        final String familyType = device.getFamilyType();
        if (familyType != null && familyType.length() > 0) {
            addTextWithLabelTopSeparator(deviceLayout, familyType, activity.getString(R.string.device_family_type));
        }
    }

    private void addHardwareId(Device device) {
        final String hardwareID = device.getHardwareId();
        if (hardwareID != null && hardwareID.length() > 0) {
            addTextWithLabelTopSeparator(deviceLayout, hardwareID, activity.getString(R.string.device_hardware_id));
        }
    }

    private void addFirmwareVersion(Device device) {
        final String firmwareVersion = device.getFirmwareVersion();
        if (firmwareVersion != null && firmwareVersion.length() > 0) {
            addTextWithLabelTopSeparator(deviceLayout, firmwareVersion,
                    activity.getString(R.string.device_firmware_version));
        }
    }

    private void addRouterInfo(Device device) {
        final boolean isRouter = device.isRouter();
        if (isRouter) {
            addTextWithLabelTopSeparator(deviceLayout,
                    activity.getString(R.string.device_is_router), activity.getString(R.string.device_router_info));
        } else {
            addTextWithLabelTopSeparator(deviceLayout, activity.getString(R.string.device_is_no_router),
                    activity.getString(R.string.device_router_info));
        }
    }

    private void addTextWithLabelTopSeparator(@NonNull LinearLayout layout, String text, String label) {
        addText(layout, text, label, true);
    }

    private void addTextWithLabelNoSeparator(@NonNull LinearLayout layout, String text, String label) {
        addText(layout, text, label, false);
    }

    private void addTextNoSeparator(@NonNull LinearLayout layout, String text) {
        addText(layout, text, null, false);
    }

    private void addText(@NonNull LinearLayout layout, String text, @Nullable String label, boolean withTopSeparator) {
        if (withTopSeparator) {
            addRule(layout);
        }

        final AppCompatTextView textView = new AppCompatTextView(activity);
        textView.setPadding(
                activity.getResources().getDimensionPixelSize(R.dimen.device_info_padding_left),
                activity.getResources().getDimensionPixelSize(R.dimen.device_info_padding_top),
                0, 0);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            textView.setTextAppearance(activity, R.style.DetailsTextView);
        } else {
            textView.setTextAppearance(R.style.DetailsTextView);
        }

        textView.setText(text);
        layout.addView(textView);

        if (label != null) {
            addLabel(layout, label);
        }
    }

    private void addBottomMargin(LinearLayout layout) {
        final View rule = new View(activity);
        final LinearLayout.LayoutParams viewLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                activity.getResources().getDimensionPixelSize(R.dimen.rule_height));
        viewLp.setMargins(
                activity.getResources().getDimensionPixelSize(R.dimen.device_info_padding_left),
                activity.getResources().getDimensionPixelSize(R.dimen.device_info_padding_top),
                0, 0);
        rule.setLayoutParams(viewLp);
        layout.addView(rule);
    }

    private void addLabel(@NonNull LinearLayout layout, String label) {
        final AppCompatTextView labelView = new AppCompatTextView(activity);
        labelView.setPadding(
                activity.getResources().getDimensionPixelSize(R.dimen.device_info_padding_left),
                activity.getResources().getDimensionPixelSize(R.dimen.device_info_padding_top),
                0, 0);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            labelView.setTextAppearance(activity, R.style.DetailsLabelView);
        } else {
            labelView.setTextAppearance(R.style.DetailsTextView);
        }

        labelView.setText(label);
        layout.addView(labelView);
    }

    private void addRule(LinearLayout layout) {
        final View rule = new View(activity);
        final LinearLayout.LayoutParams viewLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                activity.getResources().getDimensionPixelSize(R.dimen.rule_height));
        viewLp.setMargins(
                activity.getResources().getDimensionPixelSize(R.dimen.device_info_padding_left),
                activity.getResources().getDimensionPixelSize(R.dimen.device_info_padding_top),
                0, 0);
        rule.setLayoutParams(viewLp);
        rule.setBackgroundColor(ContextCompat.getColor(activity, R.color.details_horizontal_rule_color));
        layout.addView(rule);
    }
}
