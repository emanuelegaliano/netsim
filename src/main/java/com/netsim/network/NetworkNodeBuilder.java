package com.netsim.network;

import java.util.ArrayList;
import java.util.List;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;

/**
 * Base builder providing common configuration methods for {@link NetworkNode}s.
 * <p>
 * Subclasses must implement {@link #build()} to return a concrete T.
 * </p>
 *
 * @param <T> the concrete NetworkNode type produced
 */
public abstract class NetworkNodeBuilder<T extends NetworkNode> {
    protected String name;
    protected final RoutingTable routingTable;
    protected final ArpTable arpTable;
    protected final List<Interface> interfaces;

    protected NetworkNodeBuilder() {
        this.routingTable = new RoutingTable();
        this.arpTable = new ArpTable();
        this.interfaces = new ArrayList<>();
    }

    /**
     * Sets the node's name.
     * 
     * @param name non-null identifier
     * @return this builder
     * @throws IllegalArgumentException if name is null
     */
    public NetworkNodeBuilder<T> setName(String name) throws IllegalArgumentException {
        if(name == null)
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": name cannot be null");

        this.name = name;
        return this;
    }

    /**
     * Adds a network interface to the node.
     * 
     * @param iface non-null Interface
     * @return this builder
     * @throws IllegalArgumentException if iface is null
     */
    public NetworkNodeBuilder<T> addInterface(Interface iface) throws IllegalArgumentException {
        if(iface == null) 
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": iface cannot be null");
        
        this.interfaces.add(iface);
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
    public NetworkNodeBuilder<T> addRoute(IPv4 subnet, String adapterName, IPv4 nextHop) throws IllegalArgumentException {
        if(subnet == null || adapterName == null || nextHop == null) 
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": arguments cannot be null");

        // find the interface by name
        NetworkAdapter adapter = interfaces.stream()
                                            .map(Interface::getAdapter)
                                            .filter(a -> a.getName().equals(adapterName))
                                            .findFirst()
                                            .orElseThrow(() -> 
                                                new IllegalArgumentException(
                                                    this.getClass().getSimpleName() + ": no interface named " 
                                                    + adapterName
                                                    )
                                            );
        this.routingTable.add(subnet, new RoutingInfo(adapter, nextHop));
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
    public NetworkNodeBuilder<T> addArpEntry(IPv4 ip, Mac mac) throws IllegalArgumentException {
        if(ip == null || mac == null) 
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": arguments cannot be null");
        
        this.arpTable.add(ip, mac);
        return this;
    }

    public abstract T build();
}
