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

import com.hbm.devices.scan.announce.DefaultGateway;
import com.hbm.devices.scan.announce.Interface;
import com.hbm.devices.scan.announce.NetSettings;

final class ParceledNetSettings implements Parcelable {

    private ParceledDefaultGateway defaultGateway;
    private ParceledInterface iface;

    public static final Parcelable.Creator<ParceledNetSettings> CREATOR
            = new Parcelable.Creator<ParceledNetSettings>() {

        @Override
        public ParceledNetSettings createFromParcel(Parcel incoming) {
            return new ParceledNetSettings(incoming);
        }

        @Override
        public ParceledNetSettings[] newArray(int size) {
            return new ParceledNetSettings[size];
        }
    };

    ParceledNetSettings(NetSettings netSettings) {
        final DefaultGateway aDefaultGateway = netSettings.getDefaultGateway();
        if (aDefaultGateway != null) {
            this.defaultGateway = new ParceledDefaultGateway(aDefaultGateway);
        }
        final Interface aIface = netSettings.getInterface();
        if (aIface != null) {
            this.iface = new ParceledInterface(aIface);
        }
    }

    private ParceledNetSettings(Parcel incoming) {
        defaultGateway = incoming.readParcelable(Thread.currentThread().getContextClassLoader());
        iface = incoming.readParcelable(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public void writeToParcel(Parcel outgoing, int flags) {
        outgoing.writeParcelable(defaultGateway, flags);
        outgoing.writeParcelable(iface, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    ParceledInterface getInterface() {
        return iface;
    }
}
