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

import com.hbm.devices.scan.announce.AnnounceParams;
import com.hbm.devices.scan.announce.Device;
import com.hbm.devices.scan.announce.NetSettings;
import com.hbm.devices.scan.announce.Router;
import com.hbm.devices.scan.announce.ServiceEntry;

final class ParceledParams implements Parcelable {
    private ParceledDevice device;
    private ParceledNetSettings netSettings;
    private ParceledRouter router;
    private List<ParceledServiceEntry> services;
    private final int expiration;

    public static final Parcelable.Creator<ParceledParams> CREATOR
            = new Parcelable.Creator<ParceledParams>() {

        @Override
        public ParceledParams createFromParcel(Parcel incoming) {
            return new ParceledParams(incoming);
        }

        @Override
        public ParceledParams[] newArray(int size) {
            return new ParceledParams[size];
        }
    };

    ParceledParams(AnnounceParams params) {
        final Device dev = params.getDevice();
        if (dev != null) {
            this.device = new ParceledDevice(dev);
        }
        final NetSettings net = params.getNetSettings();
        if (net != null) {
            this.netSettings = new ParceledNetSettings(net);
        }
        final Router aRouter = params.getRouter();
        if (aRouter != null) {
            this.router = new ParceledRouter(aRouter);
        }
        final List<ServiceEntry> entries = params.getServices();
        if (entries != null) {
            services = new ArrayList<ParceledServiceEntry>(entries.size());
            for (final ServiceEntry entry: entries) {
                services.add(new ParceledServiceEntry(entry));
            }
        }
        expiration = params.getExpiration();
    }

    private ParceledParams(Parcel incoming) {
        device = incoming.readParcelable(Thread.currentThread().getContextClassLoader());
        netSettings = incoming.readParcelable(Thread.currentThread().getContextClassLoader());
        router = incoming.readParcelable(Thread.currentThread().getContextClassLoader());
        services = new ArrayList<ParceledServiceEntry>();
        incoming.readTypedList(services, ParceledServiceEntry.CREATOR);
        expiration = incoming.readInt();
    }

    @Override
    public void writeToParcel(Parcel outgoing, int flags) {
        outgoing.writeParcelable(device, flags);
        outgoing.writeParcelable(netSettings, flags);
        outgoing.writeParcelable(router, flags);
        outgoing.writeTypedList(services);
        outgoing.writeInt(expiration);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    ParceledDevice getDevice() {
        return device;
    }

    ParceledNetSettings getNetSettings() {
        return netSettings;
    }

    List<ParceledServiceEntry> getServices() {
        return services;
    }
}
