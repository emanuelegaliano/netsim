package com.netsim.network;

import java.util.ArrayList;
import java.util.List;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;
import com.netsim.utils.Logger;

/**
 * Base builder providing common configuration methods for {@link NetworkNode}s.
 * <p>
 * Subclasses must implement {@link #build()} to return a concrete T.
 * </p>
 *
 * @param <T> the concrete NetworkNode type produced
 */
public abstract class NetworkNodeBuilder<T extends NetworkNode> {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = NetworkNodeBuilder.class.getSimpleName();

    protected String name;
    protected final RoutingTable routingTable;
    protected final ArpTable arpTable;
    protected final List<Interface> interfaces;

    protected NetworkNodeBuilder() {
        this.routingTable = new RoutingTable();
        this.arpTable = new ArpTable();
        this.interfaces = new ArrayList<>();
        logger.info("[" + CLS + "] new builder created");
    }

    /**
     * Sets the node's name.
     *
     * @param name non-null identifier
     * @return this builder
     * @throws IllegalArgumentException if name is null
     */
    public NetworkNodeBuilder<T> setName(String name) {
        if (name == null) {
            logger.error("[" + CLS + "] name cannot be null");
            throw new IllegalArgumentException(CLS + ": name cannot be null");
        }
        this.name = name;
        logger.info("[" + CLS + "] name set to '" + name + "'");
        return this;
    }

    /**
     * Adds a network interface to the node.
     *
     * @param iface non-null Interface
     * @return this builder
     * @throws IllegalArgumentException if iface is null
     */
    public NetworkNodeBuilder<T> addInterface(Interface iface) {
        if (iface == null) {
            logger.error("[" + CLS + "] interface cannot be null");
            throw new IllegalArgumentException(CLS + ": iface cannot be null");
        }
        this.interfaces.add(iface);
        logger.info("[" + CLS + "] interface added: adapter=" +
                    iface.getAdapter().getName() +
                    ", ip=" + iface.getIP().stringRepresentation());
        return this;
    }

    /**
     * Adds a route entry to the node's routing table.
     *
     * @param subnet      the destination subnet (non-null)
     * @param adapterName the name of one of this node's interfaces
     * @param nextHop     the next-hop IP (non-null)
     * @return this builder
     * @throws IllegalArgumentException if any argument is null, or if adapterName
     *                                  does not match any added Interface
     */
    public NetworkNodeBuilder<T> addRoute(IPv4 subnet, String adapterName, IPv4 nextHop) {
        if (subnet == null || adapterName == null || nextHop == null) {
            logger.error("[" + CLS + "] route arguments cannot be null");
            throw new IllegalArgumentException(CLS + ": arguments cannot be null");
        }
        Interface iface = this.interfaces.stream()
            .filter(i -> i.getAdapter().getName().equals(adapterName))
            .findFirst()
            .orElseThrow(() -> {
                logger.error("[" + CLS + "] no interface named " + adapterName);
                return new IllegalArgumentException(
                    CLS + ": no interface named " + adapterName);
            });
        routingTable.add(subnet, new RoutingInfo(iface.getAdapter(), nextHop));
        logger.info("[" + CLS + "] route added: subnet=" +
                    subnet.stringRepresentation() +
                    ", adapter=" + adapterName +
                    ", nextHop=" + nextHop.stringRepresentation());
        return this;
    }

    /**
     * Adds an entry to the node's ARP cache.
     *
     * @param ip  the IPv4 address (non-null)
     * @param mac the corresponding MAC (non-null)
     * @return this builder
     * @throws IllegalArgumentException if either argument is null
     */
    public NetworkNodeBuilder<T> addArpEntry(IPv4 ip, Mac mac) {
        if (ip == null || mac == null) {
            logger.error("[" + CLS + "] ARP entry arguments cannot be null");
            throw new IllegalArgumentException(CLS + ": arguments cannot be null");
        }
        arpTable.add(ip, mac);
        logger.info("[" + CLS + "] ARP entry added: ip=" +
                    ip.stringRepresentation() +
                    ", mac=" + mac.stringRepresentation());
        return this;
    }

    public abstract T build();
}