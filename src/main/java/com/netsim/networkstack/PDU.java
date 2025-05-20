package com.netsim.networkstack;

import com.netsim.addresses.Address;

/**
 * The class used in protocols for encapsulation/decapsulation
 */
public class PDU {
    protected Address source;
    protected Address destination;
    protected Payload payload;

    /**
     * @param src the source address
     * @param dst the destination address
     * @param payload of the message
     */
    protected PDU(Address src, Address dst, Payload payload) {
        this.source = src;
        this.destination = dst;
        this.payload = payload;
    }

    /**
     * @return the source address
     */
    public Address getSource() {
        return this.source;
    }

    /**
     * @return the destination address
     */
    public Address getDestination() {
        return this.destination;
    }

    /**
     * @return the payload to encapsulate/decapsulate
     */
    public Payload getPayload() {
        return this.payload;
    }
}