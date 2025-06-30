package com.netsim.networkstack;

import java.io.Serializable;

import com.netsim.addresses.Address;

/**
 * Abstract base for protocol data units (PDUs), encapsulating
 * source and destination addresses.
 */
public abstract class PDU implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Source address of this PDU */
    protected Address source;
    /** Destination address of this PDU */
    protected Address destination;

    /**
     * Constructs a PDU with the given source and destination addresses.
     *
     * @param src the source Address (non‐null)
     * @param dst the destination Address (non‐null)
     * @throws IllegalArgumentException if src or dst is null
     */
    protected PDU(Address src, Address dst) throws IllegalArgumentException {
        this.source = src;
        this.destination = dst;
    }

    /**
     * @return the source Address of this PDU
     */
    public Address getSource() {
        return this.source;
    }

    /**
     * @return the destination Address of this PDU
     */
    public Address getDestination() {
        return this.destination;
    }

    /**
     * Builds and returns the protocol header bytes for this PDU.
     *
     * @return the header as a byte array
     */
    public abstract byte[] getHeader();

    /**
     * Serializes the entire PDU (header + payload) into bytes.
     *
     * @return the full PDU as a byte array
     */
    public abstract byte[] toByte();
}