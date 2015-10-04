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

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.Device;
import com.hbm.devices.scan.announce.Interface;
import com.hbm.devices.scan.announce.IPv4Entry;
import com.hbm.devices.scan.announce.IPv6Entry;
import com.hbm.devices.scan.announce.ServiceEntry;

public final class DeviceDetailsFragment extends Fragment {

    private ParceledAnnounce announce;

    private ScrollView scroller;
    private LinearLayout layout;
    private Activity activity;
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
        this.activity = getActivity();

        announce = getArguments().getParcelable("bla");
        activity.getActionBar().setTitle(getDisplayName(announce.getParams().getDevice()));

        scroller = new ScrollView(activity);
        HorizontalScrollView hscroller = new HorizontalScrollView(activity);
        scroller.addView(hscroller);
        layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        hscroller.addView(layout);

        final Resources resources = this.getResources();
        paddingTop = (int) resources.getDimension(R.dimen.level2_top_bottom_padding);

        paddingStartLevel1 = (int) resources.getDimension(R.dimen.level1_start_padding);
        paddingStartLevel2 = (int) resources.getDimension(R.dimen.level2_start_padding);
        paddingStartLevel3 = (int) resources.getDimension(R.dimen.level3_start_padding);

        textSizeLarge = (int) resources.getDimension(R.dimen.text_size_large);
        textSizeMedium = (int) resources.getDimension(R.dimen.text_size_medium);
        textSizeSmall = (int) resources.getDimension(R.dimen.text_size_small);
    }

   	private String getDisplayName(ParceledDevice device) {
        String displayName = device.getName();
        if (displayName == null || displayName.length() == 0) {
            displayName = device.getUuid();
        }
        return displayName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }

        addDeviceInformation(layout);
        addNetSettings(layout);
        addServices(layout);

        return scroller;
    }

    private void addDeviceInformation(LinearLayout layout) {
        final ParceledDevice device = announce.getParams().getDevice();

        final TextView deviceText = new TextView(activity);
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
    }

    private void addNetSettings(LinearLayout layout) {
        final ParceledInterface iface = announce.getParams().getNetSettings().getInterface();

        final TextView settings = new TextView(activity);
        settings.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeLarge);
        settings.setPadding(paddingStartLevel1, paddingStartLevel1, 0, 0);
        settings.setText("Network Settings");
        layout.addView(settings);

        addSecondLevelText(layout, iface.getName(), "Interface Name: ");
        addSecondLevelText(layout, iface.getType(), "Interface Type: ");
        addSecondLevelText(layout, iface.getDescription(), "Interface Description: ");

        final List<ParceledIPv4Entry> ipv4 = iface.getIPv4();
        if (ipv4 != null && !ipv4.isEmpty()) {
            final TextView ipv4Text = new TextView(activity);
            ipv4Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeMedium);
            ipv4Text.setPadding(paddingStartLevel2, paddingTop, 0, paddingTop);
            ipv4Text.setText("IPv4 addresses");
            layout.addView(ipv4Text);

            for (final ParceledIPv4Entry entry : ipv4) {
                final StringBuilder builder = new StringBuilder(entry.getAddress()).append("/")
                    .append(entry.getNetmask());
                addThirdLevelText(layout, builder.toString(), "∙ ");
            }
        }

        final List<ParceledIPv6Entry> ipv6 = iface.getIPv6();
        if (ipv6 != null && !ipv6.isEmpty()) {
            final TextView ipv6Text = new TextView(activity);
            ipv6Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeMedium);
            ipv6Text.setPadding(paddingStartLevel2, paddingTop, 0, paddingTop);
            ipv6Text.setText("IPv6 addresses");
            layout.addView(ipv6Text);

            for (final ParceledIPv6Entry entry : ipv6) {
                final StringBuilder builder = new StringBuilder(entry.getAddress())
                    .append("/").append(entry.getPrefix());
                addThirdLevelText(layout, builder.toString(), "∙ ");
            }
        }
    }

    private void addServices(LinearLayout layout) {
        final List<ParceledServiceEntry> services = announce.getParams().getServices();
        if (!services.isEmpty()) {
            final TextView servicesText = new TextView(activity);
            servicesText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeLarge);
            servicesText.setPadding(paddingStartLevel1, paddingStartLevel1, 0, 0);
            servicesText.setText("Services");
            layout.addView(servicesText);

            for (final ParceledServiceEntry entry : services) {
                final StringBuilder builder = new StringBuilder(entry.getType())
                    .append(": ").append(entry.getPort());
                addThirdLevelText(layout, builder.toString(), "∙ ");
            }
        }
    }

    private void addSecondLevelText(LinearLayout layout, String text, String label) {
        if (text != null && text.length() > 0) {
            final TextView view = new TextView(activity);
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeMedium);
            view.setPadding(paddingStartLevel2, paddingTop, 0, paddingTop);
            view.setText(label + text);
            layout.addView(view);
        }
    }

    private void addThirdLevelText(LinearLayout layout, String text, String label) {
        if (text != null && text.length() > 0) {
            final TextView view = new TextView(activity);
            view.setSingleLine(true);
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSmall);
            view.setPadding(paddingStartLevel3, paddingTop, 0, paddingTop);
            view.setText(label + text);
            layout.addView(view);
        }
    }
}
