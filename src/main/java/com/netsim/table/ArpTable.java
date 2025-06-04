// src/main/java/com/netsim/table/ArpTable.java
package com.netsim.table;

import com.netsim.addresses.IP;
import com.netsim.addresses.Mac;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple ARP table implementing NetworkTable<IP, Mac>.
 * 
 * Although the NetworkTable interface mandates a NetworkAdapter parameter,
 * ARP entries are global per‐host, so the adapter argument is ignored.
 */
public class ArpTable implements NetworkTable<IP, Mac> {
    private final Map<IP, Mac> table;

    public ArpTable() {
        this.table = new HashMap<>();
    }

    /**
     * Looks up the MAC address associated with the given IPv4 key.
     *
     * @param key the IP address to resolve
     * @return the Mac address if present
     * @throws IllegalArgumentException if key or device is null
     * @throws NullPointerException if no entry exists for that IPv4
     */
    public Mac lookup(IP key) throws IllegalArgumentException, NullPointerException {
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
     * @throws IllegalArgumentException if key, value, or device is null
     */
    public void add(IP key, Mac value) throws IllegalArgumentException {
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
    public void remove(IP key) throws IllegalArgumentException, NullPointerException {
        if(key == null) 
            throw new IllegalArgumentException("ArpTable.remove: key (IPv4) cannot be null");

        Mac macCheck = this.table.remove(key);

        if(macCheck == null) 
            throw new NullPointerException(
                "ArpTable.remove: cannot remove IP " + key.stringRepresentation() + " (not present)"
            );
    }
}