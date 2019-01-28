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
import androidx.annotation.NonNull;
import android.view.View;

import com.hbm.devices.scan.ScanInterfaces;
import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.ConnectionFinder;
import com.hbm.devices.scan.announce.IPEntry;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;

final class ModuleCardClickListener implements View.OnClickListener {

    private static final String WTX_MOBILE_PACKAGE = "com.hbm.devices.wtx.ui.android";
    private final String moduleType;
    private final Announce announce;

    ModuleCardClickListener(final Announce announce, final String moduleType) {
        this.announce = announce;
        this.moduleType = moduleType;
    }

    @Override
    public void onClick(@NonNull View view) {
        if ("WTX120".equals(moduleType) || ("WTX110".equals(moduleType))) {
            final Context context = view.getContext();
            final PackageManager pm = context.getPackageManager();
            boolean isInstalled = isPackageInstalled(pm);
            if (isInstalled) {
                final Intent sendIntent = pm.getLaunchIntentForPackage(WTX_MOBILE_PACKAGE);
                fillIntent(sendIntent);
                context.startActivity(sendIntent);
            }
        } else {
            openBrowser(view.getContext(), announce);
        }
    }

    private void fillIntent(@NonNull Intent sendIntent) {
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
            Collection<NetworkInterface> networkInterfaces = interfaces.getInterfaces();
            ConnectionFinder connectionFinder = new ConnectionFinder(networkInterfaces);
            Collection<InetAddress> sameNetAddresses = connectionFinder.getSameNetworkAddresses(announce);
            final ArrayList<InetAddress> sameNetIps = new ArrayList<>(sameNetAddresses);
            sendIntent.putExtra("same_net_addresses", sameNetIps);
        } catch (SocketException ignored) {
        }
    }

    private boolean isPackageInstalled(PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(ModuleCardClickListener.WTX_MOBILE_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void openBrowser(@NonNull Context context, Announce announce) {
        final BrowserStartTask browserTask = new BrowserStartTask(context);
        browserTask.execute(announce);
    }
}
