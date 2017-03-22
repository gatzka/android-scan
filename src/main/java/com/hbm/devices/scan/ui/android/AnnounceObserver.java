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

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.LostDeviceEvent;
import com.hbm.devices.scan.announce.NewDeviceEvent;
import com.hbm.devices.scan.announce.UpdateDeviceEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

class AnnounceObserver implements Observer {

    private static final int INITIAL_ANNOUNCE_CAPACITY = 100;

    private final List<Announce> collectedAnnounces;
    private final DeviceListFragment listFragment;

    AnnounceObserver(DeviceListFragment listFragment) {
        this.listFragment = listFragment;
        collectedAnnounces = new ArrayList<>(INITIAL_ANNOUNCE_CAPACITY);
    }

    @Override
    public void update(Observable observable, Object arg) {
        if (arg instanceof NewDeviceEvent) {
            final NewDeviceEvent event = (NewDeviceEvent) arg;
            final Announce announce = event.getAnnounce();
            collectedAnnounces.add(announce);
        } else if (arg instanceof LostDeviceEvent) {
            final LostDeviceEvent event = (LostDeviceEvent) arg;
            remove(event.getAnnounce());
        } else if (arg instanceof UpdateDeviceEvent) {
            final UpdateDeviceEvent event = (UpdateDeviceEvent) arg;
            final Announce announce = event.getNewAnnounce();
            remove(announce);
            collectedAnnounces.add(announce);
        }
        final List<Announce> copiedList = new ArrayList<>(collectedAnnounces);
        listFragment.notify(copiedList);
    }

    private void remove(Announce announce) {
        final int count = collectedAnnounces.size();
        for (int i = count - 1; i >= 0; i--) {
            final Announce element = collectedAnnounces.get(i);
            if (element.sameCommunicationPath(announce)) {
                collectedAnnounces.remove(i);
            }
        }
    }
}
