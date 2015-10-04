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

import com.hbm.devices.scan.announce.ServiceEntry;

final class ParceledServiceEntry implements Parcelable {

    private final String type;
    private final int port;

    public static final Parcelable.Creator<ParceledServiceEntry> CREATOR
            = new Parcelable.Creator<ParceledServiceEntry>() {

        @Override
        public ParceledServiceEntry createFromParcel(Parcel incoming) {
            return new ParceledServiceEntry(incoming);
        }

        @Override
        public ParceledServiceEntry[] newArray(int size) {
            return new ParceledServiceEntry[size];
        }
    };

    ParceledServiceEntry(ServiceEntry entry) {
        type = entry.getType();
        port = entry.getPort();
    }

    private ParceledServiceEntry(Parcel incoming) {
        type = incoming.readString();
        port = incoming.readInt();
    }

    @Override
    public void writeToParcel(Parcel outgoing, int flags) {
        outgoing.writeString(type);
        outgoing.writeInt(port);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    String getType() {
        return type;
    }

    int getPort() {
        return port;
    }
}
