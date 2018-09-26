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

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.hbm.devices.scan.announce.Announce;

import java.util.List;

class AnnounceSharer {

    private final AppCompatActivity activity;

    AnnounceSharer(AppCompatActivity activity) {
        this.activity = activity;
    }

    void handleShare(@NonNull List<Announce> announces) {
        final Uri uri = DeviceZipper.saveAnnounces(announces, activity);
        share(uri);
    }

    void handleShare(Announce announce) {
        final Uri uri = DeviceZipper.saveAnnounce(announce, activity);
        share(uri);
    }

    private void share(@Nullable Uri uri) {
        if (uri == null) {
            final Toast exitToast = Toast.makeText(activity, R.string.create_devices_file_error, Toast.LENGTH_SHORT);
            exitToast.show();
        } else {
            final Intent devices = new Intent();
            devices.setAction(Intent.ACTION_SEND);
            devices.putExtra(Intent.EXTRA_STREAM, uri);
            devices.setTypeAndNormalize("application/zip");
            activity.startActivity(Intent.createChooser(devices, activity.getResources().getText(R.string
                    .share_devices)));
        }
    }
}
