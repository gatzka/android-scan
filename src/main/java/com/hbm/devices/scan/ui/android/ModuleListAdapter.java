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
import java.util.List;

import com.hbm.devices.scan.announce.Announce;

final class ModuleListAdapter extends RecyclerView.Adapter<DeviceViewHolder> {

    private List<Announce> filteredAnnounces;
    private final DeviceListFragment listFragment;

    public ModuleListAdapter(DeviceListFragment fragment) {
        super();
        filteredAnnounces = new ArrayList<>();
        listFragment = fragment;

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
    public int getItemCount() {
        return filteredAnnounces.size();
    }

    private void applyAndAnimateRemovals(List<Announce> newAnnounces) {
        for (int i = filteredAnnounces.size() - 1; i >= 0; i--) {
            final Announce model = filteredAnnounces.get(i);
            if (!newAnnounces.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<Announce> newAnnounces) {
        for (int i = 0, count = newAnnounces.size(); i < count; i++) {
            final Announce model = newAnnounces.get(i);
            if (!filteredAnnounces.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<Announce> newAnnounces) {
        for (int toPosition = newAnnounces.size() - 1; toPosition >= 0; toPosition--) {
            final Announce model = newAnnounces.get(toPosition);
            final int fromPosition = filteredAnnounces.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private Announce removeItem(int position) {
        final Announce announce = filteredAnnounces.remove(position);
        notifyItemRemoved(position);
        return announce;
    }

    private void addItem(int position, Announce announce) {
        filteredAnnounces.add(position, announce);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final Announce announce = filteredAnnounces.remove(fromPosition);
        filteredAnnounces.add(toPosition, announce);
        notifyItemMoved(fromPosition, toPosition);
    }

    void notifyList(List<Announce> filteredAnnounces) {
        applyAndAnimateRemovals(filteredAnnounces);
        applyAndAnimateAdditions(filteredAnnounces);
        applyAndAnimateMovedItems(filteredAnnounces);
    }

    void setFilterString(String filterString) {
        listFragment.setFilterString(filterString);
    }

    String getFilterString() {
       return listFragment.getFilterString();
    }

    List<Announce> getFilteredAnnounces() {
        return filteredAnnounces;
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
