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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;

import com.hbm.devices.scan.ScanInterfaces;
import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.ConnectionFinder;
import com.hbm.devices.scan.announce.IPEntry;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;

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
            StringBuilder url = new StringBuilder("https://hbm-pwa.herokuapp.com/");
            String ip = getBestIP4Address();
            if (ip != null) {
                url.append("?ip=");
                url.append(ip);
            }

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url.toString()));
            context.startActivity(i);
        } else {
            openBrowser(view.getContext(), announce);
        }
    }

    private static String getPAD2PackageName(PackageManager pm) {
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            CharSequence nonLocalizedName = pm.getApplicationLabel(packageInfo);
            if (nonLocalizedName != null) {
                if (nonLocalizedName.toString().equals("PAD 2")) {
                    return packageInfo.packageName;
                }
            }
        }

        return null;
    }

    private String getBestIP4Address() {
        try {
            final ScanInterfaces interfaces = new ScanInterfaces();
            Collection<NetworkInterface> networkInterfaces = interfaces.getInterfaces();

            List<IPEntry> ipList = announce.getParams().getNetSettings().getInterface().getIPList();
            LinkedList<IPEntry> connectCandidates = new LinkedList<>();

            for (IPEntry entry: ipList) {
                InetAddress address = entry.getAddress();
                if (address instanceof Inet4Address) {
                    connectCandidates.add(entry);
                }
            }

            Collections.sort(connectCandidates, new BestIpComparator(networkInterfaces));

            if (!connectCandidates.isEmpty()) {
                return connectCandidates.get(0).getAddress().getHostAddress();
            }
        } catch (SocketException ignored) {
        }

        return null;
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

class BestIpComparator implements Comparator<IPEntry> {

    private Collection<NetworkInterface> networkInterfaces;

    BestIpComparator(Collection<NetworkInterface> networkInterfaces) {
        this.networkInterfaces = networkInterfaces;
    }

    @Override
    public int compare(IPEntry e1, IPEntry e2) {
        final Inet4Address e1Address = (Inet4Address)e1.getAddress();
        final int e1Prefix = e1.getPrefix();
        final Inet4Address e2Address = (Inet4Address)e2.getAddress();
        final int e2Prefix = e2.getPrefix();

        for (final NetworkInterface iface : networkInterfaces) {
            final List<InterfaceAddress> niAddresses = iface.getInterfaceAddresses();
            for (final InterfaceAddress niAddress : niAddresses) {
                final InetAddress interfaceAddress = niAddress.getAddress();
                if (interfaceAddress instanceof Inet4Address) {
                    final Inet4Address interfaceAddress4 = (Inet4Address)interfaceAddress;
                    final int niPrefix = niAddress.getNetworkPrefixLength();
                    boolean e1SameNet = sameIPv4Net(e1Address, e1Prefix, interfaceAddress4, niPrefix);
                    boolean e2SameNet = sameIPv4Net(e2Address, e2Prefix, interfaceAddress4, niPrefix);
                    if (e1SameNet && e2SameNet) {
                        return 0;
                    }

                    if (!e1SameNet && !e2SameNet) {
                        if (!e1Address.isLinkLocalAddress() && e2Address.isLinkLocalAddress()) {
                            return -1;
                        }

                        if (e1Address.isLinkLocalAddress() && !e2Address.isLinkLocalAddress()) {
                            return 1;
                        }

                        return 0;
                    }

                    if (e1SameNet && !e2SameNet) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
        }

        return 0;
    }

    private static boolean sameIPv4Net(Inet4Address announceAddress, int announcePrefix,
                               Inet4Address interfaceAddress, int interfacePrefix) {
        final byte[] announceBytes = announceAddress.getAddress();
        final byte[] interfaceBytes = interfaceAddress.getAddress();
        int announceInteger = convertToInteger(announceBytes);
        int interfaceInteger = convertToInteger(interfaceBytes);
        announceInteger = announceInteger >>> (Integer.SIZE - announcePrefix);
        interfaceInteger = interfaceInteger >>> (Integer.SIZE - interfacePrefix);
        return announceInteger == interfaceInteger;
    }

    private static int convertToInteger(byte... address) {
        int value = 0;
        for (final byte b: address) {
            value = (value << Byte.SIZE) | (b & 0xff);
        }
        return value;
    }
}
