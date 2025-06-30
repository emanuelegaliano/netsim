package com.netsim.network;

import com.netsim.addresses.IPv4;
import com.netsim.networkstack.ProtocolPipeline;

/**
 * Represents a network node capable of sending and receiving IP‐based packets.
 */
public interface Node {
    /**
     * Sends data to the given IPv4 destination using the specified protocol pipeline.
     *
     * @param destination the IPv4 address to send to (non‐null)
     * @param protocols   the protocol pipeline to apply (non‐null)
     * @param data        the payload bytes to send (non‐null, non‐empty)
     * @throws IllegalArgumentException if any argument is null or data is empty
     */
    void send(IPv4 destination, ProtocolPipeline protocols, byte[] data) throws IllegalArgumentException;

    /**
     * Receives a block of raw bytes from the network and processes it through the
     * provided protocol pipeline, delivering the result to an upper layer handler.
     *
     * @param protocols the protocol pipeline to apply (non‐null)
     * @param data      the raw bytes received (non‐null, non‐empty)
     * @throws IllegalArgumentException if any argument is null or data is empty
     */
    void receive(ProtocolPipeline protocols, byte[] data) throws IllegalArgumentException;

    /**
     * @return the name of this node
     */
    String getName();
}
