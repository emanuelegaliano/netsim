package com.netsim.network;

import com.netsim.addresses.Address;
import com.netsim.addresses.Mac;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.SimpleDLL.SimpleDLLProtocol;
import com.netsim.utils.Logger;

/**
 * Represents a point‐to‐point network adapter for sending/receiving raw frames.
 */
public final class NetworkAdapter {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = NetworkAdapter.class.getSimpleName();

    private final String       name;
    private final int          MTU;
    private final Mac          macAddress;
    private       NetworkAdapter remote;
    private       Node          owner;
    private       boolean       isUp;

    /**
     * Constructs a new NetworkAdapter.
     *
     * @param name       adapter identifier (non‐null)
     * @param MTU        maximum transmission unit
     * @param macAddress hardware MAC address (non‐null)
     * @throws IllegalArgumentException if name or macAddress is null
     */
    public NetworkAdapter(String name, int MTU, Mac macAddress) {
        if (name == null) {
            logger.error("[" + CLS + "] name cannot be null");
            throw new IllegalArgumentException("NetworkAdapter: name cannot be null");
        }
        if (macAddress == null) {
            logger.error("[" + CLS + "] macAddress cannot be null");
            throw new IllegalArgumentException("NetworkAdapter: mac address cannot be null");
        }
        this.name       = name;
        this.MTU        = MTU;
        this.macAddress = macAddress;
        this.remote     = null;
        this.isUp       = true;
        logger.info("[" + CLS + "] created adapter \"" + this.name
            + "\" with MTU=" + this.MTU
            + " and MAC=" + this.macAddress.stringRepresentation());
    }

    /**
     * Sets the owning Node.
     *
     * @param newOwner the Node that owns this adapter (non‐null)
     * @throws IllegalArgumentException if newOwner is null
     */
    public void setOwner(Node newOwner) {
        if (newOwner == null) {
            logger.error("[" + CLS + "] cannot set null owner");
            throw new IllegalArgumentException("NetworkAdapter: node owner cannot be null");
        }
        this.owner = newOwner;
        logger.info("[" + CLS + "] adapter \"" + this.name
            + "\" owner set to node \"" + this.owner.getName() + "\"");
    }

    /**
     * Returns the owning Node.
     *
     * @return the Node owning this adapter
     * @throws NullPointerException if owner not set
     */
    public Node getNode() {
        if (this.owner == null) {
            logger.error("[" + CLS + "] owner not set");
            throw new NullPointerException("NetworkAdapter: node owner not set");
        }
        return this.owner;
    }

    /**
     * Links to a remote adapter (cable).
     *
     * @param newRemoteAdapter the adapter at the other end (non‐null)
     * @throws IllegalArgumentException if newRemoteAdapter is null
     */
    public void setRemoteAdapter(NetworkAdapter newRemoteAdapter) {
        if (newRemoteAdapter == null) {
            logger.error("[" + CLS + "] cannot set null remote adapter");
            throw new IllegalArgumentException("NetworkAdapter: remote adapter cannot be null");
        }
        this.remote = newRemoteAdapter;
        logger.info("[" + CLS + "] adapter \"" + this.name
            + "\" linked to remote adapter \"" + this.remote.getName() + "\"");
    }

    /**
     * Returns the linked remote adapter.
     *
     * @return the remote adapter
     * @throws NullPointerException if not connected
     */
    public NetworkAdapter getLinkedAdapter() {
        if (this.remote == null) {
            logger.error("[" + CLS + "] no remote adapter connected");
            throw new NullPointerException("NetworkAdapter: remote adapter not connected");
        }
        return this.remote;
    }

    /** @return adapter name */
    public String getName() {
        return this.name;
    }

    /** @return adapter MTU */
    public int getMTU() {
        return this.MTU;
    }

    /** @return adapter MAC address */
    public Mac getMacAddress() {
        return this.macAddress;
    }

    /** @return true if adapter is up */
    public boolean isUp() {
        return this.isUp;
    }

    /** Brings the adapter up. */
    public void setUp() {
        this.isUp = true;
        logger.info("[" + CLS + "] adapter \"" + this.name + "\" is UP");
    }

    /** Brings the adapter down. */
    public void setDown() {
        this.isUp = false;
        logger.info("[" + CLS + "] adapter \"" + this.name + "\" is DOWN");
    }

    /**
     * Sends a raw frame to the linked adapter using DLL framing.
     *
     * @param stack protocol pipeline (non‐null)
     * @param frame payload bytes (non‐empty)
     * @throws IllegalArgumentException if stack or frame is null/empty
     * @throws RuntimeException         if adapter is down or unlinked
     */
    public void send(ProtocolPipeline stack, byte[] frame) {
        if (stack == null || frame == null || frame.length == 0) {
            logger.error("[" + CLS + "] invalid arguments to send");
            throw new IllegalArgumentException("NetworkAdapter: invalid arguments");
        }
        if (!this.isUp) {
            logger.error("[" + CLS + "] adapter \"" + this.name + "\" is down");
            throw new RuntimeException("NetworkAdapter: adapter is down");
        }
        SimpleDLLProtocol framingProtocol = new SimpleDLLProtocol(
            this.macAddress,
            this.getLinkedAdapter().getMacAddress()
        );
        byte[] encapsulated = framingProtocol.encapsulate(frame);
        stack.push(framingProtocol);
        logger.info("[" + CLS + "] adapter \"" + this.name + "\" sent frame ("
            + encapsulated.length + " bytes) to adapter \""
            + this.getLinkedAdapter().getName() + "\"");
        this.getLinkedAdapter().receive(stack, encapsulated);
    }

    /**
     * Receives a raw frame from the linked adapter, checks destination,
     * strips DLL, and forwards up the stack.
     *
     * @param stack protocol pipeline (non‐null)
     * @param frame raw bytes received (non‐empty)
     * @throws IllegalArgumentException if stack or frame is null/empty
     */
    public void receive(ProtocolPipeline stack, byte[] frame) {
        if (stack == null || frame == null || frame.length == 0) {
            logger.error("[" + CLS + "] invalid arguments to receive");
            throw new IllegalArgumentException("NetworkAdapter: invalid arguments");
        }
        if (!this.isUp) {
            logger.debug("[" + CLS + "] adapter \"" + this.name + "\" is down, dropping frame");
            return;
        }
        if (this.owner == null) {
            logger.error("[" + CLS + "] owner node is null");
            throw new RuntimeException("NetworkAdapter: owner node is null");
        }
        Protocol framingProtocol = stack.pop();
        Address destAddr = framingProtocol.extractDestination(frame);
        if (!(destAddr instanceof Mac)) {
            logger.error("[" + CLS + "] expected DLL protocol, got "
                + framingProtocol.getClass().getSimpleName());
            throw new RuntimeException("NetworkAdapter: expected dll protocol");
        }
        Mac destMac = (Mac) destAddr;
        if (!(destMac.equals(this.macAddress) || destMac.equals(Mac.broadcast()))) {
            logger.debug("[" + CLS + "] frame not for this adapter (" 
                + destMac.stringRepresentation() + ")");
            return;
        }
        byte[] next = framingProtocol.decapsulate(frame);
        logger.info("[" + CLS + "] adapter \"" + this.name + "\" received frame, passing up");
        this.owner.receive(stack, next);
    }

    /**
     * Two adapters are equal if they share the same MAC.
     *
     * @param obj other object
     * @return true if same MAC
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof NetworkAdapter)) {
            logger.debug("[" + CLS + "] equals: object not a NetworkAdapter");
            return false;
        }
        NetworkAdapter other = (NetworkAdapter) obj;
        boolean eq = this.macAddress.equals(other.macAddress);
        logger.debug("[" + CLS + "] equals: MAC comparison result=" + eq);
        return eq;
    }

    @Override
    public int hashCode() {
        return this.macAddress.hashCode();
    }
}