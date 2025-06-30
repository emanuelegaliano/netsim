package com.netsim.networkstack;

import com.netsim.addresses.Address;

/**
 * Defines the contract for a protocol in the network stack.
 */
public interface Protocol {

    /**
     * Encapsulates the given upper‐layer PDU bytes into this protocol’s format.
     *
     * @param upperLayerPDU the byte sequence from the upper layer (non‐null, non‐empty)
     * @return the encapsulated byte sequence
     * @throws IllegalArgumentException if upperLayerPDU is null or empty
     */
    byte[] encapsulate(byte[] upperLayerPDU) throws IllegalArgumentException;

    /**
     * Decapsulates the given lower‐layer PDU bytes from this protocol’s format.
     *
     * @param lowerLayerPDU the byte sequence from the lower layer (non‐null, non‐empty)
     * @return the decapsulated byte sequence
     * @throws IllegalArgumentException if lowerLayerPDU is null or empty
     */
    byte[] decapsulate(byte[] lowerLayerPDU) throws IllegalArgumentException;

    /**
     * @return this protocol’s source Address, or null if not applicable
     */
    Address getSource();

    /**
     * @return this protocol’s destination Address, or null if not applicable
     */
    Address getDestination();

    /**
     * Extracts the source Address from the given PDU bytes.
     *
     * @param pdu the protocol data unit bytes (non‐null, non‐empty)
     * @return the extracted source Address, or null if not present
     * @throws IllegalArgumentException if pdu is null or empty
     */
    Address extractSource(byte[] pdu) throws IllegalArgumentException;

    /**
     * Extracts the destination Address from the given PDU bytes.
     *
     * @param pdu the protocol data unit bytes (non‐null, non‐empty)
     * @return the extracted destination Address, or null if not present
     * @throws IllegalArgumentException if pdu is null or empty
     */
    Address extractDestination(byte[] pdu) throws IllegalArgumentException;

    /**
     * Creates and returns a copy of this Protocol instance,
     * preserving any configuration but not shared mutable state.
     *
     * @return a fresh copy of this Protocol
     */
    Protocol copy();
}