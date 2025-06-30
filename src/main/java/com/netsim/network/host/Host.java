package com.netsim.network.host;

import java.util.List;

import com.netsim.addresses.IPv4;
import com.netsim.app.App;
import com.netsim.network.Interface;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.IPv4.IPv4Protocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;
import com.netsim.utils.Logger;

public class Host extends NetworkNode {
    private final Logger logger = Logger.getInstance();
    private final String CLS = this.getClass().getSimpleName();

    private App runningApp;

    public Host(String name, RoutingTable routingTable, ArpTable arpTable, List<Interface> interfaces)
            throws IllegalArgumentException {
        super(name, routingTable, arpTable, interfaces);
        this.runningApp = null;
        logger.info("[" + CLS + "] initialized with " + interfaces.size() + " interface(s)");
    }

    public void setApp(App newApp) {
        if (newApp == null) {
            logger.error("[" + CLS + "] cannot set null application");
            throw new IllegalArgumentException(CLS + ": app cannot be null");
        }
        this.runningApp = newApp;
        logger.info("[" + CLS + "] application set successfully");
    }

    public void runApp() {
        if (this.runningApp == null) {
            logger.error("[" + CLS + "] no application set");
            throw new IllegalArgumentException(CLS + ": no App set");
        }
        logger.info("[" + CLS + "] starting application");
        this.runningApp.start();
    }

    public boolean isForMe(IPv4 destination) {
        try {
            this.getInterface(destination);
            return true;
        } catch (RuntimeException e) {
            logger.debug("[" + CLS + "] " + e.getLocalizedMessage());
            return false;
        }
    }

    public void send(IPv4 destination, ProtocolPipeline stack, byte[] data) {
        if (destination == null || stack == null || data == null || data.length == 0) {
            logger.error("[" + CLS + "] invalid arguments to send");
            throw new IllegalArgumentException(CLS + ": invalid arguments");
        }

        RoutingInfo route;
        try {
            route = this.getRoute(destination);
        } catch (RuntimeException e) {
            logger.error("[" + CLS + "] routing failed for destination " + destination.stringRepresentation());
            logger.debug("[" + CLS + "] " + e.getLocalizedMessage());
            return;
        }

        IPv4Protocol ipProto = new IPv4Protocol(
            this.getInterface(route.getDevice()).getIP(),
            destination,
            5, 0, 0, 0, 64, 0,
            this.getMTU()
        );
        byte[] encapsulated = ipProto.encapsulate(data);
        stack.push(ipProto);

        logger.info("[" + CLS + "] sending packet to " + destination.stringRepresentation());
        route.getDevice().send(stack, encapsulated);
    }

    public void receive(ProtocolPipeline stack, byte[] packets) {
        if (stack == null || packets == null || packets.length == 0) {
            logger.error("[" + CLS + "] invalid arguments to receive");
            throw new IllegalArgumentException(CLS + ": invalid arguments");
        }
        if (this.runningApp == null) {
            logger.error("[" + CLS + "] no application set");
            throw new RuntimeException(CLS + ": no application set");
        }

        Protocol p = stack.pop();
        if (!(p instanceof IPv4Protocol)) {
            logger.error("[" + CLS + "] expected IPv4 protocol, got " + p.getClass().getSimpleName());
            throw new RuntimeException(CLS + ": expected IPv4 protocol");
        }

        IPv4Protocol ipProtocol = (IPv4Protocol) p;
        IPv4 destination = ipProtocol.extractDestination(packets);

        if (!this.isForMe(destination)) {
            logger.error("[" + CLS + "] packet not for me (dest=" + destination.stringRepresentation() + ")");
            return;
        }

        byte[] transport = ipProtocol.decapsulate(packets);
        logger.info("[" + CLS + "] received packet for " + destination.stringRepresentation());
        this.runningApp.receive(stack, transport);
    }
}