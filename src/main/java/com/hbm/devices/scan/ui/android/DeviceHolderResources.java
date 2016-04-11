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

import android.graphics.drawable.Drawable;
import android.content.Context;
import android.support.v4.content.ContextCompat;

class DeviceHolderResources {

    private static DeviceHolderResources instance;

    private final Drawable blackInfo;
    private final Drawable whiteInfo;

    private final int cardBackgroundNotConnectable;
    private final int cardBackgroundConnectable;
    private final int moduleTypeTextColorNotConnectable;
    private final int moduleTypeTextColorConnectable;
    private final int moduleNameTextColorNotConnectable;
    private final int moduleNameTextColorConnectable;
    private final int moduleIdTextColorNotConnectable;
    private final int moduleIdTextColorConnectable;
    private final int alpha;
    private final String unknown;

    DeviceHolderResources(Context context) {
        cardBackgroundNotConnectable = ContextCompat.getColor(context, R.color.color_not_connectable);
        cardBackgroundConnectable = ContextCompat.getColor(context, android.R.color.background_light);
        moduleTypeTextColorNotConnectable = ContextCompat.getColor(context, android.R.color.primary_text_dark);
        moduleTypeTextColorConnectable = ContextCompat.getColor(context, android.R.color.primary_text_light);
        moduleNameTextColorNotConnectable = ContextCompat.getColor(context, android.R.color.secondary_text_dark);
        moduleNameTextColorConnectable = ContextCompat.getColor(context, android.R.color.secondary_text_light);
        moduleIdTextColorNotConnectable = ContextCompat.getColor(context, android.R.color.secondary_text_dark);
        moduleIdTextColorConnectable = ContextCompat.getColor(context, android.R.color.secondary_text_light);
        unknown = context.getString(R.string.unknown);

        blackInfo = ContextCompat.getDrawable(context, R.drawable.ic_info_outline_black_48dp);
        alpha = context.getResources().getInteger(R.integer.text_alpha);
        setImageAlpha(blackInfo, alpha);
        whiteInfo = ContextCompat.getDrawable(context, R.drawable.ic_info_outline_white_48dp);
    }

    static DeviceHolderResources getInstance(Context context) {
        synchronized (DeviceHolderResources.class) {
            if (DeviceHolderResources.instance == null) {
                DeviceHolderResources.instance = new DeviceHolderResources(context);
            }
            return DeviceHolderResources.instance;
        }
    }

    private static void setImageAlpha(Drawable draw, int alphaPercent) {
        final int alpha = alphaPercent * 255 / 100;
        draw.setAlpha(alpha);
    }

    Drawable getBlackInfo() {
        return blackInfo;
    }

    Drawable getWhiteInfo() {
        return whiteInfo;
    }

    int getCardBackgroundNotConnectable() {
        return cardBackgroundNotConnectable;
    }

    int getCardBackgroundConnectable() {
        return cardBackgroundConnectable;
    }

    int getModuleTypeTextColorNotConnectable() {
        return moduleTypeTextColorNotConnectable;
    }

    int getModuleTypeTextColorConnectable() {
        return moduleTypeTextColorConnectable;
    }

    int getModuleNameTextColorNotConnectable() {
        return moduleNameTextColorNotConnectable;
    }

    int getModuleNameTextColorConnectable() {
        return moduleNameTextColorConnectable;
    }

    int getModuleIdTextColorNotConnectable() {
        return moduleIdTextColorNotConnectable;
    }

    int getModuleIdTextColorConnectable() {
        return moduleIdTextColorConnectable;
    }

    int getAlpha() {
        return alpha;
    }

    String getUnknown() {
        return unknown;
    }
}
