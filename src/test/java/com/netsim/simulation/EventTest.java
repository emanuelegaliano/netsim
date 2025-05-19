package com.netsim.simulation;

import static org.junit.Assert.*;

import org.junit.Test;


public class EventTest {

    @Test
    public void testEventOrdering() {
        Event e1 = new Event(50, EventType.LOG, () -> {});
        Event e2 = new Event(100, EventType.LOG, () -> {});
        assertTrue("Earlier event should be less", e1.compareTo(e2) < 0);
    }

    @Test
    public void testEventFields() {
        Runnable action = () -> System.out.println("Running");
        Event e = new Event(123, EventType.TIMEOUT, action);

        assertEquals(123, e.getTimestamp());
        assertEquals(EventType.TIMEOUT, e.getType());
    }

    @Test
    public void testEventExecution() {
        final boolean[] ran = {false};
        Event e = new Event(0, EventType.GENERIC, () -> ran[0] = true);

        e.execute();
        assertTrue("Action should have executed", ran[0]);
    }

    @Test
    public void testEventToStringFormat() {
        Event e = new Event(75, EventType.LOG, () -> {});
        String str = e.toString();
        assertTrue("toString should contain timestamp", str.contains("75"));
        assertTrue("toString should contain event type", str.contains("LOG"));
    }
}
