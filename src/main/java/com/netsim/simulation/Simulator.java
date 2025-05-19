package com.netsim.simulation;

import java.util.PriorityQueue;
import com.netsim.utils.Logger;

public class Simulator {
    private static Simulator instance = null;

    private long currentTime;
    private final PriorityQueue<Event> queue;
    private boolean running;

    private Simulator() {
        this.currentTime = 0;
        this.running = false;
        this.queue = new PriorityQueue<Event>();
    }

    /**
     * @return the singleton instance of the simulator
     */
    public static Simulator getInstance() {
        if (instance == null) {
            instance = new Simulator();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    /**
     * Schedules an event to be executed in the future.
     * @param event the event to be scheduled
     */
    public void schedule(Event event) {
        if (!this.running) {
            this.queue.add(event);
            Logger.getInstance().info("Scheduled event: " + event.toString());
        } else
            Logger.getInstance().error("WARNING: Ignored event scheduled after simulation end: " + event);
    }

    /**
     * Starts and runs the simulation until the event queue is empty or a SIMULATION_END event occurs.
     */
    public void run() {
        this.running = true;
        while (!this.queue.isEmpty() && this.running) {
            Event e = this.queue.poll();
            this.currentTime = e.getTimestamp();
            e.execute();
            if (e.getType() == EventType.SIMULATION_END) {
                this.running = false;
            }
        }
        Logger.getInstance().info("Simulation ended at " + this.currentTime + "ms");
    }

    /**
     * @return the current simulation time
     */
    public long getCurrentTime() {
        return this.currentTime;
    }
}
