package com.netsim.protocols.UDP;

import com.netsim.networkstack.PDU;
import com.netsim.addresses.Port;
import com.netsim.utils.Logger;

import java.nio.ByteBuffer;

/**
 * Simplified version of UDP without checksum
 */
public class UDPSegment extends PDU {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = UDPSegment.class.getSimpleName();

    private final short sequenceNumber;
    private final short length;
    private final byte[] payload;

    /**
     * Constructs a new UDPSegment.
     *
     * @param source         the source port (non-null)
     * @param destination    the destination port (non-null)
     * @param sequenceNumber an application-defined sequence number
     * @param payload        the payload data (non-null, non-empty)
     * @throws IllegalArgumentException if payload is null or empty
     */
    public UDPSegment(Port source, Port destination, int sequenceNumber, byte[] payload) {
        super(source, destination);
        logger.info("[" + CLS + "] creating segment seq=" + sequenceNumber +
                    ", src=" + source + ", dst=" + destination +
                    ", payloadLen=" + (payload == null ? "null" : payload.length));

        if (source == null || destination == null) {
            logger.error("[" + CLS + "] source or destination cannot be null");
            throw new IllegalArgumentException("UDPSegment: source or destination cannot be null");
        }
        if (payload == null || payload.length == 0) {
            logger.error("[" + CLS + "] payload must be valid");
            throw new IllegalArgumentException("UDPSegment: payload must be valid");
        }
        if (sequenceNumber > Short.MAX_VALUE) {
            logger.error("[" + CLS + "] sequence number too large: " + sequenceNumber);
            throw new IllegalArgumentException("UDPSegment: sequence number is too large");
        }

        this.sequenceNumber = (short) sequenceNumber;
        this.payload = payload.clone();
        this.length = calculateLength();

        logger.debug("[" + CLS + "] segment length (bits)=" + this.length);
    }

    /**
     * Calculates the total segment length (header + payload) in bits.
     *
     * @return the total length in bits
     */
    private short calculateLength() {
        int headerBytes = getHeader().length;
        int payloadBytes = payload.length;
        int totalBytes = headerBytes + payloadBytes;
        int totalBits = totalBytes * Byte.SIZE; // Byte.SIZE == 8

        if (totalBits > Short.MAX_VALUE) {
            logger.error("[" + CLS + "] segment too large: " + totalBits + " bits");
            throw new IllegalArgumentException(
                "Segment too large to encode length in 16 bits: " + totalBits + " bits"
            );
        }
        return (short) totalBits;
    }

    public short getSequenceNumber() {
        return sequenceNumber;
    }

    public short getLength() {
        return length;
    }

    /**
     * Constructs the raw header bytes for this segment.
     *
     * @return a byte array containing the header fields in network byte order
     */
    public byte[] getHeader() {
        byte[] srcBytes = source.byteRepresentation();
        byte[] dstBytes = destination.byteRepresentation();

        ByteBuffer buf = ByteBuffer.allocate(
            srcBytes.length + dstBytes.length + Short.BYTES + Short.BYTES
        );
        buf.put(srcBytes)
           .put(dstBytes)
           .putShort(sequenceNumber)
           .putShort(length);
        return buf.array();
    }

    /**
     * Serializes this UDPSegment to a byte array.
     *
     * @return the segment as a byte array ready for transmission
     */
    @Override
    public byte[] toByte() {
        byte[] headerBytes = getHeader();
        ByteBuffer buf = ByteBuffer.allocate(headerBytes.length + payload.length);
        buf.put(headerBytes).put(payload);
        logger.debug("[" + CLS + "] toByte(): total bytes=" + buf.capacity());
        return buf.array();
    }

    /**
     * Parses a raw UDPSegment from bytes.
     *
     * @param data header+payload in byte array
     * @return UDP segment
     * @throws IllegalArgumentException if data is null or too short
     */
    public static UDPSegment fromBytes(byte[] data) {
        logger.info("[" + CLS + "] fromBytes(): data length=" + (data == null ? "null" : data.length));
        if (data == null || data.length < 8) {
            logger.error("[" + CLS + "] input null or too short");
            throw new IllegalArgumentException(
                "UDPSegment: input null o troppo corto per contenere un header UDP"
            );
        }
        ByteBuffer buf = ByteBuffer.wrap(data);

        byte[] srcBytes = new byte[Short.BYTES];
        buf.get(srcBytes);
        Port source = Port.fromBytes(srcBytes);

        byte[] dstBytes = new byte[Short.BYTES];
        buf.get(dstBytes);
        Port destination = Port.fromBytes(dstBytes);

        short rawSeq = buf.getShort();
        int seqNum = Short.toUnsignedInt(rawSeq);

        short lengthBits = buf.getShort();

        int headerBytes = 2 + 2 + 2 + 2;
        int payloadBytes = data.length - headerBytes;
        if (payloadBytes < 0) {
            logger.error("[" + CLS + "] inconsistent buffer length");
            throw new IllegalArgumentException(
                "UDPSegment: lunghezza buffer incoerente: header=" 
                + headerBytes + " B, totale=" + data.length + " B"
            );
        }
        byte[] payload = new byte[payloadBytes];
        buf.get(payload);

        int totalBits = (headerBytes + payloadBytes) * Byte.SIZE;
        if (lengthBits != (short) totalBits) {
            logger.error("[" + CLS + "] length field mismatch: " 
                         + lengthBits + " vs " + totalBits);
            throw new IllegalArgumentException(
                "UDPSegment: campo length non corrisponde ai bit effettivi: "
                + lengthBits + " vs " + totalBits
            );
        }

        return new UDPSegment(source, destination, seqNum, payload);
    }

    /**
     * Returns the raw payload carried in this segment.
     *
     * @return a copy of the payload data
     */
    public byte[] getPayload() {
        return payload.clone();
    }
}