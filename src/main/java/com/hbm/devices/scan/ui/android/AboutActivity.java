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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

/**
 * This activity shows some 'About' information of the app.
 */
public class AboutActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);
        initToolbar();
        fillVersion();

        final Button thirdParty = findViewById(R.id.third_party);
        if (thirdParty!= null) {
            thirdParty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final Notices notices = new Notices();
                    notices.addNotice(new Notice("gson", "https://github.com/google/gson", "Google Inc.", new
                            ApacheSoftwareLicense20()));
                    notices.addNotice(new Notice("devscan", "https://github.com/HBM/java-scan", "Hottinger Baldwin " +
                            "Messtechnik GmbH", new MITLicense()));
                    notices.addNotice(new Notice("LicensesDialog", "https://psdev.de/LicensesDialog", "Philip Schiffer", new
                            ApacheSoftwareLicense20()));
                    notices.addNotice(new Notice("Picasso", "http://square.github.io/picasso", "Square, Inc.", new
                            ApacheSoftwareLicense20()));

                    new LicensesDialog.Builder(view.getContext())
                            .setTitle(R.string.third_party)
                            .setNotices(notices)
                            .setIncludeOwnLicense(false)
                            .build()
                            .show();
                }
            });
        }
    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle(getString(R.string.about));
    }

    private void fillVersion() {
        final TextView versionView = findViewById(R.id.about_version);
        if (versionView == null) {
            return;
        }
        versionView.setText(this.getResources().getString(R.string.about_version, BuildConfig.VERSION_NAME));
        final TextView imageCourtesy = findViewById(R.id.about_images);
        final MovementMethod method = LinkMovementMethod.getInstance();
        if ((imageCourtesy != null) && (method != null)) {
            imageCourtesy.setMovementMethod(method);
        }
        final TextView buildInfo = findViewById(R.id.about_build_info);
        if (buildInfo == null) {
            return;
        }
        buildInfo.setText(this.getResources().getString(R.string.about_build_info,
                BuildConfig.VERSION_CODE,
                new Date(BuildConfig.TIMESTAMP),
                BuildConfig.GITHASH));
    }
}
