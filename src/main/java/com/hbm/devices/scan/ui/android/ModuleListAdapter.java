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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hbm.devices.scan.announce.Announce;

final class ModuleListAdapter extends RecyclerView.Adapter<DeviceViewHolder> implements DisplayNotifier {

    private final List<Announce> filteredAnnounces;
    private final DeviceListFragment listFragment;
    private final DisplayUpdateEventGenerator eventGenerator;

    ModuleListAdapter(DeviceListFragment fragment) {
        super();
        eventGenerator = new DisplayUpdateEventGenerator(this);
        filteredAnnounces = new ArrayList<>();
        listFragment = fragment;
        this.setHasStableIds(true);

        listFragment.setAdapter(this);
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dev_item, parent, false);
        return new DeviceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        final Announce announce = filteredAnnounces.get(position);
        holder.bind(announce);
    }

    @Override
    public long getItemId(int position) {
        final Announce announce = filteredAnnounces.get(position);
        return announce.getCommunicationPathId();
    }

    @Override
    public int getItemCount() {
        return filteredAnnounces.size();
    }

    @Override
    public void notifyRemoveAt(int position) {
        notifyItemRemoved(position);
    }

    @Override
    public void notifyAddAt(int position) {
        notifyItemInserted(position);
    }

    @Override
    public void notifyChangeAt(int position) {
        notifyItemChanged(position);
    }

    @Override
    public void notifyMoved(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
    }

    void notifyList(List<Announce> newFilteredAnnounces) {
        eventGenerator.compareLists(filteredAnnounces, newFilteredAnnounces);
    }

    void setFilterString(String filterString) {
        listFragment.setFilterString(filterString);
    }

    List<Announce> getFilteredAnnounces() {
        return Collections.unmodifiableList(filteredAnnounces);
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
}
