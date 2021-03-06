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

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Comparator;

class BestConnectableAddressComparator implements Comparator<InetAddress>, Serializable {

    private static final long serialVersionUID = 4760985979057804116L;

    @Override
    public int compare(InetAddress first, InetAddress second) {
        if ((first instanceof Inet4Address) && (second instanceof Inet6Address)) {
            return -1;
        } else if ((first instanceof Inet6Address) && (second instanceof Inet4Address)) {
            return 1;
        } else {
            return compareLinkLocal(first, second);
        }
    }

    private static int compareLinkLocal(InetAddress first, @NonNull InetAddress second) {
        if (!first.isLinkLocalAddress() && second.isLinkLocalAddress()) {
            return -1;
        } else if (first.isLinkLocalAddress() && !second.isLinkLocalAddress()) {
            return 1;
        } else {
            return 0;
        }
    }
}
