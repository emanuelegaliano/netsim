package com.netsim.protocols.UDP;

import com.netsim.networkstack.PDU;
import com.netsim.addresses.Port;
import com.netsim.utils.Logger;

import java.nio.ByteBuffer;

/**
 * Represents a simplified UDP segment without checksum.
 */
public class UDPSegment extends PDU {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = UDPSegment.class.getSimpleName();

    private final short sequenceNumber;
    private final short length;
    private final byte[] payload;

    /**
     * Constructs a new UDPSegment.
     *
     * @param source         the source Port (non-null)
     * @param destination    the destination Port (non-null)
     * @param sequenceNumber application-defined sequence number (0…Short.MAX_VALUE)
     * @param payload        the payload data (non-null, non-empty)
     * @throws IllegalArgumentException if any argument is invalid
     */
    public UDPSegment(Port source, Port destination, int sequenceNumber, byte[] payload) throws IllegalArgumentException {
        super(source, destination);
        logger.info("[" + CLS + "] creating segment seq=" + sequenceNumber
                    + ", src=" + source + ", dst=" + destination
                    + ", payloadLen=" + (payload == null ? "null" : payload.length));
        if (source == null || destination == null) {
            logger.error("[" + CLS + "] source or destination is null");
            throw new IllegalArgumentException("UDPSegment: source and destination must be non-null");
        }
        if (payload == null || payload.length == 0) {
            logger.error("[" + CLS + "] payload is null or empty");
            throw new IllegalArgumentException("UDPSegment: payload must be non-null and non-empty");
        }
        if (sequenceNumber < 0 || sequenceNumber > Short.MAX_VALUE) {
            logger.error("[" + CLS + "] sequenceNumber out of range: " + sequenceNumber);
            throw new IllegalArgumentException("UDPSegment: sequenceNumber must fit in 16 bits");
        }
        this.sequenceNumber = (short) sequenceNumber;
        this.payload        = payload.clone();
        this.length         = calculateLength();
        logger.debug("[" + CLS + "] segment length (bits)=" + this.length);
    }

    /**
     * Calculates the total segment length in bits.
     *
     * @return total length in bits
     * @throws IllegalArgumentException if length exceeds 16-bit max
     */
    private short calculateLength() throws IllegalArgumentException {
        int headerBytes  = getHeader().length;
        int payloadBytes = this.payload.length;
        int totalBytes   = headerBytes + payloadBytes;
        int totalBits    = totalBytes * Byte.SIZE;
        if (totalBits > Short.MAX_VALUE) {
            logger.error("[" + CLS + "] segment too large: " + totalBits + " bits");
            throw new IllegalArgumentException("UDPSegment: segment too large to encode length");
        }
        return (short) totalBits;
    }

    /**
     * @return the sequence number of this segment
     */
    public short getSequenceNumber() {
        return this.sequenceNumber;
    }

    /**
     * @return the length field (in bits) of this segment
     */
    public short getLength() {
        return this.length;
    }

    /**
     * Builds the UDP header: [srcPort(2B)][dstPort(2B)][seqNum(2B)][length(2B)].
     *
     * @return header bytes in network byte order
     */
    @Override
    public byte[] getHeader() {
        byte[] srcBytes = this.source.byteRepresentation();
        byte[] dstBytes = this.destination.byteRepresentation();
        ByteBuffer buf = ByteBuffer.allocate(srcBytes.length + dstBytes.length + Short.BYTES + Short.BYTES);
        buf.put(srcBytes)
           .put(dstBytes)
           .putShort(this.sequenceNumber)
           .putShort(this.length);
        return buf.array();
    }

    /**
     * Serializes the entire UDP segment (header + payload).
     *
     * @return the segment as a byte array
     */
    @Override
    public byte[] toByte() {
        byte[] header = getHeader();
        ByteBuffer buf = ByteBuffer.allocate(header.length + this.payload.length);
        buf.put(header).put(this.payload);
        logger.debug("[" + CLS + "] toByte(): total bytes=" + buf.capacity());
        return buf.array();
    }

    /**
     * Parses a UDPSegment from raw bytes.
     *
     * @param data raw segment bytes (header + payload)
     * @return a new UDPSegment instance
     * @throws IllegalArgumentException if data is null, too short, or inconsistent
     */
    public static UDPSegment fromBytes(byte[] data) throws IllegalArgumentException {
        logger.info("[" + CLS + "] fromBytes(): data length=" + (data == null ? "null" : data.length));
        if (data == null || data.length < 8) {
            logger.error("[" + CLS + "] data is null or too short");
            throw new IllegalArgumentException("UDPSegment: input must be at least 8 bytes");
        }
        ByteBuffer buf = ByteBuffer.wrap(data);

        byte[] srcBytes = new byte[Short.BYTES];
        buf.get(srcBytes);
        Port source = Port.fromBytes(srcBytes);

        byte[] dstBytes = new byte[Short.BYTES];
        buf.get(dstBytes);
        Port destination = Port.fromBytes(dstBytes);

        int seqNum      = Short.toUnsignedInt(buf.getShort());
        short lengthBits = buf.getShort();

        int headerBytes  = 2 + 2 + 2 + 2;
        int payloadBytes = data.length - headerBytes;
        if (payloadBytes < 0) {
            logger.error("[" + CLS + "] inconsistent buffer length");
            throw new IllegalArgumentException("UDPSegment: inconsistent data length");
        }
        byte[] payload = new byte[payloadBytes];
        buf.get(payload);

        int totalBits = (headerBytes + payloadBytes) * Byte.SIZE;
        if (lengthBits != (short) totalBits) {
            logger.error("[" + CLS + "] length field mismatch: " + lengthBits + " vs " + totalBits);
            throw new IllegalArgumentException("UDPSegment: length field does not match payload size");
        }

        return new UDPSegment(source, destination, seqNum, payload);
    }

    /**
     * @return a copy of the payload data
     */
    public byte[] getPayload() {
        return this.payload.clone();
    }
}