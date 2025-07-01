package com.netsim.table;

import java.util.HashMap;
import java.util.Map;

import com.netsim.addresses.IPv4;
import com.netsim.utils.Logger;

/**
 * A routing table mapping IPv4 subnets to {@link RoutingInfo}.
 */
public class RoutingTable implements NetworkTable<IPv4, RoutingInfo> {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = RoutingTable.class.getSimpleName();

    private final HashMap<IPv4, RoutingInfo> table;

    /**
     * Constructs an empty RoutingTable.
     */
    public RoutingTable() {
        this.table = new HashMap<>();
        logger.info("[" + CLS + "] initialized");
    }

    /**
     * Looks up the best-matching route for the given destination.
     *
     * @param destination the IPv4 address to route (non-null)
     * @return the {@link RoutingInfo} for the best match
     * @throws IllegalArgumentException if destination is null
     * @throws NullPointerException     if no route matches
     */
    public RoutingInfo lookup(IPv4 destination) throws IllegalArgumentException, NullPointerException {
        if (destination == null) {
            logger.error("[" + CLS + "] lookup: destination cannot be null");
            throw new IllegalArgumentException("RoutingTable: destination cannot be null");
        }

        RoutingInfo bestMatch = null;
        int         bestPrefix = -1;
        for (Map.Entry<IPv4, RoutingInfo> e : this.table.entrySet()) {
            IPv4 subnet = e.getKey();
            int  prefix = subnet.getMask();
            if (destination.isInSubnet(subnet.stringRepresentation(), prefix)
                    && prefix > bestPrefix) {
                bestMatch = e.getValue();
                bestPrefix = prefix;
            }
        }

        if (bestMatch == null) {
            logger.error("[" + CLS + "] lookup: no route found for " + destination.stringRepresentation());
            throw new NullPointerException(
                "RoutingTable: no route found for " + destination.stringRepresentation()
            );
        }

        logger.debug("[" + CLS + "] lookup: selected route via "
                     + bestMatch.getDevice().getName()
                     + (bestMatch.getNextHop() != null
                        ? " nextHop=" + bestMatch.getNextHop().stringRepresentation()
                        : " direct"));
        return bestMatch;
    }

    /**
     * Adds a new route for the given subnet.
     *
     * @param destination the IPv4 subnet (non-null)
     * @param route       the RoutingInfo (non-null)
     * @throws IllegalArgumentException if destination or route is null
     * @throws RuntimeException         if a route for this subnet already exists
     */
    public void add(IPv4 destination, RoutingInfo route) throws IllegalArgumentException, RuntimeException {
        if (destination == null) {
            logger.error("[" + CLS + "] add: destination cannot be null");
            throw new IllegalArgumentException("RoutingTable: destination cannot be null");
        }
        if (route == null) {
            logger.error("[" + CLS + "] add: route cannot be null");
            throw new IllegalArgumentException("RoutingTable: route cannot be null");
        }
        if (this.table.containsKey(destination)) {
            logger.error("[" + CLS + "] add: route for " + destination.stringRepresentation() + " already exists");
            throw new RuntimeException("RoutingTable: route already contained");
        }
        this.table.put(destination, route);
        logger.info("[" + CLS + "] add: added route to " + destination.stringRepresentation());
    }

    /**
     * Sets or replaces the default (0.0.0.0/0) route.
     *
     * @param route the RoutingInfo for default route (non-null)
     * @throws IllegalArgumentException if route is null
     */
    public void setDefault(RoutingInfo route) throws IllegalArgumentException {
        if (route == null) {
            logger.error("[" + CLS + "] setDefault: route cannot be null");
            throw new IllegalArgumentException("RoutingTable: route cannot be null");
        }
        IPv4 defaultIP = new IPv4("0.0.0.0", 0);
        if (this.table.containsKey(defaultIP)) {
            this.table.remove(defaultIP);
            logger.debug("[" + CLS + "] setDefault: removed existing default route");
        }
        this.table.put(defaultIP, route);
        logger.info("[" + CLS + "] setDefault: set default route via " + route.getDevice().getName());
    }

    /**
     * Removes the route for the given subnet.
     *
     * @param destination the IPv4 subnet (non-null)
     * @throws IllegalArgumentException if destination is null
     * @throws NullPointerException     if no such route exists
     */
    public void remove(IPv4 destination) throws IllegalArgumentException, NullPointerException {
        if (destination == null) {
            logger.error("[" + CLS + "] remove: destination cannot be null");
            throw new IllegalArgumentException("RoutingTable: destination cannot be null");
        }
        RoutingInfo removed = this.table.remove(destination);
        if (removed == null) {
            logger.error("[" + CLS + "] remove: no route for " + destination.stringRepresentation());
            throw new NullPointerException(
                "RoutingTable: unable to remove " + destination.stringRepresentation()
            );
        }
        logger.info("[" + CLS + "] remove: removed route to " + destination.stringRepresentation());
    }

    /**
     * @return the number of entries in this table
     */
    public int size() {
        return this.table.size();
    }

    /**
     * Clears all routes from this table.
     */
    public void clear() {
        this.table.clear();
        logger.info("[" + CLS + "] clear: all routes removed");
    }

    /**
     * @return true if the table contains no entries
     */
    public boolean isEmpty() {
        return this.table.isEmpty();
    }
}