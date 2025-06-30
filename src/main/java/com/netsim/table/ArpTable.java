package com.netsim.table;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.utils.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple ARP table implementing NetworkTable<IP, Mac>.
 * 
 * Although the NetworkTable interface mandates a NetworkAdapter parameter,
 * ARP entries are global per‐host, so the adapter argument is ignored.
 */
public class ArpTable implements NetworkTable<IPv4, Mac> {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = ArpTable.class.getSimpleName();

    private final Map<IPv4, Mac> table;

    public ArpTable() {
        this.table = new HashMap<>();
        logger.info("[" + CLS + "] initialized");
    }

    /**
     * sets 0.0.0.0 as gateway with Mac address router
     * @param router 
     * @throws IllegalArgumentException if router is null
     */
    public void setGateway(Mac router) {
        if (router == null) {
            logger.error("[" + CLS + "] setGateway: router cannot be null");
            throw new IllegalArgumentException("ArpTable: router cannot be null");
        }
        IPv4 gw = new IPv4("0.0.0.0", 0);
        table.put(gw, router);
        logger.info("[" + CLS + "] gateway set to " + router);
    }

    /** @return  */
    public Mac gateway() {
        try {
            Mac mac = lookup(new IPv4("0.0.0.0", 0));
            logger.info("[" + CLS + "] gateway lookup succeeded: " + mac);
            return mac;
        } catch (NullPointerException e) {
            logger.error("[" + CLS + "] gateway not set");
            logger.debug(e.getLocalizedMessage());
            throw new RuntimeException("ArpTable: default gateway not set yet");
        }
    }

    /**
     * Looks up the MAC address associated with the given IPv4 key.
     *
     * @param key the IP address to resolve
     * @return the Mac address if present
     * @throws IllegalArgumentException if key is null
     * @throws NullPointerException     if no entry exists for that IPv4
     */
    public Mac lookup(IPv4 key) {
        if (key == null) {
            logger.error("[" + CLS + "] lookup: key cannot be null");
            throw new IllegalArgumentException("ArpTable: key cannot be null");
        }
        Mac mac = table.get(key);
        if (mac == null) {
            logger.error("[" + CLS + "] lookup failed for IP " + key.stringRepresentation());
            throw new NullPointerException(
                "ArpTable: no MAC entry for IP " + key.stringRepresentation()
            );
        }
        logger.info("[" + CLS + "] lookup succeeded for IP " + key.stringRepresentation() + ": " + mac);
        return mac;
    }

    /**
     * Adds or updates an ARP entry mapping IPv4 → Mac.
     *
     * @param key   the IPv4 address (cannot be null)
     * @param value the Mac address (cannot be null)
     * @throws IllegalArgumentException if key or value is null
     */
    public void add(IPv4 key, Mac value) {
        if (key == null) {
            logger.error("[" + CLS + "] add: key (IPv4) cannot be null");
            throw new IllegalArgumentException("ArpTable.add: key (IPv4) cannot be null");
        }
        if (value == null) {
            logger.error("[" + CLS + "] add: value (Mac) cannot be null");
            throw new IllegalArgumentException("ArpTable.add: value (Mac) cannot be null");
        }
        table.put(key, value);
        logger.info("[" + CLS + "] added entry: " + key.stringRepresentation() + " -> " + value);
    }

    /**
     * Removes the ARP entry for the given IPv4 key.
     *
     * @param key the IPv4 address (cannot be null)
     * @throws IllegalArgumentException if key is null
     * @throws NullPointerException     if no entry exists for that IPv4
     */
    public void remove(IPv4 key) {
        if (key == null) {
            logger.error("[" + CLS + "] remove: key (IPv4) cannot be null");
            throw new IllegalArgumentException("ArpTable.remove: key (IPv4) cannot be null");
        }
        Mac removed = table.remove(key);
        if (removed == null) {
            logger.error("[" + CLS + "] remove failed: no entry for IP " + key.stringRepresentation());
            throw new NullPointerException(
                "ArpTable.remove: cannot remove IP " + key.stringRepresentation() + " (not present)"
            );
        }
        logger.info("[" + CLS + "] removed entry for IP " + key.stringRepresentation());
    }

    public boolean isEmpty() {
        boolean empty = table.isEmpty();
        logger.debug("[" + CLS + "] isEmpty = " + empty);
        return empty;
    }
}