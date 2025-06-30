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
 * Represents an end‐host on the network capable of running a single App.
 */
public class Host extends NetworkNode {
    private final Logger logger = Logger.getInstance();
    private final String CLS = this.getClass().getSimpleName();
    private App runningApp;

    /**
     * Constructs a Host node.
     *
     * @param name          node name (non-null)
     * @param routingTable  routing table (non-null)
     * @param arpTable      ARP table (non-null)
     * @param interfaces    attached interfaces (non-null, non-empty)
     * @throws IllegalArgumentException if any argument is null or interfaces empty
     */
    public Host(String name,
                RoutingTable routingTable,
                ArpTable arpTable,
                List<Interface> interfaces) throws IllegalArgumentException
    {
        super(name, routingTable, arpTable, interfaces);
        this.runningApp = null;
        this.logger.info("[" + this.CLS + "] initialized with " + interfaces.size() + " interface(s)");
    }

    /**
     * Sets the application to run on this host.
     *
     * @param newApp the App instance (non-null)
     * @throws IllegalArgumentException if newApp is null
     */
    public void setApp(App newApp) throws IllegalArgumentException {
        if (newApp == null) {
            this.logger.error("[" + this.CLS + "] cannot set null application");
            throw new IllegalArgumentException(this.CLS + ": app cannot be null");
        }
        this.runningApp = newApp;
        this.logger.info("[" + this.CLS + "] application set successfully");
    }

    /**
     * Launches the configured application.
     *
     * @throws IllegalArgumentException if no App has been set
     */
    public void runApp() throws IllegalArgumentException {
        if (this.runningApp == null) {
            this.logger.error("[" + this.CLS + "] no application set");
            throw new IllegalArgumentException(this.CLS + ": no App set");
        }
        this.logger.info("[" + this.CLS + "] starting application");
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
            this.logger.debug("[" + this.CLS + "] " + e.getLocalizedMessage());
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
            this.logger.error("[" + this.CLS + "] invalid arguments to send");
            throw new IllegalArgumentException(this.CLS + ": invalid arguments");
        }

        RoutingInfo route;
        try {
            route = this.getRoute(destination);
        } catch (RuntimeException e) {
            this.logger.error("[" + this.CLS + "] routing failed for destination " 
                              + destination.stringRepresentation());
            this.logger.debug("[" + this.CLS + "] " + e.getLocalizedMessage());
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

        this.logger.info("[" + this.CLS + "] sending packet to " + destination.stringRepresentation());
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
            this.logger.error("[" + this.CLS + "] invalid arguments to receive");
            throw new IllegalArgumentException(this.CLS + ": invalid arguments");
        }
        if (this.runningApp == null) {
            this.logger.error("[" + this.CLS + "] no application set");
            throw new RuntimeException(this.CLS + ": no application set");
        }

        Protocol p = stack.pop();
        if (!(p instanceof IPv4Protocol)) {
            this.logger.error("[" + this.CLS + "] expected IPv4 protocol, got "
                              + p.getClass().getSimpleName());
            throw new RuntimeException(this.CLS + ": expected IPv4 protocol");
        }
        IPv4Protocol ipProtocol = (IPv4Protocol) p;
        IPv4 destination = ipProtocol.extractDestination(packets);

        if (!this.isForMe(destination)) {
            this.logger.error("[" + this.CLS + "] packet not for me (dest=" 
                              + destination.stringRepresentation() + ")");
            return;
        }

        byte[] transport = ipProtocol.decapsulate(packets);
        this.logger.info("[" + this.CLS + "] received packet for " 
                         + destination.stringRepresentation());
        this.runningApp.receive(stack, transport);
    }
}