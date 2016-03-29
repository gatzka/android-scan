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

import java.util.List;

class DisplayUpdateEventGenerator {
    private final DisplayNotifier notifier;

    DisplayUpdateEventGenerator(final DisplayNotifier n) {
        this.notifier = n;
    }

    void compareLists(final List<Announce> oldList, final List<Announce> newList) {
        updateRemovals(oldList, newList);
        updateAdditions(oldList, newList);
        updateMoves(oldList, newList);
    }

    private void updateRemovals(final List<Announce> oldList, final List<Announce> newList) {
        int count = oldList.size();
        for (int i = count - 1; i >= 0; i--) {
            final Announce announce = oldList.get(i);
            if (isNotInList(newList, announce)) {
                oldList.remove(i);
                notifier.notifyRemoveAt(i);
            }
        }
    }

    private void updateAdditions(final List<Announce> oldList, final List<Announce> newList) {
        for (int i = 0, count = newList.size(); i < count; i++) {
            final Announce announce = newList.get(i);
            if (isNotInList(oldList, announce)) {
                oldList.add(announce);
                notifier.notifyAddAt(i);
            } else {
                if (!oldList.contains(announce)) {
                    oldList.set(i, announce);
                    notifier.notifyChangeAt(i);
                }
            }
        }
    }

    private void updateMoves(final List<Announce> oldList, final List<Announce> newList) {
        for (int toPosition = newList.size() - 1; toPosition >= 0; toPosition--) {
            final Announce model = newList.get(toPosition);
            final int fromPosition = oldList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                final Announce announce = oldList.remove(fromPosition);
                oldList.add(toPosition, announce);
                notifier.notifyMoved(fromPosition, toPosition);
            }
        }
    }

    private static boolean isNotInList(final List<Announce> list, final Announce announce) {
        for (Announce element : list) {
            if (element.sameCommunicationPath(announce)) {
                return true;
            }
        }
        return false;
    }
}
