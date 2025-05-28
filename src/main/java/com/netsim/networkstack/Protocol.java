package com.netsim.networkstack;

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

    /**
     * Method to set next protocol in the chain
     * @param nextProtocol the next protocol
     */
    public abstract void setNext(Protocol nextProtocol);
    /**
     * Method to set previous protocol in the chain
     * @param previousProtocol the previous protocol
     */
    public abstract void setPrevious(Protocol previousProtocol);
}
