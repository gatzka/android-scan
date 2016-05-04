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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

class DisplayUpdateEventGenerator {
    private final DisplayNotifier notifier;

    DisplayUpdateEventGenerator(final DisplayNotifier n) {
        this.notifier = n;
    }

    List<Announce> oldListClone;
    List<Announce> newListClone;

    void compareLists(final List<Announce> oldList, final List<Announce> newList) {
        checkList(oldList);
        checkList(newList);
        oldListClone = (List<Announce>) ((ArrayList<Announce>)oldList).clone();
        newListClone = (List<Announce>) ((ArrayList<Announce>)newList).clone();
        updateRemovals(oldList, newList);
        updateAdditions(oldList, newList);
        updateMoves(oldList, newList);
    }

    private void checkList(List<Announce> list) {
        for(int i = 0; i < list.size(); i++) {
            Announce a = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                Announce b = list.get(j);
                if (a.sameCommunicationPath(b)) {
                    System.out.println("err");
                }
            }
        }
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
        checkList(oldList);
    }

    private void updateAdditions(final List<Announce> oldList, final List<Announce> newList) {
        final int count = newList.size();
        for (int i = 0; i < count; i++) {
            final Announce announce = newList.get(i);
            final int changeIndex = getIndexInList(oldList, announce);
            if (changeIndex == -1) {
                oldList.add(announce);
                checkList(oldList);
                notifier.notifyAddAt(oldList.size() - 1);
            } else {
                Announce oldAnnounce = oldList.get(changeIndex);
                if (!oldAnnounce.equals(announce)) {
                    oldList.set(changeIndex, announce);
                    checkList(oldList);
                    notifier.notifyChangeAt(changeIndex);
                }
            }
        }
    }

    private void updateMoves(final List<Announce> oldList, final List<Announce> newList) {
        if (oldList.size() != newList.size()) {
            System.out.println("error");
        }
        for (int toPosition = newList.size() - 1; toPosition >= 0; toPosition--) {
            final Announce model = newList.get(toPosition);
            final int fromPosition = oldList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                final Announce announce = oldList.remove(fromPosition);
                oldList.add(toPosition, announce);
                notifier.notifyMoved(fromPosition, toPosition);
            }
        }
        checkList(oldList);
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
