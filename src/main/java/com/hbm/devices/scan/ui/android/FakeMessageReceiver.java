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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hbm.devices.scan.AbstractMessageReceiver;

import java.util.LinkedList;
import java.util.List;

final class FakeMessageReceiver extends AbstractMessageReceiver {

    private boolean shallRun = true;
    private final Gson gson = new Gson();
    private static final String ADDRESS_KEY = "address";
    private static final String NETMASK_KEY = "netmask";
    private static final int IPV6_PREFIX = 64;
    private static final int HTTP_PORT = 80;
    private static final int SSH_PORT = 22;
    private static final int STREAMING_PORT = 7411;
    private static final int DEFAULT_EXPIRATION = 15;
    private static final int NUMBER_OF_ANNOUNCED_MODULES = 100;
    private static final int NUMBER_CONNECTABLE_MODULES = 10;
    private static final int NUMBER_OF_MODULE_GROUPS = 2;
    private static final int ANNOUNCE_PERIOD_MS = 6000;
    private static final int NEW_ANNOUNCE_PERIOD_MS = 1000;
    private final FakeMessageType messageType;
    private static int deviceCounter;

    private static final String[] devices = {
        "CX23R",
        "MX1601BR",
        "MX1609KBR",
        "MX1615BR",
        "MX411BR",
        "MX471BR",
        "MX840BR",
        "MX879B",
        "MX878B",
        "MX840B",
        "MX471B",
        "MX460B",
        "MX440B",
        "MX411P",
        "MX410B",
        "MX403B",
        "MX1615B",
        "MX1609TB",
        "MX1609T",
        "MX1609KB",
        "MX1601B",
        "MX430B",
        "MX809B",
        "MX238",
        "CX27B",
        "CX22B",
        "CX22W",
        "PMX"
    };

    FakeMessageReceiver(FakeMessageType messageType) {
        super();
        this.messageType = messageType;
    }

    @Override
    public void run() {
        if (messageType == FakeMessageType.NEW_DEVICE_EVERY_SECOND) {
            announceOneEverySecond();
        } else {
            announceAtSameTime();
        }
    }

    private void announceAtSameTime() {
        final List<String> deviceList = new LinkedList<>();
        for (int i = 0; i < FakeMessageReceiver.NUMBER_OF_ANNOUNCED_MODULES; i++) {
            deviceList.add(createAnnounceString(devices[getDeviceIndex()], i));
        }
        while (shallRun) {
            try {
                for (final String message : deviceList) {
                    setChanged();
                    notifyObservers(message);
                }
                Thread.sleep(ANNOUNCE_PERIOD_MS);
            } catch (InterruptedException e) {
                shallRun = false;
            }
        }
    }

    private void announceOneEverySecond() {
        int idCounter = 0;
        while (shallRun) {
            try {
                final String message = createAnnounceString(devices[getDeviceIndex()], idCounter);
                setChanged();
                notifyObservers(message);
                Thread.sleep(NEW_ANNOUNCE_PERIOD_MS);
                idCounter++;
            } catch (InterruptedException e) {
                shallRun = false;
            }
        }

    }

    @Override
    public void close() {
        shallRun = false;
    }

    private static int getDeviceIndex() {
        deviceCounter++;
        if (deviceCounter >= devices.length) {
            deviceCounter = 0;
        }
        return deviceCounter;
    }

    private static void composeDeviceSettings(JsonObject params, String name, String type, String uuid) {
        final JsonObject device = new JsonObject();
        params.add("device", device);
        device.addProperty("familyType", "QuantumX");
        device.addProperty("firmwareVersion", "1.234");
        device.addProperty("hardwareId", type + "_R0");
        device.addProperty("name", name);
        device.addProperty("type", type);
        device.addProperty("uuid", uuid);
        device.addProperty("isRouter", Boolean.FALSE);
    }

    private static void composeNetSettings(JsonObject params, int counter) {
        final JsonObject netSettings = new JsonObject();
        params.add("netSettings", netSettings);
        final JsonObject defaultGW = new JsonObject();
        netSettings.add("defaultGateway", defaultGW);
        defaultGW.addProperty("ipv4Address", R.string.defaultGW);
        final JsonObject iface = new JsonObject();
        netSettings.add("interface", iface);
        iface.addProperty("name", "eth0");
        iface.addProperty("type", "ethernet");
        iface.addProperty("description", "ethernet backplane side");

        final JsonArray ipv4Addresses = new JsonArray();
        iface.add("ipv4", ipv4Addresses);

        final JsonObject ipv4Entry = new JsonObject();
        ipv4Addresses.add(ipv4Entry);
        if ((counter / NUMBER_CONNECTABLE_MODULES) % NUMBER_OF_MODULE_GROUPS == 0) {
            ipv4Entry.addProperty(ADDRESS_KEY, R.string.ip1);
            ipv4Entry.addProperty(NETMASK_KEY, R.string.netmask1);
        } else {
            ipv4Entry.addProperty(ADDRESS_KEY, R.string.ip2);
            ipv4Entry.addProperty(NETMASK_KEY, R.string.netmask2);
        }

        final JsonObject apipa = new JsonObject();
        ipv4Addresses.add(apipa);
        apipa.addProperty(ADDRESS_KEY, R.string.apipaIP);
        apipa.addProperty(NETMASK_KEY, R.string.apipaNetmask);


        final JsonArray ipv6Addresses = new JsonArray();
        iface.add("ipv6", ipv6Addresses);

        final JsonObject ipv6Entry = new JsonObject();
        ipv6Addresses.add(ipv6Entry);
        ipv6Entry.addProperty(ADDRESS_KEY, R.string.ipv6address1);
        ipv6Entry.addProperty("prefix", IPV6_PREFIX);

        final JsonObject ipv6Entry2 = new JsonObject();
        ipv6Addresses.add(ipv6Entry2);
        ipv6Entry2.addProperty(ADDRESS_KEY, R.string.ipv6address2);
        ipv6Entry2.addProperty("prefix", IPV6_PREFIX);
    }

    private static void composeServices(JsonObject params) {
        final JsonArray services = new JsonArray();
        params.add("services", services);

        final JsonObject http = new JsonObject();
        services.add(http);
        http.addProperty("type", "http");
        http.addProperty("port", HTTP_PORT);

        final JsonObject ssh = new JsonObject();
        services.add(ssh);
        ssh.addProperty("type", "ssh");
        ssh.addProperty("port", SSH_PORT);

        final JsonObject daq = new JsonObject();
        services.add(daq);
        daq.addProperty("type", "daq");
        daq.addProperty("port", STREAMING_PORT);
    }

    private String createAnnounceString(String type, int idPostfix) {

        final String deviceName = type + '_' + idPostfix;
        final String uuid = "0009e50027" + Integer.toString(idPostfix);

        final JsonObject root = new JsonObject();
        root.addProperty("jsonrpc", "2.0");
        root.addProperty("method", "announce");
        final JsonObject params = new JsonObject();
        root.add("params", params);
        params.addProperty("expiration", DEFAULT_EXPIRATION);
        params.addProperty("apiVersion", "1.0");

        composeDeviceSettings(params, deviceName, type, uuid);
        composeNetSettings(params, idPostfix);
        composeServices(params);
        return gson.toJson(root);
    }
}
