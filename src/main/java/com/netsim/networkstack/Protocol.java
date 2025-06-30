package com.netsim.networkstack;

import com.netsim.addresses.Address;

/**
 * Protocol interface for chain of responsibility
 * network stack
 */
public interface Protocol {
    /**
     * 
     * @param upperLayerPDU the byte sequence of the incapsulated message
     * from upper layer 
     * @return the byte sequence of the incapsulated message
     * using this protocol
     */
    public byte[] encapsulate(byte[] upperLayerPDU);
    /**
     * 
     * @param lowerLayerPDUthe the byte sequence of the decapsulated message
     * from lower layer 
     * @return the byte sequence of the decapsulated message
     * using this protocol
     */
    public byte[] decapsulate(byte[] lowerLayerPDU);

    /** @return address of source */
    public Address getSource();

    /** @return address of destination */
    public Address getDestination();

    /**
     * Using non-static method because in java it's not possible 
     * to define a static method in interfaces without its implementation
     * return nulls if a protocol does not have source
     * @param pdu the payload of the protocol
     * @return the source address
     */
    public Address extractSource(byte[] pdu);

    /**
     * Using non-static method because in java it's not possible 
     * to define a static method in interfaces without its implementation.
     * return nulls if a protocol does not have destination
     * @param pdu the payload of the protocol
     * @return the destination address
     */
    public Address extractDestination(byte[] pdu);

    /**
     * Used for extract a protocol without touching
     * chain of responsibility
     * @return
     */
    public Protocol copy();
}
