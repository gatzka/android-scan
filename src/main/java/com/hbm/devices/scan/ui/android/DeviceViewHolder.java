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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.InetSocketAddress;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.Device;

final class DeviceViewHolder extends RecyclerView.ViewHolder {

    private final TextView tvModuleId;
    private final TextView tvModuleType;
    private final TextView tvModuleName;
    private final ImageView devicePhoto;
    private final ImageButton infoButton;
    private final CardView cardView;
    private final Context context;
    private final Drawable blackInfo;
    private final Drawable whiteInfo;
    private Announce announce;

    public DeviceViewHolder(View itemView) {
        super(itemView);

        context = itemView.getContext();

        tvModuleId = (TextView) itemView.findViewById(R.id.moduleID);
        tvModuleType = (TextView) itemView.findViewById(R.id.moduleType);
        tvModuleName = (TextView) itemView.findViewById(R.id.moduleName);
        devicePhoto = (ImageView) itemView.findViewById(R.id.device_photo);
        infoButton = (ImageButton) itemView.findViewById(R.id.infoButton);
        cardView = (CardView) ((LinearLayout) itemView).getChildAt(0);
        blackInfo = ContextCompat.getDrawable(context, R.drawable.ic_info_outline_black_48dp);
        setImageAlpha(blackInfo, 87);
        whiteInfo = ContextCompat.getDrawable(context, R.drawable.ic_info_outline_white_48dp);
    }

    public void bind(Announce a) {
        this.announce = a;
        final Device device = announce.getParams().getDevice();
        final String displayName = getDisplayName(device);
        final String moduleType = getModuleType(device);
        final String uuid = device.getUuid();

        if (announce.getCookie() == null) {
            int color = ContextCompat.getColor(context, R.color.color_not_connectable);
            cardView.setCardBackgroundColor(color);
            tvModuleType.setTextColor(ContextCompat.getColor(context, android.R.color.primary_text_dark));
            tvModuleId.setTextColor(ContextCompat.getColor(context, android.R.color.secondary_text_dark));
            tvModuleName.setTextColor(ContextCompat.getColor(context, android.R.color.secondary_text_dark));
            infoButton.setImageDrawable(whiteInfo);
        } else {
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.background_light));
            tvModuleType.setTextColor(setTextAlpha(ContextCompat.getColor(context, android.R.color.primary_text_light), 87));
            tvModuleName.setTextColor(setTextAlpha(ContextCompat.getColor(context, android.R.color.secondary_text_light), 87));
            tvModuleId.setTextColor(setTextAlpha(ContextCompat.getColor(context, android.R.color.secondary_text_light), 87));
            infoButton.setImageDrawable(blackInfo);
        }

        tvModuleType.setText(moduleType);
        tvModuleName.setText(displayName);
        tvModuleId.setText(uuid);
        devicePhoto.setImageResource(R.drawable.mx840b);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final InetSocketAddress address  = (InetSocketAddress) announce.getCookie();
                if (address == null) {
                //TODO: showConfigure(announce);
                } else {
                    openBrowser(address);
                }
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("click on " + announce.getParams().getDevice().getUuid());
            }
        });

    }

    private static void setImageAlpha(Drawable draw, int alphaPercent) {
        int alpha = alphaPercent * 255 / 100;
        draw.setAlpha(alpha);
    }

    private static int setTextAlpha(int color, int alphaPercent) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        int alpha = alphaPercent * 255 / 100;
        return Color.argb(alpha, red, green, blue);
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

    private void openBrowser(InetSocketAddress address) {
        final BrowserStartTask browserTask = new BrowserStartTask(context);
        browserTask.execute(address);
    }
}
