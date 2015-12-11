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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils.TruncateAt;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.Device;
import com.hbm.devices.scan.announce.Interface;
import com.hbm.devices.scan.announce.IPv4Entry;
import com.hbm.devices.scan.announce.IPv6Entry;
import com.hbm.devices.scan.announce.ServiceEntry;

public final class DeviceDetailsActivity extends AppCompatActivity {

    private Announce announce;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_details);

        announce = (Announce) getIntent().getSerializableExtra(DeviceViewHolder.DETAILS);
    	initToolbar(announce);

        addDeviceInformation();
        addNetSettings();
        addServices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.device_details_actions, menu);
		return true;
    }

   	private String getDisplayName(Device device) {
        String displayName = device.getName();
        if (displayName == null || displayName.length() == 0) {
            displayName = device.getUuid();
        }
        return displayName;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_share) {
            handleShare(announce);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleShare(Announce announce) {
        final Uri uri = DeviceZipper.saveAnnounce(announce, this);
        if (uri != null) {
            Intent devices = new Intent();
            devices.setAction(Intent.ACTION_SEND);
            devices.putExtra(Intent.EXTRA_STREAM, uri);
            devices.setTypeAndNormalize("application/zip");
            startActivity(Intent.createChooser(devices, getResources().getText(R.string.share_devices)));
        } else {
            final Toast exitToast = Toast.makeText(this, R.string.create_devices_file_error, Toast.LENGTH_SHORT);
            exitToast.show();
        }
    }

    private void addDeviceInformation() {
        final LinearLayout layout = (LinearLayout) findViewById(R.id.device_container);

        final Device device = announce.getParams().getDevice();
        addTextWithLabelNoSeparator(layout, device.getUuid(), getString(R.string.device_uuid));

        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0) {
            addTextWithLabelTopSeparator(layout, deviceName, getString(R.string.device_name));
        }

        final String type = device.getType();
        if (type != null && type.length() > 0) {
            addTextWithLabelTopSeparator(layout, type, getString(R.string.device_type));
        }

        final String familyType = device.getFamilyType();
        if (familyType != null && familyType.length() > 0) {
            addTextWithLabelTopSeparator(layout, familyType, getString(R.string.device_family_type));
        }

        final String hardwareID = device.getHardwareId();
        if (hardwareID != null && hardwareID.length() > 0) {
            addTextWithLabelTopSeparator(layout, hardwareID, getString(R.string.device_hardware_id));
        }

        final String firmwareVersion = device.getFirmwareVersion();
        if (firmwareVersion != null && firmwareVersion.length() > 0) {
            addTextWithLabelTopSeparator(layout, firmwareVersion, getString(R.string.device_firmware_version));
        }

        final boolean isRouter = device.isRouter();
        if (isRouter) {
            addTextWithLabelTopSeparator(layout, getString(R.string.device_is_router), getString(R.string.device_router_info));
        } else {
            addTextWithLabelTopSeparator(layout, getString(R.string.device_is_no_router), getString(R.string.device_router_info));
        }

        addBottomMargin(layout);
    }

    private void addNetSettings() {
        final LinearLayout layout = (LinearLayout) findViewById(R.id.network_container);
        final Interface iface = announce.getParams().getNetSettings().getInterface();

        addTextWithLabelNoSeparator(layout, iface.getName(), getString(R.string.interface_name));

        final String interfaceType = iface.getType();
        if (interfaceType != null && interfaceType.length() > 0) {
            addTextWithLabelTopSeparator(layout, interfaceType, getString(R.string.interface_type));
        }

        final String interfaceDescription = iface.getDescription();
        if (interfaceDescription != null && interfaceDescription.length() > 0) {
            addTextWithLabelTopSeparator(layout, interfaceDescription, getString(R.string.interface_description));
        }

        final List<IPv4Entry> ipv4Address = iface.getIPv4();
        if (ipv4Address != null && !ipv4Address.isEmpty()) {
            addRule(layout);
            for (final IPv4Entry entry : ipv4Address) {
                addTextNoSeparator(layout, entry.getAddress() + "/" + entry.getNetmask());
            }
            addLabel(layout, getString(R.string.ipv4_addresses));
        }

        final List<IPv6Entry> ipv6Address = iface.getIPv6();
        if (ipv6Address != null && !ipv6Address.isEmpty()) {
            addRule(layout);
            for (final IPv6Entry entry : ipv6Address) {
                addTextNoSeparator(layout, entry.getAddress().replaceFirst("(^|:)(0+(:|$)){2,8}", "::") + "/" + entry.getPrefix());
            }
            addLabel(layout, getString(R.string.ipv6_addresses));
        }

        addBottomMargin(layout);
    }

	private void addServices() {
		final List<ServiceEntry> services = announce.getParams().getServices();
		if (services != null && !services.isEmpty()) {
			final LinearLayout cardContainer = (LinearLayout) findViewById(R.id.card_container);
			
			CardView card = new CardView(this);

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
					);
			params.setMargins(
				getResources().getDimensionPixelSize(R.dimen.details_card_margin_start),
				getResources().getDimensionPixelSize(R.dimen.details_card_margin_top),
				getResources().getDimensionPixelSize(R.dimen.details_card_margin_end),
				getResources().getDimensionPixelSize(R.dimen.details_card_margin_bottom));
			card.setLayoutParams(params);
			cardContainer.addView(card);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            card.addView(layout);

			AppCompatTextView textView = new AppCompatTextView(this);
			textView.setPadding(
					getResources().getDimensionPixelSize(R.dimen.details_headings_padding), 
					getResources().getDimensionPixelSize(R.dimen.details_headings_padding),
					getResources().getDimensionPixelSize(R.dimen.details_headings_padding), 
					getResources().getDimensionPixelSize(R.dimen.details_headings_padding));
			if (Build.VERSION.SDK_INT < 23) {
				textView.setTextAppearance(this, R.style.DetailsHeading);
			} else {
				textView.setTextAppearance(R.style.DetailsHeading);
            }
			textView.setTextIsSelectable(false);
			textView.setMaxLines(1);
			textView.setEllipsize(TruncateAt.END);
            textView.setText(getString(R.string.services));
            layout.addView(textView);

            View rule = new View(this);
            rule.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.ruler_height)));
            rule.setBackgroundColor(ContextCompat.getColor(this, R.color.details_horizontal_rule_color));
            layout.addView(rule);

            for (final ServiceEntry entry : services) {
                addTextNoSeparator(layout, entry.getType() + ": " + entry.getPort());
            }
            addBottomMargin(layout);
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

    private void addTextWithLabelTopSeparator(LinearLayout layout, String text, String label) {
        addText(layout, text, label, true);
    }

    private void addTextWithLabelNoSeparator(LinearLayout layout, String text, String label) {
        addText(layout, text, label, false);
    }

    private void addTextNoSeparator(LinearLayout layout, String text) {
        addText(layout, text, null, false);
    }

    private void addBottomMargin(LinearLayout layout) {
        View rule = new View(this);
        LinearLayout.LayoutParams viewLp = new LayoutParams(LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.rule_height));
        viewLp.setMargins(
            getResources().getDimensionPixelSize(R.dimen.device_info_padding_left), 
            getResources().getDimensionPixelSize(R.dimen.device_info_padding_top),
            0, 0);
        rule.setLayoutParams(viewLp);
        layout.addView(rule);
    }

    private void addText(LinearLayout layout, String text, String label, boolean withTopSeparator) {
        if (withTopSeparator) {
            addRule(layout);
        }

        AppCompatTextView textView = new AppCompatTextView(this);
        textView.setPadding(
            getResources().getDimensionPixelSize(R.dimen.device_info_padding_left), 
            getResources().getDimensionPixelSize(R.dimen.device_info_padding_top),
            0, 0);
        if (Build.VERSION.SDK_INT < 23) {
            textView.setTextAppearance(this, R.style.DetailsTextView);
        } else {
            textView.setTextAppearance(R.style.DetailsTextView);
        }
        textView.setText(text);
        layout.addView(textView);

        if (label != null) {
            addLabel(layout, label);
        }
    }

    private void addRule(LinearLayout layout) {
        View rule = new View(this);
        LinearLayout.LayoutParams viewLp = new LayoutParams(LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.rule_height));
        viewLp.setMargins(
            getResources().getDimensionPixelSize(R.dimen.device_info_padding_left), 
            getResources().getDimensionPixelSize(R.dimen.device_info_padding_top),
            0, 0);
        rule.setLayoutParams(viewLp);
        rule.setBackgroundColor(ContextCompat.getColor(this, R.color.details_horizontal_rule_color));
        layout.addView(rule);
    }

    private void addLabel(LinearLayout layout, String label) {
        AppCompatTextView labelView = new AppCompatTextView(this);
        labelView.setPadding(
            getResources().getDimensionPixelSize(R.dimen.device_info_padding_left), 
            getResources().getDimensionPixelSize(R.dimen.device_info_padding_top),
            0, 0);
        if (Build.VERSION.SDK_INT < 23) {
            labelView.setTextAppearance(this, R.style.DetailsLabelView);
        } else {
            labelView.setTextAppearance(R.style.DetailsTextView);
        }

        labelView.setText(label);
        layout.addView(labelView);
    }

}
