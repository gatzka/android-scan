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
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;

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

import com.hbm.devices.scan.announce.Announce;

public final class DeviceZipper {

    static Uri saveAnnounce(Announce announce, AppCompatActivity activity) {
        List<Announce> list = new ArrayList<>();
        list.add(announce);
        return saveAnnounces(list, activity);
    }

    static Uri saveAnnounces(List<Announce> announces, AppCompatActivity activity) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US);
        df.setTimeZone(tz);
        String isoDate = df.format(new Date());
        Charset charSet = Charset.forName("UTF-8");

        try {
            File cacheDir = activity.getCacheDir();
            File subDir = new File(cacheDir, "devices");
            if ( !subDir.exists() ) {
                if (!subDir.mkdirs()) {
                    return null;
                }
            }
            File file = new File(subDir, "devices.zip");
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));
            ZipEntry entry = new ZipEntry("devices.json");
            zos.putNextEntry(entry);
            zos.write(("{\"date\":\"" + isoDate + "\",").getBytes(charSet)); 
            zos.write("\"devices\":[".getBytes(charSet));

            Iterator<Announce> iterator = announces.iterator();
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
            return null;
        }
    }
}

