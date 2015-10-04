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
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ListView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SearchView;

import java.net.InetSocketAddress;

import com.hbm.devices.scan.announce.Announce;

public final class DevicesFragment extends ListFragment implements View.OnClickListener {

    private ModuleListAdapter adapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        System.out.println("---------------- DevicesFragment onAttach");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        System.out.println("---------------- DevicesFragment onActivityCreated");
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        System.out.println("---------------- DevicesFragment onViewStateRestored");
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("---------------- DevicesFragment onStart");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        adapter = new ModuleListAdapter(this);
        setListAdapter(adapter);

        setHasOptionsMenu(true);
        getActivity().getActionBar().setTitle(R.string.app_name);
        System.out.println("---------------- DevicesFragment onCreate");
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.app_name);
    }

    @Override
    public void onDestroy() {
        System.out.println("---------------- DevicesFragment onDestroy");
        super.onDestroy();
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long identifier) {
        final Announce announce = adapter.getItem(position);
        final InetSocketAddress address  = (InetSocketAddress) announce.getCookie();
        if (address == null) {
            //TODO: showConfigure(announce);
        } else {
			openBrowser(address);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.device_fragment_actions, menu);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                searchView.clearFocus();
                System.out.println("-------------- onQueryTextSubmit " + query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                System.out.println("-------------- onQueryTextChange: " + newText);
                return true;
            }
        });
        //if (filterString == null || filterString.length() == 0) {
        //    searchView.setIconified(true);
        //    searchView.setQuery("", false);
        //} else {
        //    searchView.setIconified(false);
        //    searchView.setQuery(filterString, false);
        //}
        //searchView.clearFocus();

        final MenuItem pauseItem = menu.findItem(R.id.action_pause_control);
        if (adapter.isPaused()) {
            pauseItem.setIcon(R.drawable.ic_action_play);
        } else {
            pauseItem.setIcon(R.drawable.ic_action_pause);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_pause_control) {
            handlePause(item);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
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

    private void openBrowser(InetSocketAddress address) {
        final BrowserStartTask browserTask = new BrowserStartTask(getActivity());
        browserTask.execute(new InetSocketAddress[] {address});
    }
}

