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

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.hbm.devices.scan.announce.Announce;

public final class DevicesFragment extends Fragment {

    static final String DETAILS = "Details";

    private ModuleListAdapter adapter;
    private RecyclerView devicesView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.device_view, container, false);
        devicesView = (RecyclerView) view.findViewById(R.id.devicesView);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.app_name);

        devicesView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new ModuleListAdapter(this);
        devicesView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.app_name);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.device_fragment_actions, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                searchView.clearFocus();
                adapter.setFilterString(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                adapter.setFilterString(newText);
                return false;
            }
        });

        final MenuItem pauseItem = menu.findItem(R.id.action_pause_control);
        if (adapter.isPaused()) {
            pauseItem.setIcon(R.drawable.ic_action_play);
        } else {
            pauseItem.setIcon(R.drawable.ic_action_pause);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_pause_control:
                handlePause(item);
                return true;

            case R.id.action_share:
                List<Announce> announces = adapter.getFilteredAnnounces();
                handleShare(announces);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handlePause(MenuItem item) {
        if (adapter.isPaused()) {
            item.setIcon(R.drawable.ic_action_pause);
            adapter.resumeDeviceUpdates();
        } else {
            item.setIcon(R.drawable.ic_action_play);
            adapter.pauseDeviceUpdates();
        }
    }

    private void handleShare(List<Announce> announces) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US);
        df.setTimeZone(tz);
        String isoDate = df.format(new Date());
        Charset charSet = Charset.forName("UTF-8");

        try {
            File cacheDir = getActivity().getCacheDir();
            File subDir = new File(cacheDir, "devices");
            if ( !subDir.exists() ) {
                subDir.mkdirs();
            }
            File file = new File(subDir, "devices.zip");
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));
            ZipEntry entry = new ZipEntry("devices.json");
            zos.putNextEntry(entry);
            zos.write(("{\"date\":\"" + isoDate + "\"").getBytes(charSet)); 
            zos.write("devices:[".getBytes(charSet));

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
            final Uri uri = FileProvider.getUriForFile(getActivity(), "com.hbm.devices.scan.ui.android.fileprovider", file);

        } catch (IOException e) {
           final Toast exitToast = Toast.makeText(getActivity(), R.string.create_devices_file_error, Toast.LENGTH_SHORT);
           exitToast.show();
        }

        // Intent devices = new Intent();
        // devices.setAction(Intent.ACTION_SEND);
        // devices.putExtra(Intent.EXTRA_TEXT, listBuilder.toString());
        // devices.setTypeAndNormalize("application/json");
        // startActivity(Intent.createChooser(devices, getResources().getText(R.string.share_devices)));
    }
}

