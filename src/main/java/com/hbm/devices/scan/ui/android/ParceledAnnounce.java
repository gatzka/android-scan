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

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.AnnounceParams;

final class ParceledAnnounce implements Parcelable {

    private final String jsonString;
    private ParceledParams params;
    private String connectableHostName;

    public static final Parcelable.Creator<ParceledAnnounce> CREATOR
            = new Parcelable.Creator<ParceledAnnounce>() {
        @Override
        public ParceledAnnounce createFromParcel(Parcel incoming) {
            return new ParceledAnnounce(incoming);
        }

        @Override
        public ParceledAnnounce[] newArray(int size) {
            return new ParceledAnnounce[size];
        }
    };

    ParceledAnnounce(Announce announce) {
        jsonString = announce.getJSONString();
        final AnnounceParams parameters = announce.getParams();
        if (parameters != null) {
            this.params = new ParceledParams(parameters);
        }
    }

    private ParceledAnnounce(Parcel incoming) {
        jsonString = incoming.readString();
        params = incoming.readParcelable(Thread.currentThread().getContextClassLoader());
        connectableHostName = incoming.readString();
    }

    void setAddress(String address) {
        connectableHostName = address;
    }

    String getConnectableHostName() {
        return connectableHostName;
    }

    String getJsonString() {
        return jsonString;
    }

    ParceledParams getParams() {
        return params;
    }

    @Override
    public void writeToParcel(Parcel outgoing, int flags) {
        outgoing.writeString(jsonString);
        outgoing.writeParcelable(params, flags);
        outgoing.writeString(connectableHostName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ParceledAnnounce)) {
            return false;
        }
        final ParceledAnnounce rhs = (ParceledAnnounce) obj;
        return this.params.getDevice().getUuid().equals(rhs.params.getDevice().getUuid());
    }

    @Override
    public int hashCode() {
        return params.getDevice().getUuid().hashCode();
    }
}
