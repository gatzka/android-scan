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
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
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

    private int paddingTop;
    private int paddingStartLevel1;
    private int paddingStartLevel2;
    private int paddingStartLevel3;
    private int textSizeLarge;
    private int textSizeMedium;
    private int textSizeSmall;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_details);

        announce = (Announce) getIntent().getSerializableExtra(DeviceViewHolder.DETAILS);
    	initToolbar(announce);

        LinearLayout layout = (LinearLayout) findViewById(R.id.details_container);

        final Resources resources = this.getResources();
        paddingTop = (int) resources.getDimension(R.dimen.level2_top_bottom_padding);

        paddingStartLevel1 = (int) resources.getDimension(R.dimen.level1_start_padding);
        paddingStartLevel2 = (int) resources.getDimension(R.dimen.level2_start_padding);
        paddingStartLevel3 = (int) resources.getDimension(R.dimen.level3_start_padding);

        textSizeLarge = (int) resources.getDimension(R.dimen.text_size_large);
        textSizeMedium = (int) resources.getDimension(R.dimen.text_size_medium);
        textSizeSmall = (int) resources.getDimension(R.dimen.text_size_small);

        addDeviceInformation();
        // addNetSettings(layout);
        // addServices(layout);
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
        final Device device = announce.getParams().getDevice();

        TextView view = (TextView) findViewById(R.id.device_name);
        setText(view, device.getName(), getString(R.string.no_name_announced));
        view = (TextView) findViewById(R.id.device_uuid);
        view.setText(device.getUuid());
/*
        final TextView deviceText = new TextView(this);
        deviceText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeLarge);
        deviceText.setPadding(paddingStartLevel1, paddingStartLevel1, 0, 0);
        deviceText.setText("Device Information");
        layout.addView(deviceText);

        addSecondLevelText(layout, device.getUuid(), "UUID: ");
        addSecondLevelText(layout, device.getName(), "Name: ");
        addSecondLevelText(layout, device.getType(), "Type: ");
        addSecondLevelText(layout, device.getFamilyType(), "Family Type: ");
        addSecondLevelText(layout, device.getFirmwareVersion(), "Firmware Version: ");

        final boolean isRouter = device.isRouter();
        final StringBuilder routerText = new StringBuilder();
        if (isRouter) {
            routerText.append("yes");
        } else {
            routerText.append("no");
        }
        addSecondLevelText(layout, routerText.toString(), "Is Router: ");
*/
    }

    private void setText(TextView view, String text, String alternativeText) {
        if (text != null) {
            view.setText(text);
        } else {
            view.setTypeface(null, Typeface.ITALIC);
            view.setText(alternativeText);
        }
    }

    private void addNetSettings(LinearLayout layout) {
        final Interface iface = announce.getParams().getNetSettings().getInterface();

        final TextView settings = new TextView(this);
        settings.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeLarge);
        settings.setPadding(paddingStartLevel1, paddingStartLevel1, 0, 0);
        settings.setText("Network Settings");
        layout.addView(settings);

        addSecondLevelText(layout, iface.getName(), "Interface Name: ");
        addSecondLevelText(layout, iface.getType(), "Interface Type: ");
        addSecondLevelText(layout, iface.getDescription(), "Interface Description: ");

        final List<IPv4Entry> ipv4 = iface.getIPv4();
        if (ipv4 != null && !ipv4.isEmpty()) {
            final TextView ipv4Text = new TextView(this);
            ipv4Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeMedium);
            ipv4Text.setPadding(paddingStartLevel2, paddingTop, 0, paddingTop);
            ipv4Text.setText("IPv4 addresses");
            layout.addView(ipv4Text);

            for (final IPv4Entry entry : ipv4) {
                addThirdLevelText(layout, entry.getAddress() + "/" + entry.getNetmask(), "∙ ");
            }
        }

        final List<IPv6Entry> ipv6 = iface.getIPv6();
        if (ipv6 != null && !ipv6.isEmpty()) {
            final TextView ipv6Text = new TextView(this);
            ipv6Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeMedium);
            ipv6Text.setPadding(paddingStartLevel2, paddingTop, 0, paddingTop);
            ipv6Text.setText("IPv6 addresses");
            layout.addView(ipv6Text);

            for (final IPv6Entry entry : ipv6) {
                addThirdLevelText(layout, entry.getAddress() + "/" + entry.getPrefix(), "∙ ");
            }
        }
    }

    private void addServices(LinearLayout layout) {
        final List<ServiceEntry> services = announce.getParams().getServices();
        if (!services.isEmpty()) {
            final TextView servicesText = new TextView(this);
            servicesText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeLarge);
            servicesText.setPadding(paddingStartLevel1, paddingStartLevel1, 0, 0);
            servicesText.setText("Services");
            layout.addView(servicesText);

            for (final ServiceEntry entry : services) {
                addThirdLevelText(layout, entry.getType() + ": " + entry.getPort(), "∙ ");
            }
        }
    }

    private void addSecondLevelText(LinearLayout layout, String text, String label) {
        if (text != null && text.length() > 0) {
            final TextView view = new TextView(this);
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeMedium);
            view.setPadding(paddingStartLevel2, paddingTop, 0, paddingTop);
            view.setText(label + text);
            layout.addView(view);
        }
    }

    private void addThirdLevelText(LinearLayout layout, String text, String label) {
        if (text != null && text.length() > 0) {
            final TextView view = new TextView(this);
            view.setSingleLine(true);
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSmall);
            view.setPadding(paddingStartLevel3, paddingTop, 0, paddingTop);
            view.setText(label + text);
            layout.addView(view);
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
}
