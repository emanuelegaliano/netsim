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

/**
 * A generic server node that wraps an application and handles IP
 * send/receive functionality.
 */
public class Server<AppType extends App> extends NetworkNode {
    private static final Logger logger = Logger.getInstance();
    private final String CLS = this.getClass().getSimpleName();

    private AppType app;

    /**
     * @param name         the node name (non-null)
     * @param routingTable the routing table (non-null)
     * @param arpTable     the ARP table (non-null)
     * @param interfaces   the list of network interfaces (non-empty)
     * @throws IllegalArgumentException if any argument is invalid
     */
    public Server(String name,
                  RoutingTable routingTable,
                  ArpTable arpTable,
                  List<Interface> interfaces) throws IllegalArgumentException {
        super(name, routingTable, arpTable, interfaces);
        logger.info("[" + this.CLS + "] initialized with " + interfaces.size() + " interface(s)");
        this.app = null;
    }

    /**
     * Associates and starts the application on this server.
     *
     * @param app the application instance (non-null)
     * @throws IllegalArgumentException if app is null
     */
    public void setApp(AppType app) throws IllegalArgumentException {
        if (app == null) {
            logger.error("[" + this.CLS + "] attempt to set null App");
            throw new IllegalArgumentException("Server: app cannot be null");
        }
        this.app = app;
        this.app.start();
        logger.info("[" + this.CLS + "] application set and started");
    }

    /**
     * Checks if the given IPv4 address belongs to one of this server's interfaces.
     *
     * @param destination the IPv4 to check (non-null)
     * @return true if the address matches an interface, false otherwise
     */
    public boolean isForMe(IPv4 destination) {
        try {
            this.getInterface(destination);
            return true;
        } catch (RuntimeException e) {
            logger.debug("[" + this.CLS + "] isForMe check failed: " + e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Sends raw data to the given IPv4, wrapped in an IPv4 header.
     *
     * @param destination the target IPv4 address (non-null)
     * @param stack       the protocol pipeline (non-null)
     * @param data        the payload bytes (non-empty)
     * @throws IllegalArgumentException if arguments are invalid
     */
    public void send(IPv4 destination, ProtocolPipeline stack, byte[] data) throws IllegalArgumentException {
        if (destination == null || stack == null || data == null || data.length == 0) {
            logger.error("[" + this.CLS + "] invalid arguments to send");
            throw new IllegalArgumentException("Server: invalid arguments");
        }

        try {
            RoutingInfo route = this.getRoute(destination);
            IPv4Protocol ipProto = new IPv4Protocol(
                this.getInterface(route.getDevice()).getIP(),
                destination,
                5,  /* IHL */
                0,  /* ToS */
                0,  /* ID */
                0,  /* flags */
                64, /* TTL */
                0,  /* protocol */
                this.getMTU()
            );
            byte[] encapsulated = ipProto.encapsulate(data);
            stack.push(ipProto);

            logger.info("[" + this.CLS + "] sending packet to " + destination.stringRepresentation());
            route.getDevice().send(stack, encapsulated);
        } catch (RuntimeException e) {
            logger.error("[" + this.CLS + "] routing failed for " + destination.stringRepresentation());
            logger.debug("[" + this.CLS + "] " + e.getLocalizedMessage());
        }
    }

    /**
     * Receives an IPv4‐encapsulated packet, decapsulates it, and forwards
     * the payload to the associated application.
     *
     * @param stack   the protocol pipeline (non-null)
     * @param packets the raw packet bytes (non-empty)
     * @throws IllegalArgumentException if arguments are invalid
     * @throws RuntimeException         if no application is set or protocol mismatch
     */
    public void receive(ProtocolPipeline stack, byte[] packets) throws IllegalArgumentException, RuntimeException {
        if (stack == null || packets == null || packets.length == 0) {
            logger.error("[" + this.CLS + "] invalid arguments to receive");
            throw new IllegalArgumentException("Server: invalid arguments");
        }

        if (this.app == null) {
            logger.error("[" + this.CLS + "] no application set to handle incoming packets");
            throw new RuntimeException("Server: no application set");
        }

        Protocol p = stack.pop();
        if (!(p instanceof IPv4Protocol)) {
            logger.error("[" + this.CLS + "] expected IPv4Protocol but got " + p.getClass().getSimpleName());
            throw new RuntimeException("Server: expected IPv4 protocol");
        }

        IPv4Protocol ipProtocol = (IPv4Protocol) p;
        IPv4 destination = ipProtocol.extractDestination(packets);

        if (!this.isForMe(destination)) {
            logger.error("[" + this.CLS + "] packet not for this server: dest=" + destination.stringRepresentation());
            return;
        }

        byte[] transport = ipProtocol.decapsulate(packets);
        logger.info("[" + this.CLS + "] received packet for " + destination.stringRepresentation()
                    + ", handing up to App");
        this.app.receive(stack, transport);
    }
}