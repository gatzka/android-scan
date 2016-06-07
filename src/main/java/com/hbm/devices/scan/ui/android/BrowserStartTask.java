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
import android.net.Uri;
import android.os.AsyncTask;

import com.hbm.devices.scan.ScanInterfaces;
import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.ConnectionFinder;
import com.hbm.devices.scan.announce.ServiceEntry;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

final class BrowserStartTask extends AsyncTask<Announce, Void, Integer> {

    private final Context context;

    BrowserStartTask(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected Integer doInBackground(Announce... announces) {
        final Announce announce = announces[0];

        final InetAddress address = findConnectableAddress(announce);
        if (address == null) {
            return 0;
        }
        final HttpInfo info = getHttpInfo(announce);
        if (info == null) {
            return 0;
        }

        final String hostName = address.getHostName();
        final Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme(info.scheme);
        uriBuilder.encodedAuthority(hostName + ':' + info.port);

        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, uriBuilder.build().normalizeScheme());
        context.startActivity(browserIntent);
        return 0;
    }

    private static InetAddress findConnectableAddress(Announce announce) {
        try {
            final ScanInterfaces interfaces = new ScanInterfaces();
            ConnectionFinder connectionFinder = new ConnectionFinder(interfaces.getInterfaces());
            List<InetAddress> addresses = connectionFinder.getConnectableAddresses(announce);
            addresses = removeIPv6LinkLocal(addresses);
            BestConnectableAddressComparator addressComparator = new BestConnectableAddressComparator();
            Collections.sort(addresses, addressComparator);
            if (!addresses.isEmpty()) {
                return addresses.get(0);
            }
            return null;
        } catch (SocketException e) {
            return null;
        }
    }

    private static List<InetAddress> removeIPv6LinkLocal(List<InetAddress> addresses) {
        final Iterator<InetAddress> iterator = addresses.iterator();
        while (iterator.hasNext()) {
            final InetAddress address = iterator.next();
            if ((address instanceof Inet6Address) && address.isLinkLocalAddress()) {
                iterator.remove();
            }
        }
        return addresses;
    }

    private HttpInfo getHttpInfo(Announce announce) {
        HttpInfo info = null;

        final List<ServiceEntry> entries = announce.getParams().getServices();
        for (final ServiceEntry entry : entries) {
            if ("https".equals(entry.getType())) {
                return new HttpInfo("https", entry.getPort());
            }
            if (ServiceEntry.SERVICE_HTTP.equals(entry.getType())) {
                info = new HttpInfo("http", entry.getPort());
            }
        }
        return info;
    }

    class HttpInfo {
        String scheme;
        int port;

        HttpInfo(String s, int p) {
            scheme = s;
            port = p;
        }
    }
}

