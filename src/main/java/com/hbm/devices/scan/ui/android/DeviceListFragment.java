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

import android.content.Context;
import android.net.wifi.WifiManager.MulticastLock;
import android.net.wifi.WifiManager.WifiLock;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.Device;

public final class DeviceListFragment extends Fragment {

    private AtomicReference<ModuleListAdapter> adapter = new AtomicReference<ModuleListAdapter>();
    private AtomicReference<List<Announce>> collectedAnnounces;
    private List<Announce> filteredAnnounces;
    private boolean paused;
    private String filterString;
    private ScanThread scanThread;
    private DeviceFilter deviceFilter;
    private WifiLock wifiLock;
    private MulticastLock mcLock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        collectedAnnounces = new AtomicReference<List<Announce>>(new ArrayList<Announce>());
        deviceFilter = new DeviceFilter();
        try {
            scanThread = new ScanThread(this);
            scanThread.start();
        } catch (IOException e) {
            Log.e(ScanActivity.LOG_TAG, "Can't start thread!", e);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*
         * Probably a good idea to update UI only after this method was called.
         * Then we should have a complete view. In onDestroyView we can stop updating UI.
         * Probably a better idea is to start/stop updating UI in onResume/onPause. 
         * Has to be clarified if these methods are called when activity goes into background.
         */
        final ScanActivity activity = (ScanActivity) getActivity();
        final WifiManager wifi = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            Log.e(ScanActivity.LOG_TAG, "Could not get WifiManager");
        } else {
            if (wifiLock == null || !wifiLock.isHeld()) {
                wifiLock = wifi.createWifiLock("wifi lock");
                wifiLock.acquire();
            }
            if (mcLock == null || !mcLock.isHeld()) {
                mcLock = wifi.createMulticastLock("multicast lock");
                mcLock.acquire();
            }
        }
    }

    @Override
    public void onDestroy() {
        adapter.set(null);
        scanThread.finish();
        try {
            scanThread.join();
        } catch (InterruptedException e) {
            Log.d(ScanActivity.LOG_TAG, "Interrupt while joining thread", e);
        }
        mcLock.release();
        wifiLock.release();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        adapter.set(null);
        super.onDetach();
    }

    boolean isPaused() {
        return paused;
    }

    void setPaused(boolean paused) {
        this.paused = paused;
        if (!paused) {
            updateList();
        }
    }

    String getFilterString() {
        return filterString;
    }

    void setFilterString(String filterString) {
        this.filterString = filterString;
        updateList();
    }
    
    void notify(List<Announce> announces) {
        collectedAnnounces.set(announces);
        if (!paused) {
            updateList();
        }
    }

    void setAdapter(ModuleListAdapter adapter) {
        this.adapter.set(adapter);
        updateList();
    }

    private void updateList() {
        deviceFilter.filter(filterString);
    }

    private class DeviceFilter extends Filter {
        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {
            final FilterResults filteredResults = new FilterResults();
            CharSequence filterConstraint = "";
            if (constraint != null) {
                filterConstraint = constraint;
            }
            final String upperCaseConstraint = filterConstraint.toString().toUpperCase(Locale.US);
            final List<Announce> filt = new ArrayList<Announce>();
            final List<Announce> announceList = collectedAnnounces.get();
            for (final Announce announce : announceList) {
                if (displayNameMatches(announce, upperCaseConstraint)
                    || moduleTypeMatches(announce, upperCaseConstraint)) {
                    filt.add(announce);
                }
            }
            filteredResults.values = filt;
            filteredResults.count = filt.size();
            return filteredResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredAnnounces = (ArrayList<Announce>) results.values;
            final ModuleListAdapter a = adapter.get();
            if (a != null) {
                a.notifyList(filteredAnnounces);
            }
        }

        private boolean displayNameMatches(final Announce announce, final CharSequence constraint) {
            final Device device = announce.getParams().getDevice();
            final String name = getDisplayName(device).toUpperCase(Locale.US);
            return name.contains(constraint);
        }

        private boolean moduleTypeMatches(final Announce announce, final CharSequence constraint) {
            final Device device = announce.getParams().getDevice();
            final String type = device.getType().toUpperCase(Locale.US);
            return type.contains(constraint);
        }

        private String getDisplayName(Device device) {
            String displayName = device.getName();
            if (displayName == null || displayName.length() == 0) {
                displayName = device.getUuid();
            }
            return displayName;
        }
    }
}
