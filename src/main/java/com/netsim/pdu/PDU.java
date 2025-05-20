package com.netsim.packet;

public interface PDU {

    /**
     * Encapsulates an upper-layer packet inside this packet.
     * @param payload the higher-level packet to embed
     */
    void encapsulate(PDU payload);

    /**
     * Decapsulates and returns the upper-layer packet contained in this one.
     * @return the payload, or null if none is present
     */
    PDU decapsulate();

    @Override
    String toString();
}