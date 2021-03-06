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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.Filter;
import android.widget.Toast;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.Device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static android.net.wifi.WifiManager.WIFI_MODE_FULL_HIGH_PERF;

/**
 * Retained fragment holding the announced devices. It is necessary to retain this fragment to let the list of
 * devices survive configuration events.
 */
public final class DeviceListFragment extends Fragment implements OnSharedPreferenceChangeListener {

    @NonNull protected final AtomicReference<ModuleListAdapter> adapter = new AtomicReference<>();
    @Nullable protected AtomicReference<List<Announce>> collectedAnnounces;
    private boolean paused;
    private String filterString;
    private ScanThread scanThread;
    private DeviceFilter deviceFilter;
    private WifiLock wifiLock;
    private MulticastLock mcLock;

    private String prefUseFakeMessages;
    private String prefFakeMessageType;

    /**
     * Default constructor for this Fragment. This constructor shall never be called directly from code. Fragments
     * are instantiated by the Android runtime.
     */
    public DeviceListFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        collectedAnnounces = new AtomicReference<>();
        deviceFilter = new DeviceFilter();

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        startScanThread(sharedPreferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*
         * Probably a good idea to update UI only after this method was called.
         * Then we should have a complete view. In onDestroyView we can stop updating UI.
         * Probably a better idea is to start/stop updating UI in onResume/onPause.
         * Has to be clarified if these methods are called when activity goes into background.
         */
        final ScanActivity activity = (ScanActivity) getActivity();
        final WifiManager wifi = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            final Toast failureToast = Toast.makeText(activity,
                    activity.getString(R.string.could_not_get_wifimanager), Toast.LENGTH_SHORT);
            failureToast.show();
        } else {
            if (wifiLock == null || !wifiLock.isHeld()) {
                wifiLock = wifi.createWifiLock( WIFI_MODE_FULL_HIGH_PERF, "wifi lock");
                wifiLock.acquire();
            }
            if (mcLock == null || !mcLock.isHeld()) {
                mcLock = wifi.createMulticastLock("multicast lock");
                mcLock.acquire();
            }
        }
        if (adapter.get() != null) {
            updateList();
        }

        prefUseFakeMessages = getString(R.string.pref_use_fake_messages);
        prefFakeMessageType = getString(R.string.pref_fake_message_type);
    }

    @Override
    public void onDestroy() {
        adapter.set(null);
        stopScanThread();
        mcLock.release();
        wifiLock.release();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        adapter.set(null);
        super.onDetach();
    }

    @Override
    public void onSharedPreferenceChanged(@NonNull SharedPreferences sharedPreferences, @NonNull String key) {
        Activity activity = getActivity();
        if (!isAdded() || (activity == null)) {
            return;
        }

        if (key.equals(prefUseFakeMessages) || key.equals(prefFakeMessageType)) {
            stopScanThread();
            startScanThread(sharedPreferences);
        }
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
        if (deviceFilter != null) {
            deviceFilter.filter(filterString);
        }
    }

    private void startScanThread(SharedPreferences sharedPreferences) {
        final boolean useFakeMessages = sharedPreferences.getBoolean(getString(R.string.pref_use_fake_messages), false);
        final String fakeMessageType = sharedPreferences.getString(getString(R.string.pref_fake_message_type),
                getString(R.string.default_fake_type));

        FakeMessageType messageType;
        if (fakeMessageType.equals(getString(R.string.new_dev_every_second))) {
            messageType = FakeMessageType.NEW_DEVICE_EVERY_SECOND;
        } else {
            messageType = FakeMessageType.CONSTANT_NUMBER_OF_DEVICES;
        }

        notify(new ArrayList<>());
        try {
            scanThread = new ScanThread(this, useFakeMessages, messageType);
            scanThread.start();
        } catch (IOException e) {
            final ScanActivity activity = (ScanActivity) getActivity();
            final Toast failureToast = Toast.makeText(activity,
                    activity.getString(R.string.no_thread_start), Toast.LENGTH_SHORT);
            failureToast.show();
        }
    }

    private void stopScanThread() {
        scanThread.finish();
        try {
            scanThread.join();
        } catch (InterruptedException e) {
            final ScanActivity activity = (ScanActivity) getActivity();
            final Toast failureToast = Toast.makeText(activity,
                    activity.getString(R.string.interrupted_join), Toast.LENGTH_SHORT);
            failureToast.show();
        }
    }

    class DeviceFilter extends Filter {
        @NonNull
        @Override
        protected FilterResults performFiltering(@Nullable final CharSequence constraint) {
            final FilterResults filteredResults = new FilterResults();
            CharSequence filterConstraint = "";
            if (constraint != null) {
                filterConstraint = constraint;
            }
            final String upperCaseConstraint = filterConstraint.toString().toUpperCase(Locale.US);
            final List<Announce> filteredAnnounces = new ArrayList<>();
            final List<Announce> announceList = collectedAnnounces.get();
            for (final Announce announce : announceList) {
                if (displayNameMatches(announce, upperCaseConstraint) ||
                    moduleTypeMatches(announce, upperCaseConstraint) ||
                    uuidMatches(announce, upperCaseConstraint)) {
                    filteredAnnounces.add(announce);
                }
            }
            filteredResults.values = filteredAnnounces;
            filteredResults.count = filteredAnnounces.size();
            return filteredResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, @NonNull FilterResults results) {
            final List<Announce> filteredAnnounces = (List<Announce>) results.values;
            final ModuleListAdapter a = adapter.get();
            if (a != null) {
                a.notifyList(filteredAnnounces);
            }
        }

        private boolean displayNameMatches(final Announce announce, @NonNull final CharSequence constraint) {
            final Device device = announce.getParams().getDevice();
            final String name = getDisplayName(device).toUpperCase(Locale.US);
            return name.contains(constraint);
        }

        private boolean moduleTypeMatches(final Announce announce, @NonNull final CharSequence constraint) {
            final Device device = announce.getParams().getDevice();
            final String type = device.getType().toUpperCase(Locale.US);
            return type.contains(constraint);
        }

        private boolean uuidMatches(final Announce announce, @NonNull final CharSequence constraint) {
            final Device device = announce.getParams().getDevice();
            final String uuid = device.getUuid().toUpperCase(Locale.US);
            return uuid.contains(constraint);
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

