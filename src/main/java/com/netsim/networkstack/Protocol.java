package com.netsim.networkstack;
/**
 * @param UpperLayerPDU the PDU that upper layer of network stack expects
 * @param LowerLayerPDU the PDU that lower layer of network stack expects
 */
public interface Protocol<UpperLayerPDU, LowerLayerPDU> {
    /**
     * @param pdu the pdu parameter of the upper layer 
     * that will be processed 
     * @return the lower layer pdu
     */
    public LowerLayerPDU encapsulate(UpperLayerPDU pdu);

    /**
     * @param pdu the pdu parameter of the lower layer 
     * that will be processed
     * @return the upper layer pdu
     */
    public UpperLayerPDU decapsulate(LowerLayerPDU pdu);
}
