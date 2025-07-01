package com.netsim.table;

import java.util.HashMap;
import java.util.Map;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.utils.Logger;

/**
 * A simple ARP table mapping IPv4 addresses to MAC addresses.
 * Ignores the NetworkAdapter parameter of NetworkTable, as ARP is per‐host.
 */
public class ArpTable implements NetworkTable<IPv4, Mac> {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = ArpTable.class.getSimpleName();

    private final Map<IPv4, Mac> table;

    /**
     * Initializes an empty ARP table.
     */
    public ArpTable() {
        this.table = new HashMap<>();
        logger.info("[" + CLS + "] initialized");
    }

    /**
     * Sets the default gateway (0.0.0.0) to the given MAC.
     *
     * @param router the MAC address of the gateway (non-null)
     * @throws IllegalArgumentException if router is null
     */
    public void setGateway(Mac router) throws IllegalArgumentException {
        if (router == null) {
            logger.error("[" + CLS + "] setGateway: router cannot be null");
            throw new IllegalArgumentException("ArpTable: router cannot be null");
        }
        IPv4 gw = new IPv4("0.0.0.0", 0);
        this.table.put(gw, router);
        logger.info("[" + CLS + "] gateway set to " + router.stringRepresentation());
    }

    /**
     * Retrieves the MAC of the default gateway (0.0.0.0).
     *
     * @return the MAC address of the gateway
     * @throws RuntimeException if gateway not set
     */
    public Mac gateway() throws RuntimeException {
        try {
            Mac mac = lookup(new IPv4("0.0.0.0", 0));
            logger.info("[" + CLS + "] gateway lookup succeeded: " + mac.stringRepresentation());
            return mac;
        } catch (NullPointerException e) {
            logger.error("[" + CLS + "] gateway not set");
            logger.debug("[" + CLS + "] " + e.getLocalizedMessage());
            throw new RuntimeException("ArpTable: default gateway not set");
        }
    }

    /**
     * Looks up the MAC address for the given IPv4 key.
     *
     * @param key the IPv4 address to resolve (non-null)
     * @return the corresponding MAC address
     * @throws IllegalArgumentException if key is null
     * @throws NullPointerException     if no entry exists for key
     */
    @Override
    public Mac lookup(IPv4 key) throws IllegalArgumentException, NullPointerException {
        if (key == null) {
            logger.error("[" + CLS + "] lookup: key cannot be null");
            throw new IllegalArgumentException("ArpTable: key cannot be null");
        }
        Mac mac = this.table.get(key);
        if (mac == null) {
            logger.error("[" + CLS + "] lookup failed for IP " + key.stringRepresentation());
            throw new NullPointerException("ArpTable: no MAC entry for IP " + key.stringRepresentation());
        }
        logger.info("[" + CLS + "] lookup succeeded for IP " 
                    + key.stringRepresentation() + ": " + mac.stringRepresentation());
        return mac;
    }

    /**
     * Adds or updates an ARP entry mapping IPv4 → MAC.
     *
     * @param key   the IPv4 address (non-null)
     * @param value the MAC address (non-null)
     * @throws IllegalArgumentException if key or value is null
     */
    @Override
    public void add(IPv4 key, Mac value) throws IllegalArgumentException {
        if (key == null) {
            logger.error("[" + CLS + "] add: key cannot be null");
            throw new IllegalArgumentException("ArpTable.add: key cannot be null");
        }
        if (value == null) {
            logger.error("[" + CLS + "] add: value cannot be null");
            throw new IllegalArgumentException("ArpTable.add: value cannot be null");
        }
        this.table.put(key, value);
        logger.info("[" + CLS + "] added entry: " 
                    + key.stringRepresentation() + " -> " + value.stringRepresentation());
    }

    /**
     * Removes the ARP entry for the given IPv4 key.
     *
     * @param key the IPv4 address (non-null)
     * @throws IllegalArgumentException if key is null
     * @throws NullPointerException     if no entry exists for key
     */
    @Override
    public void remove(IPv4 key) throws IllegalArgumentException, NullPointerException {
        if (key == null) {
            logger.error("[" + CLS + "] remove: key cannot be null");
            throw new IllegalArgumentException("ArpTable.remove: key cannot be null");
        }
        Mac removed = this.table.remove(key);
        if (removed == null) {
            logger.error("[" + CLS + "] remove failed for IP " + key.stringRepresentation());
            throw new NullPointerException(
                "ArpTable.remove: no entry for IP " + key.stringRepresentation()
            );
        }
        logger.info("[" + CLS + "] removed entry for IP " + key.stringRepresentation());
    }

    /**
     * Checks if the ARP table is empty.
     *
     * @return true if no entries exist
     */
    @Override
    public boolean isEmpty() {
        boolean empty = this.table.isEmpty();
        logger.debug("[" + CLS + "] isEmpty = " + empty);
        return empty;
    }
}