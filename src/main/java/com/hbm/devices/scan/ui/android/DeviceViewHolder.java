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
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.Device;
import com.squareup.picasso.Picasso;
final class DeviceViewHolder extends RecyclerView.ViewHolder {

    public static final String DETAILS = "Details";

    private final TextView tvModuleId;
    private final TextView tvModuleType;
    private final TextView tvModuleName;
    private final ImageView devicePhoto;
    private final ImageButton infoButton;
    @NonNull
    private final CardView cardView;
    protected final Context context;

    protected Announce announce;

    DeviceViewHolder(@NonNull CardView itemView) {
        super(itemView);

        context = itemView.getContext();

        tvModuleId = itemView.findViewById(R.id.moduleID);
        tvModuleType = itemView.findViewById(R.id.moduleType);
        tvModuleName = itemView.findViewById(R.id.moduleName);
        devicePhoto = itemView.findViewById(R.id.device_photo);
        infoButton = itemView.findViewById(R.id.infoButton);
        cardView = itemView;

    }

    void bind(@NonNull Announce a) {
        this.announce = a;
        final Device device = announce.getParams().getDevice();
        final String displayName = getDisplayName(device);
        final String moduleType = getModuleType(device);
        final String uuid = device.getUuid();

        tvModuleType.setText(moduleType);
        tvModuleName.setText(displayName);
        tvModuleId.setText(uuid);
        devicePhoto.setImageDrawable(null);
        final Picasso picasso = Picasso.get();
        picasso.load(getImageResourceId(a)).into(devicePhoto);

        cardView.setOnClickListener(new ModuleCardClickListener(announce, moduleType));

        infoButton.setOnClickListener(v -> {
            final Intent intent = new Intent(context, DeviceDetailsActivity.class);
            intent.putExtra(DETAILS, announce);
            ActivityCompat.startActivity(context, intent, null);
            ((ScanActivity) context).overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        });
    }

    private static int getImageResourceId(Announce announce) {
        String key = announce.getParams().getDevice().getLabel();
        if (key == null || key.isEmpty()) {
            key = announce.getParams().getDevice().getType();
        }

        if (key == null || key.isEmpty()) {
            return R.drawable.ic_no_device;
        }
        return ImageResourceCache.getResourceFromCache(key);
    }

    private String getModuleType(final Device device) {
        String moduleType = device.getType();
        if (moduleType == null || moduleType.length() == 0) {
            moduleType = context.getString(R.string.unknown);
        }

        return moduleType;
    }

    private String getDisplayName(final Device device) {
        String displayName = device.getName();
        if (displayName == null || displayName.length() == 0) {
            displayName = context.getString(R.string.unknown);
        }

        return displayName;
    }
}
