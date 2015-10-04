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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import com.hbm.devices.scan.announce.Interface;
import com.hbm.devices.scan.announce.IPv4Entry;
import com.hbm.devices.scan.announce.IPv6Entry;

final class ParceledInterface implements Parcelable {

    private final String name;
    private final String type;
    private final String description;
    private List<ParceledIPv4Entry> ipv4;
    private List<ParceledIPv6Entry> ipv6;

    public static final Parcelable.Creator<ParceledInterface> CREATOR
            = new Parcelable.Creator<ParceledInterface>() {

        @Override
        public ParceledInterface createFromParcel(Parcel incoming) {
            return new ParceledInterface(incoming);
        }

        @Override
        public ParceledInterface[] newArray(int size) {
            return new ParceledInterface[size];
        }
    };

    ParceledInterface(Interface iface) {
        name = iface.getName();
        type = iface.getType();
        description = iface.getDescription();
        final List<IPv4Entry> ipv4Entries = iface.getIPv4();
        if (ipv4Entries != null) {
            ipv4 = new ArrayList<ParceledIPv4Entry>(ipv4Entries.size());
            for (final IPv4Entry entry: ipv4Entries) {
                ipv4.add(new ParceledIPv4Entry(entry));
            }
        }
        final List<IPv6Entry> ipv6Entries = iface.getIPv6();
        if (ipv6Entries != null) {
            ipv6 = new ArrayList<ParceledIPv6Entry>(ipv6Entries.size());
            for (final IPv6Entry entry: ipv6Entries) {
                ipv6.add(new ParceledIPv6Entry(entry));
            }
        }
    }

    private ParceledInterface(Parcel incoming) {
        name = incoming.readString();
        type = incoming.readString();
        description = incoming.readString();
        ipv4 = new ArrayList<ParceledIPv4Entry>();
        incoming.readTypedList(ipv4, ParceledIPv4Entry.CREATOR);
        ipv6 = new ArrayList<ParceledIPv6Entry>();
        incoming.readTypedList(ipv6, ParceledIPv6Entry.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel outgoing, int flags) {
        outgoing.writeString(name);
        outgoing.writeString(type);
        outgoing.writeString(description);
        outgoing.writeTypedList(ipv4);
        outgoing.writeTypedList(ipv6);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    String getName() {
        return name;
    }

    String getType() {
        return type;
    }

    String getDescription() {
        return description;
    }

    List<ParceledIPv4Entry> getIPv4() {
        return ipv4;
    }

    List<ParceledIPv6Entry> getIPv6() {
        return ipv6;
    }
}
