package com.netsim.networkstack;

import java.nio.ByteBuffer;

import com.netsim.addresses.Port;

public abstract class TransportPDU extends PDU {
    private final int sequenceNumber;
    private final ApplicationPDU payload;

    protected TransportPDU(Port src, Port dst, ApplicationPDU payload, int sequenceNumber) {
        super(src, dst);
        this.payload = payload;
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * @return the application payload
     */
    public ApplicationPDU getPayload() {
        return this.payload;
    }
    /**
     * @return the sequence number of the Transport PDUs list
     */
    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    /**
     * Serializes this TransportPDU into a byte array ready for transmission.
     * @return a byte[] representing this PDU
     */
    public byte[] toByte() {
        byte[] srcBytes = getSource().byteRepresentation();
        byte[] dstBytes = getDestination().byteRepresentation();

        byte[] payloadBytes = payload.toByte();

        int totalLen = srcBytes.length + dstBytes.length + Integer.BYTES + payloadBytes.length;
        ByteBuffer buf = ByteBuffer.allocate(totalLen);

        buf.put(srcBytes);
        buf.put(dstBytes);
        buf.putInt(sequenceNumber);
        buf.put(payloadBytes);

        return buf.array();
    }
}