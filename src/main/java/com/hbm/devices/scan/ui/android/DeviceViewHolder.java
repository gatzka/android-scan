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
import android.view.View;
import android.widget.TextView;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.Device;

final class DeviceViewHolder extends RecyclerView.ViewHolder {

    private final TextView tvModuleId;
    private final TextView tvModuleType;

    public DeviceViewHolder(View itemView) {
        super(itemView);

        tvModuleId = (TextView) itemView.findViewById(R.id.moduleID);
        tvModuleType = (TextView) itemView.findViewById(R.id.moduleType);
    }

    public void bind(Announce announce) {
        final Device device = announce.getParams().getDevice();
        final String displayName = getDisplayName(device);
        final String moduleType = getModuleType(device);

        tvModuleType.setText(moduleType);
        tvModuleId.setText(displayName);
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
}
