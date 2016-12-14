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

import com.hbm.devices.scan.announce.Announce;
import com.hbm.devices.scan.announce.AnnounceParams;
import com.hbm.devices.scan.announce.Interface;
import com.hbm.devices.scan.announce.NetSettings;
import com.hbm.devices.scan.announce.Router;

import java.util.ArrayList;
import java.util.List;

class DisplayUpdateEventGenerator {
    private final DisplayNotifier notifier;

    DisplayUpdateEventGenerator(final DisplayNotifier n) {
        this.notifier = n;
    }

    void compareLists(final List<Announce> oldList, final List<Announce> newList) {
        ArrayList<Announce> oldListClone = new ArrayList<>(oldList);
        ArrayList<Announce> newListClone = new ArrayList<>(newList);

        try {
            updateRemovals(oldList, newList);
            updateAdditions(oldList, newList);
            updateMoves(oldList, newList);
        } catch (IndexOutOfBoundsException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("oldList size: ").append(oldListClone.size()).append('\n');
            sb.append("newList size: ").append(newListClone.size()).append('\n');
            sb.append("oldList:\n");
            addList(oldListClone, sb);
            sb.append("\n\n");
            sb.append("newList:\n");
            addList(newListClone, sb);
            IndexOutOfBoundsException ex = new IndexOutOfBoundsException(sb.toString());
            throw ex;
        }
    }

    private void addList(List<Announce> list, StringBuilder sb) {
        final int count = list.size();
        for (int i = 0; i < count; i++) {
            final Announce a = list.get(i);
//            sb.append("uuid: ").append(a.getParams().getDevice().getUuid()).append(" commPath: ").append
//                    (getCommunicationPath(a)).append('\n');
            sb.append("json: ").append(a.getJSONString()).append('\n');
        }
    }

    private String getCommunicationPath(Announce a) {
        final AnnounceParams parameters = a.getParams();

        final StringBuilder hashBuilder = new StringBuilder();
        hashBuilder.append(parameters.getDevice().getUuid());

        final Router router = parameters.getRouter();
        if (router != null) {
            hashBuilder.append(router.getUuid());
        }

        final NetSettings settings = parameters.getNetSettings();
        final Interface iface = settings.getInterface();
        final String interfaceName = iface.getName();
        hashBuilder.append(interfaceName);
        return hashBuilder.toString();
    }

    private void updateRemovals(final List<Announce> oldList, final List<Announce> newList) {
        final int count = oldList.size();
        for (int i = count - 1; i >= 0; i--) {
            final Announce announce = oldList.get(i);
            if (getIndexInList(newList, announce) == -1) {
                oldList.remove(i);
                notifier.notifyRemoveAt(i);
            }
        }
    }

    private void updateAdditions(final List<Announce> oldList, final List<Announce> newList) {
        final int count = newList.size();
        for (int i = 0; i < count; i++) {
            final Announce announce = newList.get(i);
            final int changeIndex = getIndexInList(oldList, announce);
            if (changeIndex == -1) {
                oldList.add(announce);
                notifier.notifyAddAt(oldList.size() - 1);
            } else {
                Announce oldAnnounce = oldList.get(changeIndex);
                if (!oldAnnounce.equals(announce)) {
                    oldList.set(changeIndex, announce);
                    notifier.notifyChangeAt(changeIndex);
                }
            }
        }
    }

    private void updateMoves(final List<Announce> oldList, final List<Announce> newList) {
        for (int toPosition = newList.size() - 1; toPosition >= 0; toPosition--) {
            final Announce model = newList.get(toPosition);
            final int fromPosition = oldList.indexOf(model);
            if ((fromPosition >= 0) && (fromPosition != toPosition)) {
                final Announce announce = oldList.remove(fromPosition);
                oldList.add(toPosition, announce);
                notifier.notifyMoved(fromPosition, toPosition);
            }
        }
    }

    private static int getIndexInList(final List<Announce> list, final Announce announce) {
        final int size = list.size();
        for (int i = 0; i < size; i++) {
            final Announce element = list.get(i);
            if (element.sameCommunicationPath(announce)) {
                return i;
            }
        }
        return -1;
    }
}
