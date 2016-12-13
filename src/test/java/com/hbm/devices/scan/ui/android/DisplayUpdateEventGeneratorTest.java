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
import com.hbm.devices.scan.announce.AnnounceDeserializer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class DisplayUpdateEventGeneratorTest implements DisplayNotifier, Observer {

    private static final String DEVICE1;
    private static final String DEVICE1UPDATE;
    private static final String DEVICE2;
    private static final String DEVICE3;
    private static final String DEVICE4;
    private static final String DEVICE5;
    private static final String DEVICE6;

    private String[] oldArray;
    private String[] newArray;

    private final List<Announce> oldList = new ArrayList<>();
    private final List<Announce> newList = new ArrayList<>();
    private List<Announce> oldListClone;

    private boolean fillNewList;

    @Parameterized.Parameters
    public static Collection deviceLists() {
        return Arrays.asList(new Object[][] {
                {new String[]{DEVICE2, DEVICE3, DEVICE4, DEVICE5}, new String[]{DEVICE1, DEVICE2, DEVICE3, DEVICE4, DEVICE5}},
                {new String[]{DEVICE1, DEVICE2, DEVICE3, DEVICE4}, new String[]{DEVICE1, DEVICE2, DEVICE3, DEVICE4, DEVICE5}},
                {new String[]{DEVICE1, DEVICE2, DEVICE3, DEVICE4, DEVICE5}, new String[]{DEVICE2, DEVICE3, DEVICE4, DEVICE5}},
                {new String[]{DEVICE1, DEVICE2, DEVICE3, DEVICE4, DEVICE5}, new String[]{DEVICE1, DEVICE2, DEVICE3, DEVICE4}},
                {new String[]{DEVICE1, DEVICE2, DEVICE3, DEVICE4, DEVICE5}, new String[]{DEVICE1UPDATE, DEVICE2, DEVICE3, DEVICE4, DEVICE5}},
                {new String[]{DEVICE2, DEVICE3, DEVICE4, DEVICE5, DEVICE1}, new String[]{DEVICE2, DEVICE3, DEVICE4, DEVICE5, DEVICE1UPDATE}},
                {new String[]{DEVICE1, DEVICE2, DEVICE3, DEVICE4, DEVICE5}, new String[]{DEVICE6, DEVICE2, DEVICE3, DEVICE4, DEVICE1UPDATE, DEVICE5}},
        });
    }

    public DisplayUpdateEventGeneratorTest(String[] oldArray, String[] newArray) {
        this.oldArray = oldArray;
        this.newArray = newArray;
    }

    @Test
    public void testUpdates() {
        final AnnounceDeserializer parser = new AnnounceDeserializer();
        parser.addObserver(this);

        for (final String elem : oldArray) {
            parser.update(null, elem);
        }
        fillNewList = true;
        for (final String elem : newArray) {
            parser.update(null, elem);
        }

        assertEquals(oldList.size(), oldArray.length);
        assertEquals(newList.size(), newArray.length);

        oldListClone = new ArrayList<>(oldList.size());
        for (final Announce item: oldList) {
            oldListClone.add(item);
        }
        assertEquals("Cloned old list not equals to oldList", oldList, oldListClone);
        final DisplayUpdateEventGenerator eventGenerator = new DisplayUpdateEventGenerator(this);
        eventGenerator.compareLists(oldList, newList);
        assertEquals("oldList not the same as newList after compareList", oldList, newList);
        assertTrue("updated events list not the same as newList after compareList", sameContent(oldListClone, newList));
    }


    @Override
    public void notifyRemoveAt(int position) {
        oldListClone.remove(position);
    }

    @Override
    public void notifyMoved(int fromPosition, int toPosition) {
        final Announce announce = oldListClone.remove(fromPosition);
        oldListClone.add(toPosition, announce);
    }

    @Override
    public void notifyChangeAt(int position) {
        oldListClone.set(position, oldList.get(position));
    }

    @Override
    public void notifyAddAt(int position) {
        oldListClone.add(oldList.get(position));
    }

    @Override
    public void update(Observable o, Object arg) {
        final Announce announce = (Announce)arg;
        if (fillNewList) {
            newList.add(announce);
        } else {
            oldList.add(announce);
        }
    }

    private boolean sameContent(List<Announce> l1, List<Announce> l2) {
        if (l1.size() != l2.size()) {
            return false;
        }

        for (int i = 0; i < l1.size(); i++) {
            final Announce a = l1.get(i);
            if (!foundInList(l2, a)) {
                return false;
            }
        }
        return true;
    }

    private boolean foundInList(List<Announce> list, final Announce a) {
        for (int i = 0; i < list.size(); i++) {
            final Announce b = list.get(i);
            if (a.equals(b)) {
                return true;
            }
        }
        return false;
    }

    static {
        try (final InputStream is = FakeMessageReceiver.class.getResourceAsStream("/devices.properties")) {
            final Properties props = new Properties();
            props.load(is);
            DEVICE1 = props.getProperty("scan.announce.device1");
            DEVICE1UPDATE = props.getProperty("scan.announce.device1update");
            DEVICE2 = props.getProperty("scan.announce.device2");
            DEVICE3 = props.getProperty("scan.announce.device3");
            DEVICE4 = props.getProperty("scan.announce.device4");
            DEVICE5 = props.getProperty("scan.announce.device5");
            DEVICE6 = props.getProperty("scan.announce.device6");
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
