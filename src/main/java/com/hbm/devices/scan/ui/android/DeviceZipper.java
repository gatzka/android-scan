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

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.hbm.devices.scan.announce.Announce;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class DeviceZipper {

    private static final String FILE_NAME = "devices.zip";

    private DeviceZipper() {}

    @Nullable
    static Uri saveAnnounce(Announce announce, @NonNull AppCompatActivity activity) {
        final List<Announce> list = new ArrayList<>();
        list.add(announce);
        return saveAnnounces(list, activity);
    }

    static Uri saveAnnounces(@NonNull List<Announce> announces, @NonNull AppCompatActivity activity) {
        final TimeZone tz = TimeZone.getTimeZone("UTC");
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        df.setTimeZone(tz);
        final String isoDate = df.format(new Date());
        final Charset charSet = Charset.forName("UTF-8");

        try {
            final File file = createFile(activity);
            if (file == null) {
                return null;
            }
            final FileOutputStream fos = new FileOutputStream(file, false);
            final ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));
            final ZipEntry entry = new ZipEntry("devices.json");
            zos.putNextEntry(entry);
            zos.write(("{\"date\":\"" + isoDate + "\",").getBytes(charSet));
            zos.write(("\"version\": \"1.0\",").getBytes(charSet));
            zos.write("\"devices\":[".getBytes(charSet));

            final Iterator<Announce> iterator = announces.iterator();
            while (iterator.hasNext()) {
                final Announce announce = iterator.next();
                zos.write(announce.getJSONString().getBytes(charSet));
                if (iterator.hasNext()) {
                    zos.write(",\n".getBytes(charSet));
                }
            }

            zos.write("]}".getBytes(charSet));
            zos.closeEntry();
            zos.close();
            fos.close();
            return FileProvider.getUriForFile(activity, "com.hbm.devices.scan.ui.android.fileprovider", file);
        } catch (IOException e) {
            Toast.makeText(activity, activity.getString(R.string.could_not_create, e),
                    Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private static File createFile(AppCompatActivity activity) throws IOException {
        final File cacheDir = activity.getCacheDir();
        final File subDir = new File(cacheDir, "devices");
        if (!subDir.exists() && (!subDir.mkdirs())) {
            return null;
        }
        final File file = new File(subDir, FILE_NAME);
        if (file.exists() && !file.delete()) {
            throw new IOException("Cold not delete file!");
        }
        if (!file.createNewFile()) {
            throw new IOException("Could not create file!");
        }
        return file;
    }
}

