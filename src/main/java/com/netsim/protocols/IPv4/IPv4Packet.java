package com.netsim.protocols.IPv4;

import java.nio.ByteBuffer;

import com.netsim.addresses.IPv4;
import com.netsim.networkstack.PDU;
import com.netsim.utils.Logger;

/**
 * Represents a minimal IPv4 datagram (base header, no options),
 * with 16-bit TTL and Protocol fields, and no checksum.
 */
public class IPv4Packet extends PDU {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = IPv4Packet.class.getSimpleName();

    private final VersionIHL versionAndIHL;
    private final byte       tos;
    private final short      totalLength;
    private final short      identification;
    private final short      flagsAndFragmentOffset;
    private final short      ttl;
    private final short      protocol;
    private final byte[]     payload;

    /**
     * Constructs a new IPv4 packet header + payload.
     *
     * @param source         the IPv4 source address (non-null)
     * @param destination    the IPv4 destination address (non-null)
     * @param version        IP version (must be 4)
     * @param IHL            Internet Header Length in 32-bit words (5…15)
     * @param typeOfService  Type-Of-Service (0…255)
     * @param totalLength    total length (header + payload) in bytes (0…65535)
     * @param identification Identification field (0…65535)
     * @param flags          flags bits (0…7)
     * @param fragmentOffset fragment offset (0…8191)
     * @param ttl            Time-To-Live (0…65535)
     * @param protocol       upper-layer protocol number (0…65535)
     * @param payload        the payload bytes (non-null, non-empty)
     * @throws IllegalArgumentException if any argument is null or out of range
     */
    public IPv4Packet(IPv4 source,
                      IPv4 destination,
                      int version,
                      int IHL,
                      int typeOfService,
                      int totalLength,
                      int identification,
                      int flags,
                      int fragmentOffset,
                      int ttl,
                      int protocol,
                      byte[] payload) throws IllegalArgumentException {
        super(source, destination);

        if (source == null || destination == null) {
            logger.error("[" + CLS + "] source/destination cannot be null");
            throw new IllegalArgumentException("IPv4Packet: source/destination cannot be null");
        }
        this.versionAndIHL = new VersionIHL(version, IHL);

        if (typeOfService < 0 || typeOfService > 0xFF) {
            logger.error("[" + CLS + "] TOS out of range");
            throw new IllegalArgumentException("IPv4Packet: TOS must be 0…255");
        }
        this.tos = (byte) typeOfService;

        if (totalLength < 0 || totalLength > 0xFFFF) {
            logger.error("[" + CLS + "] totalLength out of range");
            throw new IllegalArgumentException("IPv4Packet: totalLength must be 0…65535");
        }
        this.totalLength = (short) totalLength;

        if (identification < 0 || identification > 0xFFFF) {
            logger.error("[" + CLS + "] identification out of range");
            throw new IllegalArgumentException("IPv4Packet: identification must be 0…65535");
        }
        this.identification = (short) identification;

        if (flags < 0 || flags > 0x7) {
            logger.error("[" + CLS + "] flags out of range");
            throw new IllegalArgumentException("IPv4Packet: flags must be 0…7");
        }
        if (fragmentOffset < 0 || fragmentOffset > 0x1FFF) {
            logger.error("[" + CLS + "] fragmentOffset out of range");
            throw new IllegalArgumentException("IPv4Packet: fragmentOffset must be 0…8191");
        }
        this.flagsAndFragmentOffset = (short) (((flags & 0x7) << 13) | (fragmentOffset & 0x1FFF));

        if (ttl < 0 || ttl > 0xFFFF) {
            logger.error("[" + CLS + "] TTL out of range");
            throw new IllegalArgumentException("IPv4Packet: TTL must be 0…65535");
        }
        this.ttl = (short) ttl;

        if (protocol < 0 || protocol > 0xFFFF) {
            logger.error("[" + CLS + "] protocol out of range");
            throw new IllegalArgumentException("IPv4Packet: protocol must be 0…65535");
        }
        this.protocol = (short) protocol;

        if (payload == null || payload.length == 0) {
            logger.error("[" + CLS + "] payload cannot be null or empty");
            throw new IllegalArgumentException("IPv4Packet: payload cannot be null or empty");
        }
        this.payload = payload;

        logger.info("[" + CLS + "] constructed: src=" + this.getSource().stringRepresentation() +
                    " dst=" + this.getDestination().stringRepresentation() +
                    " ttl=" + this.ttl);
    }

    /**
     * Builds the IPv4 header in network byte order (no checksum).
     *
     * @return header bytes of length IHL*4
     */
    @Override
    public byte[] getHeader() {
        logger.debug("[" + CLS + "] getHeader()");
        int headerLen = this.versionAndIHL.getIhl() * 4;
        ByteBuffer buf = ByteBuffer.allocate(headerLen);
        buf.put(this.versionAndIHL.toByte());
        buf.put(this.tos);
        buf.putShort(this.totalLength);
        buf.putShort(this.identification);
        buf.putShort(this.flagsAndFragmentOffset);
        buf.putShort(this.ttl);
        buf.putShort(this.protocol);
        buf.put(this.getSource().byteRepresentation());
        buf.put(this.getDestination().byteRepresentation());
        byte[] header = buf.array();
        logger.debug("[" + CLS + "] header built, length=" + header.length);
        return header;
    }

    /**
     * Serializes entire packet (header + payload) in network byte order.
     *
     * @return full packet bytes
     */
    @Override
    public byte[] toByte() {
        logger.info("[" + CLS + "] toByte()");
        byte[] header = this.getHeader();
        ByteBuffer buf = ByteBuffer.allocate(header.length + this.payload.length);
        buf.put(header).put(this.payload);
        byte[] packet = buf.array();
        logger.info("[" + CLS + "] serialized packet, total length=" + packet.length);
        return packet;
    }
}