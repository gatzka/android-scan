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
import com.hbm.devices.scan.ui.android.DisplayNotifier;
import com.hbm.devices.scan.ui.android.DisplayUpdateEventGenerator;


import java.io.IOException;
import java.lang.Override;
import java.util.ArrayList;
import java.io.InputStream;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import org.junit.Test;
import static org.junit.Assert.*;

public class DisplayUpdateEventGeneratorTest  implements DisplayNotifier, Observer {

    private List<Announce> oldList = new ArrayList<Announce>();
    private List<Announce> newList = new ArrayList<Announce>();
    private List<Announce> oldListClone;

    private List<DisplayEventAt> events = new ArrayList<DisplayEventAt>();

    private boolean fillNewList = false;

    private static final String DEVICE1;
    private static final String DEVICE1UPDATE;
    private static final String DEVICE2;
    private static final String DEVICE3;
    private static final String DEVICE4;
    private static final String DEVICE5;
    private static final String DEVICE6;

    @Test
    public void testCompareLists() throws Exception {
        AnnounceDeserializer parser = new AnnounceDeserializer();
        parser.addObserver(this);
        parser.update(null, DEVICE1);
        parser.update(null, DEVICE2);
        parser.update(null, DEVICE3);
        parser.update(null, DEVICE4);
        parser.update(null, DEVICE5);

        fillNewList = true;
        parser.update(null, DEVICE1UPDATE);
        parser.update(null, DEVICE2);
        parser.update(null, DEVICE6);
        parser.update(null, DEVICE5);
        parser.update(null, DEVICE4);

        assertEquals(oldList.size(), 5);
        assertEquals(newList.size(), 5);
        oldListClone = new ArrayList<Announce>(oldList.size());
        for (Announce item: oldList) {
            oldListClone.add(item);
        }
        assertEquals("Cloned old list not equals to oldList", oldList, oldListClone);
        DisplayUpdateEventGenerator eventGenerator = new DisplayUpdateEventGenerator(this);
        eventGenerator.compareLists(oldList, newList);
        assertEquals("oldList not the same as newList after compareList", oldList, newList);
        assertEquals("updated events list not the same as newList after compareList", oldListClone, newList);
    }

    @Override
    public void notifyRemoveAt(int position) {
        oldListClone.remove(position);
        events.add(new DisplayEventAt(DisplayEvents.REMOVE, position));
    }

    @Override
    public void notifyMoved(int fromPosition, int toPosition) {
        Announce announce = oldListClone.remove(fromPosition);
        oldListClone.add(toPosition, announce);
        events.add(new DisplayEventAt(DisplayEvents.MOVE, fromPosition, toPosition));
    }

    @Override
    public void notifyChangeAt(int position) {
        oldListClone.set(position, newList.get(position));
        events.add(new DisplayEventAt(DisplayEvents.UPDATE, position));
    }

    @Override
    public void notifyAddAt(int position) {
        oldListClone.add(newList.get(position));
        events.add(new DisplayEventAt(DisplayEvents.ADD, position));
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

    enum DisplayEvents {
        REMOVE, ADD, MOVE, UPDATE
    }

    class DisplayEventAt {
        DisplayEvents event;
        int position;
        int fromPosition;
        int toPosition;

        DisplayEventAt(DisplayEvents ev, int pos) {
            this.event = ev;
            this.position = pos;
        }

        DisplayEventAt(DisplayEvents ev, int from, int to) {
            this.event = ev;
            this.fromPosition = from;
            this.toPosition = to;
        }
    }
}