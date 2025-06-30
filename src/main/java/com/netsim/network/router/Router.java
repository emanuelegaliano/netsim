package com.netsim.network.router;

import java.util.List;

import com.netsim.addresses.IPv4;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.IPv4.IPv4Protocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;
import com.netsim.utils.Logger;

/**
 * A router node that forwards IPv4 packets according to its routing table.
 */
public class Router extends NetworkNode {
    private static final Logger logger = Logger.getInstance();
    private final String CLS = this.getClass().getSimpleName();

    /**
     * Constructs a Router with the given name, routing table, ARP table, and interfaces.
     *
     * @param name          the router's name (non-null)
     * @param routingTable  the routing table (non-null)
     * @param arpTable      the ARP table (non-null)
     * @param interfaces    list of interfaces (non-null, non-empty)
     * @throws IllegalArgumentException if any argument is null or interfaces empty
     */
    public Router(String name, RoutingTable routingTable, ArpTable arpTable, List<Interface> interfaces)
            throws IllegalArgumentException {
        super(name, routingTable, arpTable, interfaces);
        logger.info("[" + this.CLS + "] initialized with " + this.interfaces.size() + " interface(s)");
    }

    /**
     * Sends a packet to the next hop for the given destination.
     *
     * @param destination  the IPv4 destination address (non-null)
     * @param stack        the protocol pipeline (non-null)
     * @param data         the packet bytes (non-null, non-empty)
     * @throws IllegalArgumentException if arguments are invalid
     */
    @Override
    public void send(IPv4 destination, ProtocolPipeline stack, byte[] data) throws IllegalArgumentException {
        if (destination == null || stack == null || data == null || data.length == 0) {
            throw new IllegalArgumentException("Router.send: invalid arguments");
        }
        try {
            RoutingInfo route = this.getRoute(destination);
            NetworkAdapter outAdapter = route.getDevice();
            outAdapter.send(stack, data);
            logger.info("[" + this.CLS + "] forwarded packet to " + destination.stringRepresentation());
        } catch (RuntimeException e) {
            logger.error("[" + this.CLS + "] cannot forward to " + destination.stringRepresentation());
            logger.debug("[" + this.CLS + "] routing failure: " + e.getLocalizedMessage());
        }
    }

    /**
     * Receives an IPv4 packet, decrements its TTL, and forwards or drops it.
     *
     * @param stack    the protocol pipeline (non-null)
     * @param packets  the raw packet bytes (non-null, non-empty)
     * @throws IllegalArgumentException if arguments are invalid
     * @throws RuntimeException         if protocol extraction fails
     */
    @Override
    public void receive(ProtocolPipeline stack, byte[] packets)
            throws IllegalArgumentException, RuntimeException {
        if (stack == null || packets == null || packets.length == 0) {
            throw new IllegalArgumentException("Router.receive: invalid arguments");
        }

        Protocol p = stack.pop();
        if (!(p instanceof IPv4Protocol)) {
            logger.error("[" + this.CLS + "] expected IPv4Protocol but got " + p.getClass().getSimpleName());
            throw new RuntimeException("Router.receive: expected IPv4Protocol");
        }

        IPv4Protocol ipProtocol = (IPv4Protocol) p;
        IPv4 dest = ipProtocol.extractDestination(packets);
        byte[] payload = ipProtocol.decapsulate(packets);
        int oldTTL = ipProtocol.getTtl();

        if (oldTTL == 0) {
            logger.error("[" + this.CLS + "] dropped packet due to TTL=0");
            return;
        }

        // Decrement TTL and rebuild header
        IPv4Protocol newIp = new IPv4Protocol(
            ipProtocol.getSource(),
            dest,
            ipProtocol.getIHL(),
            ipProtocol.getTypeOfService(),
            ipProtocol.getIdentification(),
            ipProtocol.getFlags(),
            oldTTL - 1,
            ipProtocol.getProtocol(),
            ipProtocol.getMTU()
        );
        byte[] encapsulated = newIp.encapsulate(payload);

        stack.push(newIp);
        logger.info("[" + this.CLS + "] received for " + dest.stringRepresentation()
                    + ", TTL decremented from " + oldTTL + " to " + (oldTTL - 1));
        this.send(dest, stack, encapsulated);
    }
}