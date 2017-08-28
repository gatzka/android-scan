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
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hbm.devices.scan.ScanInterfaces;
import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.ConnectionFinder;
import com.hbm.devices.scan.announce.Device;
import com.hbm.devices.scan.announce.IPEntry;
import com.squareup.picasso.Picasso;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class DeviceViewHolder extends RecyclerView.ViewHolder {

    public static final String DETAILS = "Details";
    private static final String WTX_MOBILE_PACKAGE = "com.hbm.devices.wtx.ui.android";

    private final TextView tvModuleId;
    private final TextView tvModuleType;
    private final TextView tvModuleName;
    private final ImageView devicePhoto;
    private final ImageButton infoButton;
    private final CardView cardView;
    private final Context context;

    private Announce announce;

    DeviceViewHolder(CardView itemView) {
        super(itemView);

        context = itemView.getContext();

        tvModuleId = (TextView) itemView.findViewById(R.id.moduleID);
        tvModuleType = (TextView) itemView.findViewById(R.id.moduleType);
        tvModuleName = (TextView) itemView.findViewById(R.id.moduleName);
        devicePhoto = (ImageView) itemView.findViewById(R.id.device_photo);
        infoButton = (ImageButton) itemView.findViewById(R.id.infoButton);
        cardView = itemView;

    }

    void bind(Announce a) {
        this.announce = a;
        final Device device = announce.getParams().getDevice();
        final String displayName = getDisplayName(device);
        final String moduleType = getModuleType(device);
        final String uuid = device.getUuid();

        final DeviceHolderResources resources = DeviceHolderResources.getInstance(context);
        final int alpha = resources.getAlpha();
        cardView.setCardBackgroundColor(resources.getCardBackgroundConnectable());
        tvModuleType.setTextColor(setTextAlpha(resources.getModuleTypeTextColorConnectable(), alpha));
        tvModuleName.setTextColor(setTextAlpha(resources.getModuleNameTextColorConnectable(), alpha));
        tvModuleId.setTextColor(setTextAlpha(resources.getModuleIdTextColorConnectable(), alpha));
        infoButton.setImageDrawable(resources.getBlackInfo());

        tvModuleType.setText(moduleType);
        tvModuleName.setText(displayName);
        tvModuleId.setText(uuid);
        devicePhoto.setImageDrawable(null);
        final Picasso picasso = Picasso.with(context);
        picasso.load(getImageResourceId(a)).into(devicePhoto);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("WTX120".equals(moduleType) || ("WTX110".equals(moduleType))) {
                    final Context context = v.getContext();
                    final PackageManager pm = context.getPackageManager();
                    boolean isInstalled = isPackageInstalled(WTX_MOBILE_PACKAGE, pm);
                    if (isInstalled) {
                        final Intent sendIntent =   pm.getLaunchIntentForPackage(WTX_MOBILE_PACKAGE);

                        final Collection<IPEntry> announceAddresses = announce.getParams().getNetSettings()
                                .getInterface().getIPList();

                        final ArrayList<InetAddress> ips = new ArrayList<>();
                        final ArrayList<Integer> prefixes = new ArrayList<>();
                        for (IPEntry entry: announceAddresses) {
                            ips.add(entry.getAddress());
                            prefixes.add(entry.getPrefix());
                        }

                        sendIntent.putExtra("addresses", ips);
                        sendIntent.putExtra("prefixes", prefixes);

                        try {
                            final ScanInterfaces interfaces = new ScanInterfaces();
                            Collection<NetworkInterface> ifaces = interfaces.getInterfaces();
                            ConnectionFinder connectionFinder = new ConnectionFinder(ifaces);
                            Collection<InetAddress> sameNetAddresses = connectionFinder.getSameNetworkAddresses(announce);
                            final ArrayList<InetAddress> sameNetIps = new ArrayList<>(sameNetAddresses);
                            sendIntent.putExtra("same_net_addresses", sameNetIps);
                            context.startActivity(sendIntent);
                        } catch (SocketException e) {
                        }
                    }
                } else {
                    openBrowser(announce);
                }
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(context, DeviceDetailsActivity.class);
                intent.putExtra(DETAILS, announce);
                ActivityCompat.startActivity(context, intent, null);
                ((ScanActivity) context).overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            }
        });
    }

    private static int getImageResourceId(Announce announce) {
        String key = announce.getParams().getDevice().getLabel();
        if (key == null || key.isEmpty()) {
            key = announce.getParams().getDevice().getType();
        }
        if (key == null || key.isEmpty()) {
            return R.drawable.ic_no_device;
        }
        return ImageResourceCache.getResourceFromCache(key);
    }

    private static int setTextAlpha(int color, int alphaPercent) {
        final int red = Color.red(color);
        final int green = Color.green(color);
        final int blue = Color.blue(color);
        final int alpha = alphaPercent * 255 / 100;
        return Color.argb(alpha, red, green, blue);
    }

    private String getModuleType(final Device device) {
        String moduleType = device.getType();
        if (moduleType == null || moduleType.length() == 0) {
            moduleType = DeviceHolderResources.getInstance(context).getUnknown();
        }
        return moduleType;
    }

    private String getDisplayName(final Device device) {
        String displayName = device.getName();
        if (displayName == null || displayName.length() == 0) {
            displayName = DeviceHolderResources.getInstance(context).getUnknown();
        }
        return displayName;
    }

    private void openBrowser(Announce announce) {
        final BrowserStartTask browserTask = new BrowserStartTask(context);
        browserTask.execute(announce);
    }

    private boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static int calculatePrefix(InetAddress announceNetmask) {
        final byte[] address = announceNetmask.getAddress();
        final int length = address.length;
        int prefix = 0;
        for (int i = 0; i < length; i++) {
            prefix += Integer.bitCount(address[i] & 0xff);
        }
        return prefix;
    }
}
