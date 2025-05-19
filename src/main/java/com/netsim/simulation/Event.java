package com.netsim.simulation;

import com.netsim.utils.Logger;

public class Event implements Comparable<Event> {
    private final long timestamp;
    private final EventType type;
    private final Runnable action;

    /**
     * Constructs a new simulation event.
     * @param timestamp the scheduled time for this event (in ms)
     * @param type the type of event
     * @param action the logic to execute when the event fires
     */
    public Event(long timestamp, EventType type, Runnable action) {
        this.timestamp = timestamp;
        this.type = type;
        this.action = action;
    }

    /**
     * @return the time at which this event is scheduled to run
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * @return the type of this event
     */
    public EventType getType() {
        return this.type;
    }

    /**
     * Executes the event's associated logic and logs its execution.
     */
    public void execute() {
        Logger.getInstance().info("Executing event: " + this.toString());
        this.action.run();
    }

    @Override
    public int compareTo(Event other) {
        return Long.compare(this.timestamp, other.timestamp);
    }

    @Override
    public String toString() {
        return "[" + this.timestamp + "ms] " + this.type;
    }
}
