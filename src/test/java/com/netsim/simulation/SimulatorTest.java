package com.netsim.simulation;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;


public class SimulatorTest {

    @Test
    public void testEventExecutionOrder() {
        Simulator sim = Simulator.getInstance();

        StringBuilder result = new StringBuilder();

        sim.schedule(new Event(200, EventType.GENERIC, () -> result.append("C")));
        sim.schedule(new Event(100, EventType.GENERIC, () -> result.append("A")));
        sim.schedule(new Event(150, EventType.GENERIC, () -> result.append("B")));
        sim.schedule(new Event(300, EventType.SIMULATION_END, () -> {}));

        sim.run();

        assertEquals("Events should execute in time order", "ABC", result.toString());
    }

    @Test
    public void testSimulationStopsOnEndEvent() {
        Simulator sim = Simulator.getInstance();

        StringBuilder result = new StringBuilder();

        sim.schedule(new Event(100, EventType.GENERIC, () -> result.append("X")));
        sim.schedule(new Event(150, EventType.SIMULATION_END, () -> result.append("Y")));
        sim.schedule(new Event(200, EventType.GENERIC, () -> result.append("Z"))); // should not run

        sim.run();

        assertEquals("Only events before SIMULATION_END should execute", "XY", result.toString());
    }

    @Test
    public void testScheduleAfterRunIsIgnored() {
        Simulator sim = Simulator.getInstance();

        StringBuilder result = new StringBuilder();

        sim.schedule(new Event(50, EventType.GENERIC, () -> result.append("Start")));
        sim.schedule(new Event(100, EventType.SIMULATION_END, () -> {}));
        sim.run();

        // schedule after simulation ended
        sim.schedule(new Event(200, EventType.GENERIC, () -> result.append("Late")));

        assertEquals("Late event should not execute", "Start", result.toString());
    }

    @After
    public void resetSingleton() {
        Simulator.reset();
    }
}
