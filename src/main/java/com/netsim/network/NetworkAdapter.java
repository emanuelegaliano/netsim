package com.netsim.network;

import com.netsim.addresses.Mac;
import com.netsim.networkstack.ProtocolPipeline;

/**
 * Represents a data‐link layer network adapter that can send and receive raw frames.
 * Implementations handle framing and delivery to a connected remote adapter.
 */
public interface NetworkAdapter {

    /**
     * Assigns the owning network node for this adapter.
     *
     * @param owner the Node that will own this adapter (non‐null)
     * @throws IllegalArgumentException if {@code owner} is null
     */
    void setOwner(Node owner);

    /**
     * Returns the current owning network node.
     *
     * @return the Node that owns this adapter
     * @throws IllegalStateException if no owner has been set
     */
    Node getOwner();

    /**
     * Connects this adapter to a remote adapter, forming a point‐to‐point link.
     *
     * @param remote the remote LinkLayerAdapter to connect to (non‐null)
     * @throws IllegalArgumentException if {@code remote} is null
     */
    void setRemoteAdapter(NetworkAdapter remote);

    /**
     * Retrieves the adapter at the other end of this link.
     *
     * @return the connected remote LinkLayerAdapter
     * @throws IllegalStateException if this adapter is not linked
     */
    NetworkAdapter getLinkedAdapter();

    /**
     * Returns the unique identifier of this adapter.
     *
     * @return the adapter's name or identifier
     */
    String getName();

    /**
     * Returns the maximum transmission unit (MTU) for this adapter.
     *
     * @return the MTU in bytes
     */
    int getMTU();

    /**
     * Returns the hardware MAC address assigned to this adapter.
     *
     * @return the adapter's MAC address
     */
    Mac getMacAddress();

    /**
     * Indicates whether this adapter is currently operational.
     *
     * @return {@code true} if the adapter is up, {@code false} if down
     */
    boolean isUp();

    /**
     * Marks this adapter as operational (up).
     */
    void setUp();

    /**
     * Marks this adapter as non‐operational (down).
     */
    void setDown();

    /**
     * Sends a raw frame through this link‐layer adapter.
     * <p>
     * The implementation should perform any necessary framing
     * and then deliver the frame to the connected remote adapter.
     * </p>
     *
     * @param stack the protocol pipeline to use for additional encapsulation (non‐null)
     * @param frame the payload bytes to transmit (non‐empty)
     * @throws IllegalArgumentException if {@code stack} is null or {@code frame} is null/empty
     */
    void send(ProtocolPipeline stack, byte[] frame);

    /**
     * Receives a raw frame from this link‐layer adapter.
     * <p>
     * The implementation should strip any framing and then
     * forward the payload up the protocol stack via the owner node.
     * </p>
     *
     * @param stack the protocol pipeline used for decapsulation (non‐null)
     * @param frame the raw frame bytes received (non‐empty)
     * @throws IllegalArgumentException if {@code stack} is null or {@code frame} is null/empty
     */
    void receive(ProtocolPipeline stack, byte[] frame);
}
