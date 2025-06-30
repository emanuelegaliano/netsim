package com.netsim.network;

import java.util.List;
import java.util.Random;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.addresses.Port;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;
import com.netsim.table.ArpTable;
import com.netsim.utils.Logger;

/**
 * Partial base implementation of Node with Network layer: holds
 *  - a list of {@link com.netsim.network.Interface interfaces}
 *  - routing and ARP tables
 * Subclasses must implement send() and receive()
 */
public abstract class NetworkNode implements Node {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = NetworkNode.class.getSimpleName();

    protected final String name;
    protected final List<Interface> interfaces;
    protected final RoutingTable routingTable;
    protected final ArpTable arpTable;

    protected NetworkNode(String name,
                          RoutingTable routingTable,
                          ArpTable arpTable,
                          List<Interface> interfaces) {
        if (name == null || routingTable == null || arpTable == null || interfaces == null) {
            logger.error("[" + CLS + "] invalid constructor arguments");
            throw new IllegalArgumentException(CLS + ": invalid arguments");
        }
        this.name = name;
        this.routingTable = routingTable;
        this.arpTable = arpTable;
        this.interfaces = interfaces;
        logger.info("[" + CLS + "] node '" + name + "' created with "
                    + interfaces.size() + " interfaces");
    }

    @Override
    public String getName() {
        return this.name;
    }

    /** look up the Interface owning the given IP */
    public Interface getInterface(IPv4 ip) {
        for (Interface iface : this.interfaces) {
            if (iface.getIP().equals(ip)) {
                logger.debug("[" + CLS + "] found interface for IP " + ip.stringRepresentation());
                return iface;
            }
        }
        logger.error("[" + CLS + "] Interface for IP " + ip.stringRepresentation() + " not found");
        throw new RuntimeException("Interface for IP " + ip.stringRepresentation() + " not found");
    }

    /** look up the Interface owning the given adapter */
    public Interface getInterface(NetworkAdapter adapter) {
        for (Interface iface : this.interfaces) {
            if (iface.getAdapter().equals(adapter)) {
                logger.debug("[" + CLS + "] found interface for adapter " + adapter.getName());
                return iface;
            }
        }
        logger.error("[" + CLS + "] Interface for adapter " + adapter.getName() + " not found");
        throw new RuntimeException("Interface for adapter " + adapter.getName() + " not found");
    }

    public List<Interface> getInterfaces() {
        return this.interfaces;
    }

    /**
     * query the routing table for next‐hop info
     * @throws RuntimeException if no entry has destination as key
     */
    public RoutingInfo getRoute(IPv4 destination) {
        try {
            RoutingInfo info = this.routingTable.lookup(destination);
            logger.debug("[" + CLS + "] route found for " + destination.stringRepresentation());
            return info;
        } catch (NullPointerException e) {
            logger.error("[" + CLS + "] route to " + destination.stringRepresentation() + " not found");
            throw new RuntimeException("Route to " + destination.stringRepresentation() + " not found");
        }
    }

    /**
     * query ARP table for a directly‐connected MAC
     * @throws RuntimeException if no entry has ip as key
     */
    public Mac getMac(IPv4 ip) {
        try {
            Mac mac = this.arpTable.lookup(ip);
            logger.debug("[" + CLS + "] ARP lookup for " + ip.stringRepresentation() + " → " + mac);
            return mac;
        } catch (NullPointerException e) {
            logger.error("[" + CLS + "] MAC for " + ip.stringRepresentation() + " not in ARP cache");
            throw new RuntimeException("MAC for " + ip.stringRepresentation() + " not in ARP cache");
        }
    }

    /**
     * Decide whether destination is on‐link (same subnet as any interface)
     * and return either the ARP entry or broadcast, otherwise broadcast.
     */
    public Mac getDestinationMac(IPv4 destination) {
        if (destination == null) {
            logger.error("[" + CLS + "] getDestinationMac: destination is null");
            throw new IllegalArgumentException("destination cannot be null");
        }
        for (Interface iface : interfaces) {
            IPv4 local = iface.getIP();
            int prefix = local.getMask();
            String netAddr = local.stringRepresentation();
            if (destination.isInSubnet(netAddr, prefix)) {
                Mac mac = getMac(destination);
                logger.info("[" + CLS + "] destination " + destination.stringRepresentation()
                            + " is on-link; MAC=" + mac);
                return mac;
            }
        }
        logger.info("[" + CLS + "] destination " + destination.stringRepresentation()
                    + " is off-link; using broadcast");
        return Mac.broadcast();
    }

    public int getMTU() {
        int mtu = Integer.MAX_VALUE;
        for (Interface iface : interfaces) {
            mtu = Math.min(mtu, iface.getAdapter().getMTU());
        }
        mtu = (mtu == Integer.MAX_VALUE ? 0 : mtu);
        logger.debug("[" + CLS + "] computed MTU=" + mtu);
        return mtu;
    }

    /**
     * Generates a random valid port in the range [1024…0xFFFF].
     */
    public Port randomPort() {
        Random rnd = new Random();
        int min = 1024;
        int max = 0xFFFF;
        int portNumber = rnd.nextInt(max - min + 1) + min;
        Port p = new Port(Integer.toString(portNumber));
        logger.debug("[" + CLS + "] generated random port " + p);
        return p;
    }

    public abstract void send(IPv4 destination, ProtocolPipeline protocols, byte[] data);
    public abstract void receive(ProtocolPipeline protocols, byte[] data);
}