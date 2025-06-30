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

public class Router extends NetworkNode {
    private static final Logger logger = Logger.getInstance();
    private final String CLS = this.getClass().getSimpleName();

    public Router(String name, RoutingTable routingTable, ArpTable arpTable, List<Interface> interfaces)
            throws IllegalArgumentException {
        super(name, routingTable, arpTable, interfaces);
        logger.info("[" + CLS + "] initialized with " + interfaces.size() + " interface(s)");
    }

    @Override
    public void send(IPv4 destination, ProtocolPipeline stack, byte[] data) {
        if (destination == null || stack == null || data == null || data.length == 0) {
            throw new IllegalArgumentException("Router: invalid arguments");
        }

        try {
            RoutingInfo route = this.getRoute(destination);
            NetworkAdapter outAdapter = route.getDevice();
            outAdapter.send(stack, data);
            logger.info("[" + CLS + "] forwarded packet to " 
                        + destination.stringRepresentation());
        } catch (RuntimeException e) {
            // only log that the router couldn't forward; 
            // don’t echo the upper‐layer message here
            logger.error("[" + CLS + "] cannot forward to " 
                        + destination.stringRepresentation());
            logger.debug("[" + CLS + "] routing failure: " 
                        + e.getLocalizedMessage());
        }
    }

    @Override
    public void receive(ProtocolPipeline stack, byte[] packets) {
        if (stack == null || packets == null || packets.length == 0) {
            throw new IllegalArgumentException("Router: invalid arguments");
        }

        Protocol p = stack.pop();
        if (!(p instanceof IPv4Protocol)) {
            logger.error("[" + CLS + "] expected IPv4Protocol but got " + p.getClass().getSimpleName());
            throw new RuntimeException("Router: expected ipv4 protocol");
        }

        IPv4Protocol ipProtocol = (IPv4Protocol) p;
        IPv4 dest = ipProtocol.extractDestination(packets);
        byte[] payload = ipProtocol.decapsulate(packets);
        int oldTTL = ipProtocol.getTtl();

        if (oldTTL == 0) {
            logger.error("[" + CLS + "] dropped packet due to TTL=0");
            return;
        }

        // decrement TTL and re-encapsulate
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
        logger.info("[" + CLS + "] received for " + dest.stringRepresentation() +
                    ", TTL decremented from " + oldTTL + " to " + (oldTTL - 1));
        this.send(dest, stack, encapsulated);
    }
}