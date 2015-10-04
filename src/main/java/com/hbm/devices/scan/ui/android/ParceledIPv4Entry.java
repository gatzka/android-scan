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

import com.hbm.devices.scan.announce.IPv4Entry;

final class ParceledIPv4Entry implements Parcelable {
    private final String address;
    private final String netMask;

    public static final Parcelable.Creator<ParceledIPv4Entry> CREATOR
            = new Parcelable.Creator<ParceledIPv4Entry>() {

        @Override
        public ParceledIPv4Entry createFromParcel(Parcel incoming) {
            return new ParceledIPv4Entry(incoming);
        }

        @Override
        public ParceledIPv4Entry[] newArray(int size) {
            return new ParceledIPv4Entry[size];
        }
    };

    ParceledIPv4Entry(IPv4Entry entry) {
        address = entry.getAddress();
        netMask = entry.getNetmask();
    }

    private ParceledIPv4Entry(Parcel incoming) {
        address = incoming.readString();
        netMask = incoming.readString();
    }

    @Override
    public void writeToParcel(Parcel outgoing, int flags) {
        outgoing.writeString(address);
        outgoing.writeString(netMask);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    String getAddress() {
        return address;
    }

    String getNetmask() {
        return netMask;
    }
}
