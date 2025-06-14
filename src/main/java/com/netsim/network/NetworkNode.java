package com.netsim.network;

import java.util.List;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;
import com.netsim.table.ArpTable;

/**
 * Partial base implementation of Node with Network layer: holds
 *  - a list of {@link com.netsim.network.Interface interfaces}
 *  - routing and ARP tables
 * Subclasses must implement send() and receive()
 */
public abstract class NetworkNode implements Node {
    protected final String name;
    protected final List<Interface> interfaces;
    protected final RoutingTable routingTable;
    protected final ArpTable arpTable;

    protected NetworkNode(String name, RoutingTable routingTable, ArpTable arpTable, List<Interface> interfaces) {
        if(name == null || routingTable == null || arpTable == null || interfaces == null)
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": invalid arguments");

        this.name = name;
        this.routingTable = routingTable;
        this.arpTable = arpTable;
        this.interfaces = interfaces;
    }

    public String getName() {
        return this.name;
    }

    /** look up the Interface owning the given IP */
    public Interface getInterface(IPv4 ip) {
        for(Interface iface : this.interfaces) {
            if(iface.getIP().equals(ip))
                return iface;
        }
        throw new RuntimeException("Interface for IP " + ip.stringRepresentation() + " not found");
    }

    /** look up the Interface owning the given adapter */
    public Interface getInterface(NetworkAdapter adapter) {
        for(Interface iface : this.interfaces) {
            if(iface.getAdapter().equals(adapter)) 
                return iface;
        }
        throw new RuntimeException("Interface for adapter " + adapter.getName() + " not found");
    }

    public List<Interface> getInterfaces() {
        return this.interfaces;
    }

    /** query the routing table for next‐hop info */
    public RoutingInfo getRoute(IPv4 destination) {
        try {
            return this.routingTable.lookup(destination);
        } catch (NullPointerException e) {
            throw new RuntimeException("Route to " + destination.stringRepresentation() + " not found");
        }
    }

    /** query ARP table for a directly‐connected MAC */
    public Mac getMac(IPv4 ip) {
        try {
            return this.arpTable.lookup(ip);
        } catch (NullPointerException e) {
            throw new RuntimeException("MAC for " + ip.stringRepresentation() + " not in ARP cache");
        }
    }

    /**
     * Decide whether destination is on‐link (same subnet as any interface)
     * and return either the ARP entry or broadcast, otherwise delegate
     * to routing for next‐hop and repeat.
     */
    public Mac getDestinationMac(IPv4 destination) {
        if(destination == null) {
            throw new IllegalArgumentException("destination cannot be null");
        }
        // check if on‐link
        for(Interface iface : interfaces) {
            IPv4 local = iface.getIP();
            int prefix = local.getMask();
            String netAddr = local.stringRepresentation();
            if(destination.isInSubnet(netAddr, prefix))
                return getMac(destination);

        }
        // off-link → broadcast
        return Mac.broadcast();
    }

    public int getMTU() {
        int mtu = Integer.MAX_VALUE;
        for(Interface iface : interfaces) 
            mtu = Math.min(mtu, iface.getAdapter().getMTU());
        
        return (mtu == Integer.MAX_VALUE ? 0 : mtu);
    }

    public int randomPort() {
        return new java.util.Random().nextInt(0xFFFF - 1024 + 1) + 1024;
    }
    
    public abstract void send(RoutingInfo route, ProtocolPipeline protocols, byte[] data);
    public abstract void receive(ProtocolPipeline protocols, byte[] data);
}