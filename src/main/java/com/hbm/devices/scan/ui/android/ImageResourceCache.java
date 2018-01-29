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

import java.util.HashMap;

class ImageResourceCache {

    private static final HashMap<String, Integer> resourceCache;

    private ImageResourceCache() {}

    static int getResourceFromCache(String key) {
        final Integer resourceId = resourceCache.get(key);
        if (resourceId == null) {
            return R.drawable.ic_no_device;
        }
        return resourceId;
    }

    static {
        resourceCache = new HashMap<>();

        resourceCache.put("CP52", R.drawable.cp52);

        resourceCache.put("CX23R", R.drawable.cx23);

        resourceCache.put("MX1601", R.drawable.mx1601);
        resourceCache.put("MX1601B", R.drawable.mx1601b);
        resourceCache.put("MX1601B-R", R.drawable.mx1601br);

        resourceCache.put("MX1609KB-R", R.drawable.mx1609kbr);
        resourceCache.put("MX1609", R.drawable.mx1609kb);
        resourceCache.put("MX1609KB", R.drawable.mx1609kb);
        resourceCache.put("MX1609TB-R", R.drawable.mx1609tbr);
        resourceCache.put("MX1609TB", R.drawable.mx1609tb);
        resourceCache.put("MX1609T", R.drawable.mx1609t);

        resourceCache.put("MX1615B-R", R.drawable.mx1615br);
        resourceCache.put("MX1615B-R S31", R.drawable.mx1615br);
        resourceCache.put("MX1615B", R.drawable.mx1615b);
        resourceCache.put("MX1615", R.drawable.mx1615);

        resourceCache.put("MX1616B", R.drawable.mx1616b);

        resourceCache.put("MX411B-R", R.drawable.mx411br);
        resourceCache.put("MX411P", R.drawable.mx411p);
        resourceCache.put("MX410", R.drawable.mx410);
        resourceCache.put("MX410B", R.drawable.mx410b);

        resourceCache.put("MX471B-R", R.drawable.mx471br);
        resourceCache.put("MX471", R.drawable.mx471);
        resourceCache.put("MX471B", R.drawable.mx471b);

        resourceCache.put("MX879", R.drawable.mx879);
        resourceCache.put("MX879B", R.drawable.mx879b);
        resourceCache.put("MX878", R.drawable.mx878);
        resourceCache.put("MX878B", R.drawable.mx878b);

        resourceCache.put("MX460", R.drawable.mx460b);
        resourceCache.put("MX460P", R.drawable.mx460p);
        resourceCache.put("MX460B", R.drawable.mx460b);
        resourceCache.put("MX460B-R", R.drawable.mx460br);

        resourceCache.put("MX440", R.drawable.mx440);
        resourceCache.put("MX440A", R.drawable.mx440b);
        resourceCache.put("MX440B", R.drawable.mx440b);

        resourceCache.put("MX403", R.drawable.mx403b);
        resourceCache.put("MX403B", R.drawable.mx403b);

        resourceCache.put("CX27", R.drawable.cx27b);
        resourceCache.put("CX27B", R.drawable.cx27b);

        resourceCache.put("CX22BW", R.drawable.cx22bw);
        resourceCache.put("CX22B", R.drawable.cx22b);
        resourceCache.put("CX22W", R.drawable.cx22w);

        resourceCache.put("MX840", R.drawable.mx840);
        resourceCache.put("MX840P", R.drawable.mx840p);
        resourceCache.put("MX840A", R.drawable.mx840);
        resourceCache.put("MX840B", R.drawable.mx840b);
        resourceCache.put("MX840B-R", R.drawable.mx840br);

        resourceCache.put("MX430", R.drawable.mx430b);
        resourceCache.put("MX430B", R.drawable.mx430b);

        resourceCache.put("MX809", R.drawable.mx809b);
        resourceCache.put("MX809B", R.drawable.mx809b);

        resourceCache.put("MX238", R.drawable.mx238b);
        resourceCache.put("MX238B", R.drawable.mx238b);

        resourceCache.put("MX590", R.drawable.mx590);

        resourceCache.put("PMX", R.drawable.pmx);
        resourceCache.put("PMX CODESYS", R.drawable.pmx);

        resourceCache.put("WTX120", R.drawable.wtx120);
        resourceCache.put("WTX110", R.drawable.wtx110);

        resourceCache.put("BM40", R.drawable.bm40);
        resourceCache.put("BM40IE", R.drawable.bm40ie);
        resourceCache.put("BM40PB", R.drawable.bm40pb);
    }
}
