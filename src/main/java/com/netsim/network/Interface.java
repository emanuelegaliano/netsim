package com.netsim.network;

import com.netsim.addresses.IPv4;
import com.netsim.utils.Logger;

public final class Interface {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = Interface.class.getSimpleName();

    private final NetworkAdapter adapter;
    private final IPv4 ip;

    /**
     * @param adapter
     * @param ip
     * @throws IllegalArgumentException if either adapter or ip is null
     */
    public Interface(NetworkAdapter adapter, IPv4 ip) {
        if (adapter == null || ip == null) {
            logger.error("[" + CLS + "] arguments to constructor cannot be null");
            throw new IllegalArgumentException("Interface: arguments cannot be null");
        }
        this.adapter = adapter;
        this.ip = ip;
        logger.info("[" + CLS + "] created for adapter \"" 
            + adapter.getName() + "\" with IP " + ip.stringRepresentation());
    }

    public NetworkAdapter getAdapter() {
        return this.adapter;
    }

    public IPv4 getIP() {
        return this.ip;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Interface)) {
            logger.debug("[" + CLS + "] equals: object is not an Interface");
            return false;
        }
        Interface other = (Interface) obj;
        boolean eq = other.getAdapter().equals(this.adapter)
                  && other.getIP().equals(this.ip);
        logger.debug("[" + CLS + "] equals: comparing to another Interface, result=" + eq);
        return eq;
    }
}