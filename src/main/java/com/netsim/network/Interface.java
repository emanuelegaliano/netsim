package com.netsim.network;

import com.netsim.addresses.IPv4;
import com.netsim.utils.Logger;

/**
 * Represents a network interface binding a physical adapter to an IPv4 address.
 */
public final class Interface {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = Interface.class.getSimpleName();

    private final NetworkAdapter adapter;
    private final IPv4           ip;

    /**
     * Constructs an Interface with the given adapter and IP.
     *
     * @param adapter the underlying network adapter (non-null)
     * @param ip      the IPv4 address assigned to the interface (non-null)
     * @throws IllegalArgumentException if adapter or ip is null
     */
    public Interface(NetworkAdapter adapter, IPv4 ip) {
        if (adapter == null || ip == null) {
            logger.error("[" + CLS + "] constructor arguments cannot be null");
            throw new IllegalArgumentException("Interface: arguments cannot be null");
        }
        this.adapter = adapter;
        this.ip      = ip;
        logger.info("[" + CLS + "] created for adapter \"" 
            + this.adapter.getName() + "\" with IP " 
            + this.ip.stringRepresentation());
    }

    /**
     * Returns the associated network adapter.
     *
     * @return the network adapter
     */
    public NetworkAdapter getAdapter() {
        return this.adapter;
    }

    /**
     * Returns the IPv4 address assigned to this interface.
     *
     * @return the IPv4 address
     */
    public IPv4 getIP() {
        return this.ip;
    }

    /**
     * Compares this Interface to another for equality.
     *
     * @param obj the object to compare against
     * @return true if obj is an Interface with the same adapter and IP
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Interface)) {
            logger.debug("[" + CLS + "] equals: object is not an Interface");
            return false;
        }
        Interface other = (Interface) obj;
        boolean result = other.getAdapter().equals(this.adapter)
                      && other.getIP().equals(this.ip);
        logger.debug("[" + CLS + "] equals: comparison result=" + result);
        return result;
    }
}