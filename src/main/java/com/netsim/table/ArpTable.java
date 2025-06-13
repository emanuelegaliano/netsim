// src/main/java/com/netsim/table/ArpTable.java
package com.netsim.table;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;

import java.util.HashMap;
import java.util.Map;
import com.netsim.utils.Logger;

/**
 * A simple ARP table implementing NetworkTable<IP, Mac>.
 * 
 * Although the NetworkTable interface mandates a NetworkAdapter parameter,
 * ARP entries are global per‐host, so the adapter argument is ignored.
 */
public class ArpTable implements NetworkTable<IPv4, Mac> {
    private final Map<IPv4, Mac> table;

    public ArpTable() {
        this.table = new HashMap<>();
    }

    /**
     * sets 0.0.0.0 as gateway with Mac address router
     * @param router 
     * @throws IllegalArgumentException if router is null
     */
    public void setGateway(Mac router) throws IllegalArgumentException {
        if(router == null)
            throw new IllegalArgumentException("ArpTable: router cannot be null");
        
        this.table.put(new IPv4("0.0.0.0", 0), router);
    }

    /** @return  */
    public Mac gateway() {
        try {
            return this.lookup(new IPv4("0.0.0.0", 0));
        } catch(final NullPointerException e) {
            Logger.getInstance().error(e.getLocalizedMessage());
            throw new RuntimeException("ArpTable: default gateway not setted yet");
        }
    }

    /**
     * Looks up the MAC address associated with the given IPv4 key.
     *
     * @param key the IP address to resolve
     * @return the Mac address if present
     * @throws IllegalArgumentException if key is null
     * @throws NullPointerException if no entry exists for that IPv4
     */
    public Mac lookup(IPv4 key) throws IllegalArgumentException, NullPointerException {
        if(key == null) 
            throw new IllegalArgumentException("ArpTable: key cannot be null");

        Mac mac = this.table.get(key);
        if(mac == null)
            throw new NullPointerException(
                "ArpTable: no MAC entry for IP " + key.stringRepresentation()
            );

        return mac;
    }

    /**
     * Adds or updates an ARP entry mapping IPv4 → Mac.
     *
     * @param key the IPv4 address (cannot be null)
     * @param value the Mac address (cannot be null)
     * @throws IllegalArgumentException if key or value is null
     */
    public void add(IPv4 key, Mac value) throws IllegalArgumentException {
        if(key == null)
            throw new IllegalArgumentException("ArpTable.add: key (IPv4) cannot be null");
        if(value == null)
            throw new IllegalArgumentException("ArpTable.add: value (Mac) cannot be null");

        this.table.put(key, value);
    }

    /**
     * Removes the ARP entry for the given IPv4 key.
     *
     * @param key the IPv4 address (cannot be null)
     * @throws IllegalArgumentException if key is null
     * @throws NullPointerException if no entry exists for that IPv4
     */
    public void remove(IPv4 key) throws IllegalArgumentException, NullPointerException {
        if(key == null) 
            throw new IllegalArgumentException("ArpTable.remove: key (IPv4) cannot be null");

        Mac macCheck = this.table.remove(key);

        if(macCheck == null) 
            throw new NullPointerException(
                "ArpTable.remove: cannot remove IP " + key.stringRepresentation() + " (not present)"
            );
    }
}