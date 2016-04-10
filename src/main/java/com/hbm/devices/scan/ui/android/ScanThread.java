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

import android.util.Log;

import com.hbm.devices.scan.AbstractMessageReceiver;
import com.hbm.devices.scan.ScanInterfaces;
import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.AnnounceDeserializer;
import com.hbm.devices.scan.announce.AnnounceReceiver;
import com.hbm.devices.scan.announce.ConnectionFinder;
import com.hbm.devices.scan.announce.DeviceMonitor;
import com.hbm.devices.scan.announce.LostDeviceEvent;
import com.hbm.devices.scan.announce.NewDeviceEvent;
import com.hbm.devices.scan.announce.ServiceEntry;
import com.hbm.devices.scan.announce.UpdateDeviceEvent;

import java.io.IOException;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

final class ScanThread extends Thread implements Observer {

    private static final int INITIAL_ANNOUNCE_CAPACITY = 100;

    private final AbstractMessageReceiver messageReceiver;
    private final DeviceMonitor deviceMonitor;
    private final BestConnectableAddressComparator addressComparator;
    private ConnectionFinder connectionFinder;
    private final List<Announce> collectedAnnounces;
    private final DeviceListFragment listFragment;

    ScanThread(DeviceListFragment listFragment, boolean useFakeMessages, FakeMessageType fakeMessageType) throws IOException {
        super("device scan thread");

        collectedAnnounces = new ArrayList<>(INITIAL_ANNOUNCE_CAPACITY);
        addressComparator = new BestConnectableAddressComparator();
        this.listFragment = listFragment;

        deviceMonitor = new DeviceMonitor();
        deviceMonitor.addObserver(this);
        final AnnounceDeserializer announceParser = new AnnounceDeserializer();
        announceParser.addObserver(deviceMonitor);
        if (useFakeMessages) {
            messageReceiver = new FakeMessageReceiver(fakeMessageType);
        } else {
            messageReceiver = new AnnounceReceiver();
        }
        messageReceiver.addObserver(announceParser);
    }

    @Override
    public void run() {
        try {
            final ScanInterfaces interfaces = new ScanInterfaces();
            connectionFinder = new ConnectionFinder(interfaces.getInterfaces());
            messageReceiver.run();
        } catch (SocketException e) {
            Log.e(ScanActivity.LOG_TAG, "Could not get list of network interfaces!", e);
        }
    }

    void finish() {
        messageReceiver.deleteObservers();
        messageReceiver.close();
        deviceMonitor.deleteObservers();
        deviceMonitor.close();
        this.interrupt();
    }

    @Override
    public void update(Observable observable, Object arg) {
        if (arg instanceof NewDeviceEvent) {
            final NewDeviceEvent event = (NewDeviceEvent) arg;
            final Announce announce = event.getAnnounce();
            findConnectableAddress(announce);
            collectedAnnounces.add(announce);
        } else if (arg instanceof LostDeviceEvent) {
            final LostDeviceEvent event = (LostDeviceEvent) arg;
            remove(event.getAnnounce());
        } else if (arg instanceof UpdateDeviceEvent) {
            final UpdateDeviceEvent event = (UpdateDeviceEvent) arg;
            final Announce announce = event.getNewAnnounce();
            findConnectableAddress(announce);
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

    private void findConnectableAddress(Announce announce) {
        List<InetAddress> addresses = connectionFinder.getConnectableAddresses(announce);
        addresses = removeIPv6LinkLocal(addresses);
        Collections.sort(addresses, addressComparator);
        if (!addresses.isEmpty()) {
            final InetAddress address = addresses.get(0);
            final int httpPort = getHttpPort(announce);
            if (httpPort != -1) {
                final InetSocketAddress isa = new InetSocketAddress(address, httpPort);
                announce.setCookie(isa);
            }
        }
    }

    private int getHttpPort(Announce announce) {
        final List<ServiceEntry> entries = announce.getParams().getServices();
        for (final ServiceEntry entry : entries) {
            if (ServiceEntry.SERVICE_HTTP.equals(entry.getType())) {
                return entry.getPort();
            }
        }
        return -1;
    }

    private static List<InetAddress> removeIPv6LinkLocal(List<InetAddress> addresses) {
        final Iterator<InetAddress> iterator = addresses.iterator();
        while (iterator.hasNext()) {
            final InetAddress address = iterator.next();
            if ((address instanceof Inet6Address) && address.isLinkLocalAddress()) {
                iterator.remove();
            }
        }
        return addresses;
    }

    private static class BestConnectableAddressComparator implements Comparator<InetAddress>, Serializable {

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

        private static int compareLinkLocal(InetAddress first, InetAddress second) {
            if (!first.isLinkLocalAddress() && second.isLinkLocalAddress()) {
                return -1;
            } else if (first.isLinkLocalAddress() && !second.isLinkLocalAddress()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}

enum FakeMessageType {
    CONSTANT_NUMBER_OF_DEVICES,
    NEW_DEVICE_EVERY_SECOND
}
