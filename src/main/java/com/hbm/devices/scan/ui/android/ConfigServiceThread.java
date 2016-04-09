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

import android.os.AsyncTask;
import android.widget.Toast;

import com.hbm.devices.scan.ScanInterfaces;
import com.hbm.devices.scan.configure.ConfigurationCallback;
import com.hbm.devices.scan.configure.ConfigurationMessageReceiver;
import com.hbm.devices.scan.configure.ConfigurationMulticastSender;
import com.hbm.devices.scan.configure.ConfigurationParams;
import com.hbm.devices.scan.configure.ConfigurationSerializer;
import com.hbm.devices.scan.configure.ConfigurationService;
import com.hbm.devices.scan.configure.Response;
import com.hbm.devices.scan.configure.ResponseDeserializer;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Collection;

class ConfigServiceThread extends Thread {
    private final ConfigurationService configService;
    private final ConfigurationMessageReceiver responseReceiver;

    ConfigServiceThread() throws IOException {
        responseReceiver = new ConfigurationMessageReceiver();
        final ResponseDeserializer responseParser = new ResponseDeserializer();
        responseReceiver.addObserver(responseParser);
        final Collection<NetworkInterface> scanInterfaces = new ScanInterfaces().getInterfaces();
        final ConfigurationMulticastSender multicastSender = new ConfigurationMulticastSender(scanInterfaces);
        final ConfigurationSerializer sender = new ConfigurationSerializer(multicastSender);
        this.configService = new ConfigurationService(sender, responseParser);
    }

    @Override
    public void run() {
        responseReceiver.run();
    }

    void kill() {
        responseReceiver.close();
        configService.close();
    }

    void sendConfiguration(ConfigurationParams configParams, ConfigureActivity activity) {
        new SendConfigTask(activity).execute(configParams);
    }

    private class SendConfigTask extends AsyncTask<ConfigurationParams, Integer, Void> implements ConfigurationCallback {
        private final ConfigureActivity activity;
        private String message;

        SendConfigTask(final ConfigureActivity act) {
            activity = act;
        }

        @Override
        protected Void doInBackground(ConfigurationParams... params) {

            for (final ConfigurationParams sendParam : params) {
                try {
                    configService.sendConfiguration(sendParam,
                            this, ConfigureActivity.CONFIGURATION_TIMEOUT);
                } catch (IOException e) {
                    message = activity.getString(R.string.could_not_send_config_req) +
                            ": " + e;
                }
                // Escape early if cancel() is called
                if (isCancelled()) {
                    break;
                }
            }
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    message = activity.getString(R.string.config_error, "Call interrupted!");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        }

        @Override
        public synchronized void onSuccess(final Response response) {
            message = activity.getString(R.string.config_successful);
            notifyAll();
        }
        @Override
        public synchronized void onTimeout(long t) {
            message = activity.getString(R.string.config_timeout);
            notifyAll();
        }

        @Override
        public synchronized void onError(final Response response) {
            message = activity.getString(R.string.config_error, response.getError().getMessage());
            notifyAll();
        }
    }
}
