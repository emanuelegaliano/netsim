package com.netsim.networkstack;

import java.io.Serializable;

import com.netsim.addresses.Address;

public abstract class PDU implements Serializable {
    protected static final long serialVersionUID = 1L;

    protected Address source;
    protected Address destination;

    /**
     * @param src the source address
     * @param dst the destination address
     */
    protected PDU(Address src, Address dst) {
        this.source = src;
        this.destination = dst;
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
     * This abstract method defines the header of the protocol payload
     * @return the hader in raw byte array
     */
    public abstract byte[] getHeader();

    /**
     * This abstract method compute the whole protocol
     * payload
     * @return the whole payload in a raw byte array
     */
    public abstract byte[] toByte();
}