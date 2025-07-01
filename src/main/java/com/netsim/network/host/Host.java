package com.netsim.network.host;

import java.util.List;

import com.netsim.app.App;
import com.netsim.addresses.IPv4;
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
 * Represents an end-host on the network capable of running a single App.
 */
public class Host extends NetworkNode {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = Host.class.getSimpleName();

    private App runningApp;

    /**
     * Constructs a Host node.
     *
     * @param name          node name (non-null)
     * @param routingTable  routing table (non-null)
     * @param arpTable      ARP table (non-null)
     * @param interfaces    attached interfaces (non-null, non-empty)
     * @throws IllegalArgumentException if any argument is null or interfaces is empty
     */
    public Host(String name,
                RoutingTable routingTable,
                ArpTable arpTable,
                List<Interface> interfaces) throws IllegalArgumentException
    {
        super(name, routingTable, arpTable, interfaces);
        this.runningApp = null;
        logger.info("[" + CLS + "] initialized with " + interfaces.size() + " interface(s)");
    }

    /**
     * Sets the application to run on this host.
     *
     * @param newApp the App instance (non-null)
     * @throws IllegalArgumentException if newApp is null
     */
    public void setApp(App newApp) throws IllegalArgumentException {
        if (newApp == null) {
            logger.error("[" + CLS + "] cannot set null application");
            throw new IllegalArgumentException(CLS + ": app cannot be null");
        }
        this.runningApp = newApp;
        logger.info("[" + CLS + "] application set successfully");
    }

    /**
     * Launches the configured application.
     *
     * @throws IllegalArgumentException if no App has been set
     */
    public void runApp() throws IllegalArgumentException {
        if (this.runningApp == null) {
            logger.error("[" + CLS + "] no application set");
            throw new IllegalArgumentException(CLS + ": no App set");
        }
        logger.info("[" + CLS + "] starting application");
        this.runningApp.start();
    }

    /**
     * Checks whether a packet destined for the given IP belongs to this host.
     *
     * @param destination the IPv4 destination (non-null)
     * @return true if one of this host’s interfaces matches destination
     */
    public boolean isForMe(IPv4 destination) {
        try {
            this.getInterface(destination);
            return true;
        } catch (RuntimeException e) {
            logger.debug("[" + CLS + "] " + e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Sends data to a destination IP by performing IP encapsulation and forwarding.
     *
     * @param destination the IPv4 destination (non-null)
     * @param stack       the protocol pipeline (non-null)
     * @param data        the payload bytes (non-null, non-empty)
     * @throws IllegalArgumentException if any argument invalid
     */
    public void send(IPv4 destination,
                     ProtocolPipeline stack,
                     byte[] data) throws IllegalArgumentException
    {
        if (destination == null || stack == null || data == null || data.length == 0) {
            logger.error("[" + CLS + "] invalid arguments to send");
            throw new IllegalArgumentException(CLS + ": invalid arguments");
        }

        RoutingInfo route;
        try {
            route = this.getRoute(destination);
        } catch (RuntimeException e) {
            logger.error("[" + CLS + "] routing failed for destination " 
                         + destination.stringRepresentation());
            logger.debug("[" + CLS + "] " + e.getLocalizedMessage());
            return;
        }

        IPv4Protocol ipProto = new IPv4Protocol(
            this.getInterface(route.getDevice()).getIP(),
            destination,
            5,          // IHL
            0,          // TOS
            0,          // identification
            0,          // flags
            64,         // TTL
            0,          // protocol
            this.getMTU()
        );
        byte[] encapsulated = ipProto.encapsulate(data);
        stack.push(ipProto);

        logger.info("[" + CLS + "] sending packet to " + destination.stringRepresentation());
        route.getDevice().send(stack, encapsulated);
    }

    /**
     * Receives raw packets, decapsulates IP, and delivers to the running App.
     *
     * @param stack   the protocol pipeline (non-null)
     * @param packets the raw packet bytes (non-null, non-empty)
     * @throws IllegalArgumentException if arguments invalid
     * @throws RuntimeException         if no App is set or wrong protocol
     */
    public void receive(ProtocolPipeline stack, byte[] packets)
            throws IllegalArgumentException, RuntimeException
    {
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
            logger.error("[" + CLS + "] expected IPv4 protocol, got "
                         + p.getClass().getSimpleName());
            throw new RuntimeException(CLS + ": expected IPv4 protocol");
        }
        IPv4Protocol ipProtocol = (IPv4Protocol) p;
        IPv4 destination = ipProtocol.extractDestination(packets);

        if (!this.isForMe(destination)) {
            logger.error("[" + CLS + "] packet not for me (dest=" 
                         + destination.stringRepresentation() + ")");
            return;
        }

        byte[] transport = ipProtocol.decapsulate(packets);
        logger.info("[" + CLS + "] received packet for " 
                    + destination.stringRepresentation());
        this.runningApp.receive(stack, transport);
    }
}