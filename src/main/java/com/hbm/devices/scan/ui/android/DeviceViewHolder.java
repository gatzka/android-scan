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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.InetSocketAddress;
import java.util.HashMap;

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.Device;

import com.squareup.picasso.Picasso;

final class DeviceViewHolder extends RecyclerView.ViewHolder {

	static final String DETAILS = "Details";

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

    private final int cardBackgroundNotConntectable;
    private final int cardBackgroundConntectable;
    private final int moduleTypeTextColorNotConnectable;
    private final int moduleTypeTextColorConnectable;
    private final int moduleNameTextColorNotConnectable;
    private final int moduleNameTextColorConnectable;
    private final int moduleIdTextColorNotConnectable;
    private final int moduleIdTextColorConnectable;
    private final int alpha;
    private final String unknown;

    private static final HashMap<String, Integer> resourceCache = new HashMap<>();

    public DeviceViewHolder(View itemView) {
        super(itemView);

        context = itemView.getContext();

        tvModuleId = (TextView) itemView.findViewById(R.id.moduleID);
        tvModuleType = (TextView) itemView.findViewById(R.id.moduleType);
        tvModuleName = (TextView) itemView.findViewById(R.id.moduleName);
        devicePhoto = (ImageView) itemView.findViewById(R.id.device_photo);
        infoButton = (ImageButton) itemView.findViewById(R.id.infoButton);
        cardView = (CardView) itemView;
        blackInfo = ContextCompat.getDrawable(context, R.drawable.ic_info_outline_black_48dp);
        alpha = context.getResources().getInteger(R.integer.text_alpha);
        setImageAlpha(blackInfo, alpha);
        whiteInfo = ContextCompat.getDrawable(context, R.drawable.ic_info_outline_white_48dp);

        cardBackgroundNotConntectable = ContextCompat.getColor(context, R.color.color_not_connectable);
        cardBackgroundConntectable = ContextCompat.getColor(context, android.R.color.background_light);
        moduleTypeTextColorNotConnectable = ContextCompat.getColor(context, android.R.color.primary_text_dark);
        moduleTypeTextColorConnectable = ContextCompat.getColor(context, android.R.color.primary_text_light);
        moduleNameTextColorNotConnectable = ContextCompat.getColor(context, android.R.color.secondary_text_dark);
        moduleNameTextColorConnectable = ContextCompat.getColor(context, android.R.color.secondary_text_light);
        moduleIdTextColorNotConnectable = ContextCompat.getColor(context, android.R.color.secondary_text_dark);
        moduleIdTextColorConnectable = ContextCompat.getColor(context, android.R.color.secondary_text_light);
        unknown = itemView.getContext().getString(R.string.unknown);
    }

    public void bind(Announce a) {
        this.announce = a;
        final Device device = announce.getParams().getDevice();
        final String displayName = getDisplayName(device);
        final String moduleType = getModuleType(device);
        final String uuid = device.getUuid();

        if (announce.getCookie() == null) {
            cardView.setCardBackgroundColor(cardBackgroundNotConntectable);
            tvModuleType.setTextColor(moduleTypeTextColorNotConnectable);
            tvModuleName.setTextColor(moduleNameTextColorNotConnectable);
            tvModuleId.setTextColor(moduleIdTextColorNotConnectable);
            infoButton.setImageDrawable(whiteInfo);
        } else {
            cardView.setCardBackgroundColor(cardBackgroundConntectable);
            tvModuleType.setTextColor(setTextAlpha(moduleTypeTextColorConnectable, alpha));
            tvModuleName.setTextColor(setTextAlpha(moduleNameTextColorConnectable, alpha));
            tvModuleId.setTextColor(setTextAlpha(moduleIdTextColorConnectable, alpha));
            infoButton.setImageDrawable(blackInfo);
        }

        tvModuleType.setText(moduleType);
        tvModuleName.setText(displayName);
        tvModuleId.setText(uuid);
        devicePhoto.setImageDrawable(null);
        Picasso picasso = Picasso.with(context);
        picasso.load(getImageResourceId(a)).into(devicePhoto);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final InetSocketAddress address  = (InetSocketAddress) announce.getCookie();
                if (address != null) {
                    openBrowser(address);
                }
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, DeviceDetailsActivity.class);
                intent.putExtra(DETAILS, announce);
                ActivityCompat.startActivity((ScanActivity) context, intent, null);
                ((ScanActivity) context).overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            }
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
        return getResourceFromCache(key);
    }

    private static int getResourceFromCache(String key) {
        Integer resourceId = resourceCache.get(key);
        if (resourceId == null) {
            resourceId = resolveResourceId(key);
            resourceCache.put(key, resourceId);
        }
        return resourceId;
    }

    private static int resolveResourceId(String key) {
        if (key.equals("CX23R")) {
            return R.drawable.cx23;
        }

        if (key.equals("MX1601") || key.equals("MX1601B")) {
            return R.drawable.mx1601b;
        }
        if (key.equals("MX1601BR")) {
            return R.drawable.mx1601br;
        }

        if (key.equals("MX1609KBR")) {
            return R.drawable.mx1609kbr;
        }
        if (key.equals("MX1609") || key.equals("MX1609KB")) {
            return R.drawable.mx1609kb;
        }

        if (key.equals("MX1609TB")) {
            return R.drawable.mx1609tb;
        }
        if (key.equals("MX1609T")) {
            return R.drawable.mx1609t;
        }

        if (key.equals("MX1615BR")) {
            return R.drawable.mx1615br;
        }

        if (key.equals("MX1615B") || key.equals("MX1615")) {
            return R.drawable.mx1615b;
        }

        if (key.equals("MX411BR")) {
            return R.drawable.mx411br;
        }

        if (key.equals("MX411P")) {
            return R.drawable.mx411p;
        }

        if (key.equals("MX410") || key.equals("MX410B")) {
            return R.drawable.mx410b;
        }

        if (key.equals("MX471BR")) {
            return R.drawable.mx471br;
        }

        if (key.equals("MX471") || key.equals("MX471B")) {
            return R.drawable.mx471b;
        }

        if (key.equals("MX879") || key.equals("MX879B")) {
            return R.drawable.mx879b;
        }

        if (key.equals("MX878") || key.equals("MX878B")) {
            return R.drawable.mx878b;
        }

        if (key.equals("MX460") || key.equals("MX460B")) {
            return R.drawable.mx460b;
        }

        if (key.equals("MX440") || key.equals("MX440A") || key.equals("MX440B")) {
            return R.drawable.mx440b;
        }

        if (key.equals("MX403") || key.equals("MX403B")) {
            return R.drawable.mx403b;
        }

        if (key.equals("CX27") || key.equals("CX27B")) {
            return R.drawable.cx27b;
        }

        if (key.equals("CX22B")) {
            return R.drawable.cx22b;
        }

        if (key.equals("CX22W")) {
            return R.drawable.cx22w;
        }

        if (key.equals("MX840") || key.equals("MX840A") || key.equals("MX840B")) {
            return R.drawable.mx840b;
        }

        if (key.equals("MX840BR")) {
            return R.drawable.mx840br;
        }

        if (key.equals("MX403") || key.equals("MX430B")) {
            return R.drawable.mx430b;
        }

        if (key.equals("MX809") || key.equals("MX809B")) {
            return R.drawable.mx809b;
        }

        if (key.equals("MX238") || key.equals("MX238B")) {
            return R.drawable.mx238b;
        }

        if (key.equals("PMX")) {
            return R.drawable.pmx;
        }

        return R.drawable.ic_no_device;
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
            moduleType = unknown;
        }
        return moduleType;
    }

    private String getDisplayName(final Device device) {
        String displayName = device.getName();
        if (displayName == null || displayName.length() == 0) {
            displayName = unknown;
        }
        return displayName;
    }

    private void openBrowser(InetSocketAddress address) {
        final BrowserStartTask browserTask = new BrowserStartTask(context);
        browserTask.execute(address);
    }
}
