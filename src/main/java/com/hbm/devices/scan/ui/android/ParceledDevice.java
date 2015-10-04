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

import com.hbm.devices.scan.announce.Device;

final class ParceledDevice implements Parcelable {

    private final String uuid;
    private final String name;
    private final String type;
    private final String familyType;
    private final String firmwareVersion;
    private final String hardwareId;
    private final boolean router;

    public static final Parcelable.Creator<ParceledDevice> CREATOR
            = new Parcelable.Creator<ParceledDevice>() {

        @Override
        public ParceledDevice createFromParcel(Parcel incoming) {
            return new ParceledDevice(incoming);
        }

        @Override
        public ParceledDevice[] newArray(int size) {
            return new ParceledDevice[size];
        }
    };

    ParceledDevice(Device device) {
        uuid = device.getUuid();
        name = device.getName();
        type = device.getType();
        familyType = device.getFamilyType();
        firmwareVersion = device.getFirmwareVersion();
        hardwareId = device.getHardwareId();
        router = device.isRouter();
    }

    private ParceledDevice(Parcel incoming) {
        uuid = incoming.readString();
        name = incoming.readString();
        type = incoming.readString();
        familyType = incoming.readString();
        firmwareVersion = incoming.readString();
        hardwareId = incoming.readString();
        router = (incoming.readInt() != 0);
    }

    @Override
    public void writeToParcel(Parcel outgoing, int flags) {
        outgoing.writeString(uuid);
        outgoing.writeString(name);
        outgoing.writeString(type);
        outgoing.writeString(familyType);
        outgoing.writeString(firmwareVersion);
        outgoing.writeString(hardwareId);
        if (router) {
            outgoing.writeInt(1);
        } else {
            outgoing.writeInt(0);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    String getUuid() {
        return uuid;
    }

    String getName() {
        return name;
    }

    String getType() {
        return type;
    }

    String getFamilyType() {
        return familyType;
    }

    String getFirmwareVersion() {
        return firmwareVersion;
    }

    boolean isRouter() {
        return router;
    }


    String getDisplayName() {
        String displayName = getName();
        if (displayName == null || displayName.length() == 0) {
            displayName = getUuid();
        }
        return displayName;
    }
}
