package com.netsim.network.server;

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

public class Server<AppType extends App> extends NetworkNode {
    private static final Logger logger = Logger.getInstance();
    private final String CLS = this.getClass().getSimpleName();

    private AppType app;

    public Server(String name,
                  RoutingTable routingTable,
                  ArpTable arpTable,
                  List<Interface> interfaces)
            throws IllegalArgumentException {
        super(name, routingTable, arpTable, interfaces);
        logger.info("[" + CLS + "] initialized with " + interfaces.size() + " interface(s)");
        this.app = null;
    }

    public void setApp(AppType app) {
        if (app == null) {
            logger.error("[" + CLS + "] attempt to set null App");
            throw new IllegalArgumentException("Server: app cannot be null");
        }
        this.app = app;
        this.app.start();
        logger.info("[" + CLS + "] application set and started");
    }

    public boolean isForMe(IPv4 destination) {
        try {
            this.getInterface(destination);
            return true;
        } catch (RuntimeException e) {
            logger.debug("[" + CLS + "] isForMe check failed: " + e.getLocalizedMessage());
            return false;
        }
    }

    public void send(IPv4 destination, ProtocolPipeline stack, byte[] data) {
        if (destination == null || stack == null || data == null || data.length == 0) {
            logger.error("[" + CLS + "] invalid arguments to send");
            throw new IllegalArgumentException("Server: invalid arguments");
        }

        try {
            RoutingInfo route = this.getRoute(destination);
            IPv4Protocol ipProto = new IPv4Protocol(
                this.getInterface(route.getDevice()).getIP(),
                destination,
                5, 0, 0, 0,
                64, 0, this.getMTU()
            );
            byte[] encapsulated = ipProto.encapsulate(data);
            stack.push(ipProto);

            logger.info("[" + CLS + "] sending packet to " + destination.stringRepresentation());
            route.getDevice().send(stack, encapsulated);
        } catch (RuntimeException e) {
            logger.error("[" + CLS + "] routing failed for " + destination.stringRepresentation());
            logger.debug("[" + CLS + "] " + e.getLocalizedMessage());
        }
    }

    public void receive(ProtocolPipeline stack, byte[] packets) {
        if (stack == null || packets == null || packets.length == 0) {
            logger.error("[" + CLS + "] invalid arguments to receive");
            throw new IllegalArgumentException("Server: invalid arguments");
        }

        if (this.app == null) {
            logger.error("[" + CLS + "] no application set to handle incoming packets");
            throw new RuntimeException("Server: no application set");
        }

        Protocol p = stack.pop();
        if (!(p instanceof IPv4Protocol)) {
            logger.error("[" + CLS + "] expected IPv4Protocol but got " + p.getClass().getSimpleName());
            throw new RuntimeException("Server: expected IPv4 protocol");
        }

        IPv4Protocol ipProtocol = (IPv4Protocol) p;
        IPv4 destination    = ipProtocol.extractDestination(packets);

        if (!isForMe(destination)) {
            logger.error("[" + CLS + "] packet not for this server: dest=" + destination.stringRepresentation());
            return;
        }

        byte[] transport = ipProtocol.decapsulate(packets);
        logger.info("[" + CLS + "] received packet for " + destination.stringRepresentation() +
                     ", handing up to App");
        this.app.receive(stack, transport);
    }
}