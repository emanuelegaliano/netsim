package com.netsim.network;

import java.util.List;
import java.util.Random;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.addresses.Port;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;
import com.netsim.utils.Logger;

/**
 * Base class for nodes implementing IP routing and ARP resolution.
 */
public abstract class NetworkNode implements Node {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = NetworkNode.class.getSimpleName();

    protected final String         name;
    protected final List<Interface> interfaces;
    protected final RoutingTable   routingTable;
    protected final ArpTable       arpTable;

    /**
     * @param name         node identifier (non‐null)
     * @param routingTable routing table instance (non‐null)
     * @param arpTable     ARP table instance (non‐null)
     * @param interfaces   list of interfaces (non‐null)
     * @throws IllegalArgumentException if any argument is null
     */
    protected NetworkNode(String name,
                          RoutingTable routingTable,
                          ArpTable arpTable,
                          List<Interface> interfaces) {
        if (name == null
            || routingTable == null
            || arpTable == null
            || interfaces == null) {
            logger.error("[" + CLS + "] invalid constructor arguments");
            throw new IllegalArgumentException(CLS + ": invalid arguments");
        }
        this.name         = name;
        this.routingTable = routingTable;
        this.arpTable     = arpTable;
        this.interfaces   = interfaces;
        logger.info("[" + CLS + "] node '" + this.name
            + "' created with " + this.interfaces.size() + " interfaces");
    }

    /**
     * @return the node name
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Finds the Interface with the given IP.
     *
     * @param ip the IPv4 to look up (non‐null)
     * @return the matching Interface
     * @throws RuntimeException if no such interface exists
     */
    public Interface getInterface(IPv4 ip) {
        for (Interface iface : this.interfaces) {
            if (iface.getIP().equals(ip)) {
                logger.debug("[" + CLS + "] found interface for IP "
                    + ip.stringRepresentation());
                return iface;
            }
        }
        logger.error("[" + CLS + "] Interface for IP "
            + ip.stringRepresentation() + " not found");
        throw new RuntimeException("Interface for IP "
            + ip.stringRepresentation() + " not found");
    }

    /**
     * Finds the Interface with the given adapter.
     *
     * @param adapter the adapter to look up (non‐null)
     * @return the matching Interface
     * @throws RuntimeException if no such interface exists
     */
    public Interface getInterface(NetworkAdapter adapter) {
        for (Interface iface : this.interfaces) {
            if (iface.getAdapter().equals(adapter)) {
                logger.debug("[" + CLS + "] found interface for adapter "
                    + adapter.getName());
                return iface;
            }
        }
        logger.error("[" + CLS + "] Interface for adapter "
            + adapter.getName() + " not found");
        throw new RuntimeException("Interface for adapter "
            + adapter.getName() + " not found");
    }

    /**
     * @return the list of interfaces
     */
    public List<Interface> getInterfaces() {
        return this.interfaces;
    }

    /**
     * Looks up the route to a destination IP.
     *
     * @param destination the IPv4 destination (non‐null)
     * @return routing information
     * @throws RuntimeException if no route is found
     */
    public RoutingInfo getRoute(IPv4 destination) {
        try {
            RoutingInfo info = this.routingTable.lookup(destination);
            logger.debug("[" + CLS + "] route found for "
                + destination.stringRepresentation());
            return info;
        } catch (NullPointerException e) {
            logger.error("[" + CLS + "] route to "
                + destination.stringRepresentation() + " not found");
            throw new RuntimeException("Route to "
                + destination.stringRepresentation() + " not found");
        }
    }

    /**
     * Looks up the MAC for a directly connected IP.
     *
     * @param ip the IPv4 to resolve (non‐null)
     * @return the MAC address
     * @throws RuntimeException if not in ARP cache
     */
    public Mac getMac(IPv4 ip) {
        try {
            Mac mac = this.arpTable.lookup(ip);
            logger.debug("[" + CLS + "] ARP lookup for "
                + ip.stringRepresentation() + " → " + mac);
            return mac;
        } catch (NullPointerException e) {
            logger.error("[" + CLS + "] MAC for "
                + ip.stringRepresentation() + " not in ARP cache");
            throw new RuntimeException("MAC for "
                + ip.stringRepresentation() + " not in ARP cache");
        }
    }

    /**
     * Determines whether a destination is on‐link and returns
     * its MAC or the broadcast address.
     *
     * @param destination the IPv4 destination (non‐null)
     * @return on‐link MAC or broadcast
     * @throws IllegalArgumentException if destination is null
     */
    public Mac getDestinationMac(IPv4 destination) {
        if (destination == null) {
            logger.error("[" + CLS + "] getDestinationMac: destination is null");
            throw new IllegalArgumentException("destination cannot be null");
        }
        for (Interface iface : this.interfaces) {
            IPv4 local  = iface.getIP();
            int  prefix = local.getMask();
            String net  = local.stringRepresentation();
            if (destination.isInSubnet(net, prefix)) {
                Mac mac = this.getMac(destination);
                logger.info("[" + CLS + "] destination "
                    + destination.stringRepresentation()
                    + " is on-link; MAC=" + mac);
                return mac;
            }
        }
        logger.info("[" + CLS + "] destination "
            + destination.stringRepresentation()
            + " is off-link; using broadcast");
        return Mac.broadcast();
    }

    /**
     * Computes the minimum MTU across all interfaces.
     *
     * @return the effective MTU, or 0 if none
     */
    public int getMTU() {
        int mtu = Integer.MAX_VALUE;
        for (Interface iface : this.interfaces) {
            mtu = Math.min(mtu, iface.getAdapter().getMTU());
        }
        mtu = (mtu == Integer.MAX_VALUE ? 0 : mtu);
        logger.debug("[" + CLS + "] computed MTU=" + mtu);
        return mtu;
    }

    /**
     * Generates a random ephemeral port in [1024…65535].
     *
     * @return a new Port instance
     */
    public Port randomPort() {
        Random rnd      = new Random();
        int    min      = 1024;
        int    max      = 0xFFFF;
        int    portNum  = rnd.nextInt(max - min + 1) + min;
        Port   p        = new Port(Integer.toString(portNum));
        logger.debug("[" + CLS + "] generated random port " + p.getPort());
        return p;
    }

    /**
     * Send data to a destination via this node.
     *
     * @param destination IPv4 destination (non‐null)
     * @param protocols   protocol pipeline (non‐null)
     * @param data        payload bytes (non‐empty)
     */
    public abstract void send(IPv4 destination,
                              ProtocolPipeline protocols,
                              byte[] data);

    /**
     * Receive data coming up through the protocols.
     *
     * @param protocols protocol pipeline (non‐null)
     * @param data      payload bytes (non‐empty)
     */
    public abstract void receive(ProtocolPipeline protocols, byte[] data);
}