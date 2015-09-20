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
import android.app.FragmentManager;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.Device;

final class ModuleListAdapter extends BaseAdapter {

    private List<Announce> filteredAnnounces;
    private final LayoutInflater layoutInflater;
    private final DeviceListFragment listFragment;
    private final DevicesFragment fragment;
    private final Activity activity;

    public ModuleListAdapter(DevicesFragment fragment) {
        super();
        this.fragment = fragment;
        this.activity = fragment.getActivity();
        filteredAnnounces = new ArrayList<Announce>();
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final FragmentManager manager = activity.getFragmentManager();
        listFragment = (DeviceListFragment) manager.findFragmentByTag("deviceListFragment");
        listFragment.setAdapter(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        final Announce announce = filteredAnnounces.get(position);
        View displayView = convertView;
        if (announce != null) {
            int color;
            if (announce.getCookie() == null) {
                color = ContextCompat.getColor(activity, R.color.color_not_connectable);
            } else {
                color = ContextCompat.getColor(activity, R.color.color_connectable);
            }

            ViewHolderItem viewHolder;
            if (displayView == null) {
                displayView = layoutInflater.inflate(R.layout.device_item, parent, false);
                viewHolder = new ViewHolderItem();
                viewHolder.moduleType = (TextView) displayView.findViewById(R.id.moduleType);
                viewHolder.moduleID = (TextView) displayView.findViewById(R.id.moduleID);
                viewHolder.info = (ImageView) displayView.findViewById(R.id.right);
                viewHolder.info.setTag(position);
                viewHolder.info.setOnClickListener(fragment);
                displayView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolderItem) displayView.getTag();
            }

            final Device device = announce.getParams().getDevice();
            final String displayName = getDisplayName(device);

            final String moduleType = getModuleType(device);
            viewHolder.moduleType.setText(moduleType);
            viewHolder.moduleType.setTextColor(color);
            viewHolder.moduleID.setText(displayName);
            viewHolder.moduleID.setTextColor(color);
        }

        return displayView;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getJSONString().hashCode();
    }

    @Override
    public Announce getItem(int position) {
        return filteredAnnounces.get(position);
    }

    @Override
    public int getCount() {
        return filteredAnnounces.size();
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }
    
    @Override
    public boolean hasStableIds() {
        return true;
    }

    void notifyList(List<Announce> filteredAnnounces) {
        this.filteredAnnounces = filteredAnnounces;
        notifyDataSetChanged();
    }

    private String getModuleType(final Device device) {
        String moduleType = device.getType();
        if (moduleType == null || moduleType.length() == 0) {
            moduleType = "Unknown";
        }
        return moduleType;
    }

    private String getDisplayName(Device device) {
        String displayName = device.getName();
        if (displayName == null || displayName.length() == 0) {
            displayName = device.getUuid();
        }
        return displayName;
    }

    void setFilterString(String filterString) {
        listFragment.setFilterString(filterString);
    }

    String getFilterString() {
        return listFragment.getFilterString();
    }

    boolean isPaused() {
        return listFragment.isPaused();
    }

    void resumeDeviceUpdates() {
        listFragment.setPaused(false);
    }

    void pauseDeviceUpdates() {
        listFragment.setPaused(true);
    }


    private static final class ViewHolderItem {
        private TextView moduleType;
        private TextView moduleID;
        private ImageView info;
    }
}
