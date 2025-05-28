package com.netsim.networkstack;

public interface Protocol {
    
    public byte[] encapsulate(byte[] upperLayerPDU);
    public byte[] decapsulate(byte[] lowerLayerPDU);

    public abstract void setNext(Protocol nextProtocol);
    public abstract void setPrevious(Protocol previousProtocol);
}
