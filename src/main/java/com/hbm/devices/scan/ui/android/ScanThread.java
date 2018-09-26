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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hbm.devices.scan.AbstractMessageReceiver;
import com.hbm.devices.scan.announce.AnnounceDeserializer;
import com.hbm.devices.scan.announce.AnnounceReceiver;
import com.hbm.devices.scan.announce.DeviceMonitor;

import java.io.IOException;

final class ScanThread extends Thread {

    @Nullable
    private final AbstractMessageReceiver messageReceiver;
    @NonNull
    private final DeviceMonitor deviceMonitor;

    ScanThread(@NonNull DeviceListFragment listFragment, boolean useFakeMessages,
               FakeMessageType fakeMessageType) throws IOException {
        super("device scan thread");

        deviceMonitor = new DeviceMonitor();
        final AnnounceObserver announceObserver = new AnnounceObserver(listFragment);
        deviceMonitor.addObserver(announceObserver);

        final AnnounceDeserializer announceParser = new AnnounceDeserializer();
        announceParser.addObserver(deviceMonitor);
        if (useFakeMessages) {
            messageReceiver = new FakeMessageReceiver(fakeMessageType, listFragment.getActivity());
        } else {
            messageReceiver = new AnnounceReceiver();
        }
        messageReceiver.addObserver(announceParser);
    }

    @Override
    public void run() {
        messageReceiver.run();
    }

    void finish() {
        messageReceiver.deleteObservers();
        messageReceiver.close();
        deviceMonitor.deleteObservers();
        deviceMonitor.close();
        this.interrupt();
    }
}

enum FakeMessageType {
    CONSTANT_NUMBER_OF_DEVICES,
    NEW_DEVICE_EVERY_SECOND
}
