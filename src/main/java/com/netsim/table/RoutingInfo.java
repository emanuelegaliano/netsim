package com.netsim.table;

import com.netsim.addresses.IPv4;
import com.netsim.network.NetworkAdapter;
import com.netsim.utils.Logger;

/**
 * Encapsulates the next-hop IP and outgoing device for a route.
 */
public class RoutingInfo {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = RoutingInfo.class.getSimpleName();

    private NetworkAdapter device;
    private IPv4           nextHop;

    /**
     * Constructs a new RoutingInfo.
     *
     * @param device   the device to use to send packets (non-null)
     * @param nextHop  the next-hop IP, or null if directly attached
     * @throws IllegalArgumentException if device is null
     */
    public RoutingInfo(NetworkAdapter device, IPv4 nextHop) throws IllegalArgumentException {
        if (device == null) {
            logger.error("[" + CLS + "] constructor: device cannot be null");
            throw new IllegalArgumentException("RoutingInfo: device cannot be null");
        }
        this.device  = device;
        this.nextHop = nextHop;
        logger.info("[" + CLS + "] created with device=" + this.device.getName()
                    + " nextHop=" + (this.nextHop == null
                                     ? "direct"
                                     : this.nextHop.stringRepresentation()));
    }

    /**
     * Returns the next-hop IP, or null if directly connected.
     *
     * @return the next-hop IPv4, or null
     */
    public IPv4 getNextHop() {
        logger.debug("[" + CLS + "] getNextHop -> "
                     + (this.nextHop == null
                        ? "direct"
                        : this.nextHop.stringRepresentation()));
        return this.nextHop;
    }

    /**
     * Returns the outgoing network adapter.
     *
     * @return the NetworkAdapter
     */
    public NetworkAdapter getDevice() {
        logger.debug("[" + CLS + "] getDevice -> " + this.device.getName());
        return this.device;
    }

    /**
     * Updates the next-hop IP.
     *
     * @param newNextHop the new next-hop IPv4 (or null for direct)
     */
    public void setNextHop(IPv4 newNextHop) {
        this.nextHop = newNextHop;
        logger.info("[" + CLS + "] nextHop set to "
                    + (this.nextHop == null
                       ? "direct"
                       : this.nextHop.stringRepresentation()));
    }

    /**
     * Updates the outgoing device.
     *
     * @param newDevice the new NetworkAdapter (non-null)
     * @throws IllegalArgumentException if newDevice is null
     */
    public void setDevice(NetworkAdapter newDevice) throws IllegalArgumentException {
        if (newDevice == null) {
            logger.error("[" + CLS + "] setDevice: newDevice cannot be null");
            throw new IllegalArgumentException("RoutingInfo: newDevice cannot be null");
        }
        this.device = newDevice;
        logger.info("[" + CLS + "] device set to " + this.device.getName());
    }
}