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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.Device;
import com.hbm.devices.scan.announce.IPv4Entry;
import com.hbm.devices.scan.announce.IPv6Entry;
import com.hbm.devices.scan.announce.Interface;
import com.hbm.devices.scan.announce.ServiceEntry;

import java.util.List;

class DetailsFiller {

    private final DeviceDetailsActivity activity;
    private final Announce announce;

    DetailsFiller(Announce a, DeviceDetailsActivity act) {
        announce = a;
        activity = act;
    }

    void addDeviceInformation() {
        final LinearLayout layout = (LinearLayout) activity.findViewById(R.id.device_container);

        final Device device = announce.getParams().getDevice();
        addTextWithLabelNoSeparator(layout, device.getUuid(), activity.getString(R.string.device_uuid));

        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0) {
            addTextWithLabelTopSeparator(layout, deviceName, activity.getString(R.string.device_name));
        }

        final String type = device.getType();
        if (type != null && type.length() > 0) {
            addTextWithLabelTopSeparator(layout, type, activity.getString(R.string.device_type));
        }

        final String familyType = device.getFamilyType();
        if (familyType != null && familyType.length() > 0) {
            addTextWithLabelTopSeparator(layout, familyType, activity.getString(R.string.device_family_type));
        }

        final String hardwareID = device.getHardwareId();
        if (hardwareID != null && hardwareID.length() > 0) {
            addTextWithLabelTopSeparator(layout, hardwareID, activity.getString(R.string.device_hardware_id));
        }

        final String firmwareVersion = device.getFirmwareVersion();
        if (firmwareVersion != null && firmwareVersion.length() > 0) {
            addTextWithLabelTopSeparator(layout, firmwareVersion, activity.getString(R.string.device_firmware_version));
        }

        final boolean isRouter = device.isRouter();
        if (isRouter) {
            addTextWithLabelTopSeparator(layout,
                    activity.getString(R.string.device_is_router), activity.getString(R.string.device_router_info));
        } else {
            addTextWithLabelTopSeparator(layout, activity.getString(R.string.device_is_no_router),
                    activity.getString(R.string.device_router_info));
        }

        addBottomMargin(layout);
    }

    void addNetSettings() {
        final LinearLayout layout = (LinearLayout) activity.findViewById(R.id.network_container);
        final Interface anInterface = announce.getParams().getNetSettings().getInterface();

        addTextWithLabelNoSeparator(layout, anInterface.getName(), activity.getString(R.string.interface_name));

        final String interfaceType = anInterface.getType();
        if (interfaceType != null && interfaceType.length() > 0) {
            addTextWithLabelTopSeparator(layout, interfaceType, activity.getString(R.string.interface_type));
        }

        final String interfaceDescription = anInterface.getDescription();
        if (interfaceDescription != null && interfaceDescription.length() > 0) {
            addTextWithLabelTopSeparator(layout, interfaceDescription,
                    activity.getString(R.string.interface_description));
        }

        final List<IPv4Entry> ipv4Address = anInterface.getIPv4();
        if (!ipv4Address.isEmpty()) {
            addRule(layout);
            for (final IPv4Entry entry : ipv4Address) {
                addTextNoSeparator(layout, entry.getAddress() + '/' + entry.getNetmask());
            }
            addLabel(layout, activity.getString(R.string.ipv4_addresses));
        }

        final List<IPv6Entry> ipv6Address = anInterface.getIPv6();
        if (!ipv6Address.isEmpty()) {
            addRule(layout);
            for (final IPv6Entry entry : ipv6Address) {
                addTextNoSeparator(layout, entry.getAddress().replaceFirst("(^|:)(0+(:|$)){2,8}", "::") + '/' +
                        entry.getPrefix());
            }
            addLabel(layout, activity.getString(R.string.ipv6_addresses));
        }

        addBottomMargin(layout);
    }

    void addServices() {
        final List<ServiceEntry> services = announce.getParams().getServices();
        if (!services.isEmpty()) {
            final CardView card = new CardView(activity);

            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(
                    activity.getResources().getDimensionPixelSize(R.dimen.details_card_margin_start),
                    activity.getResources().getDimensionPixelSize(R.dimen.details_card_margin_top),
                    activity.getResources().getDimensionPixelSize(R.dimen.details_card_margin_end),
                    activity.getResources().getDimensionPixelSize(R.dimen.details_card_margin_bottom));
            card.setLayoutParams(params);
            final LinearLayout cardContainer = (LinearLayout) activity.findViewById(R.id.card_container);
            if (cardContainer != null) {
                cardContainer.addView(card);
            }

            final LinearLayout layout = new LinearLayout(activity);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            card.addView(layout);

            final AppCompatTextView textView = new AppCompatTextView(activity);
            textView.setPadding(
                    activity.getResources().getDimensionPixelSize(R.dimen.details_headings_padding),
                    activity.getResources().getDimensionPixelSize(R.dimen.details_headings_padding),
                    activity.getResources().getDimensionPixelSize(R.dimen.details_headings_padding),
                    activity.getResources().getDimensionPixelSize(R.dimen.details_headings_padding));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                textView.setTextAppearance(activity, R.style.DetailsHeading);
            } else {
                textView.setTextAppearance(R.style.DetailsHeading);
            }
            textView.setTextIsSelectable(false);
            textView.setMaxLines(1);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setText(activity.getString(R.string.services));
            layout.addView(textView);

            final View rule = new View(activity);
            rule.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    activity.getResources().getDimensionPixelSize(R.dimen.ruler_height)));
            rule.setBackgroundColor(ContextCompat.getColor(activity, R.color.details_horizontal_rule_color));
            layout.addView(rule);

            for (final ServiceEntry entry : services) {
                addTextNoSeparator(layout, entry.getType() + ": " + entry.getPort());
            }
            addBottomMargin(layout);
        }
    }

    private void addTextWithLabelTopSeparator(LinearLayout layout, String text, String label) {
        addText(layout, text, label, true);
    }

    private void addTextWithLabelNoSeparator(LinearLayout layout, String text, String label) {
        addText(layout, text, label, false);
    }

    private void addTextNoSeparator(LinearLayout layout, String text) {
        addText(layout, text, null, false);
    }

    private void addText(LinearLayout layout, String text, String label, boolean withTopSeparator) {
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

    private void addLabel(LinearLayout layout, String label) {
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
