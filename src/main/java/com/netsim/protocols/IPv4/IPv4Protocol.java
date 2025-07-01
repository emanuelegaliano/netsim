package com.netsim.protocols.IPv4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.netsim.addresses.IPv4;
import com.netsim.networkstack.Protocol;
import com.netsim.utils.Logger;

/**
 * Implements the IPv4 fragmentation protocol: splits payload into
 * one or more IPv4Packet fragments and reassembles them.
 */
public class IPv4Protocol implements Protocol {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = IPv4Protocol.class.getSimpleName();

    private final IPv4 source;
    private final IPv4 destination;
    private final int  version;
    private final int  IHL;
    private final int  typeOfService;
    private final int  identification;
    private final int  flags;
    private final int  ttl;
    private final int  protocol;
    private final int  MTU;

    /**
     * Constructs a new IPv4Protocol handler.
     *
     * @param source         the source IPv4 address (non-null)
     * @param destination    the destination IPv4 address (non-null)
     * @param IHL            header length in 32-bit words (5–15)
     * @param typeOfService  Type-Of-Service (0–255)
     * @param identification Identification field (0–65535)
     * @param flags          fragmentation flags (0–7)
     * @param ttl            Time-To-Live (0–255)
     * @param protocol       upper-layer protocol number (0–65535)
     * @param MTU            maximum packet size including header
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public IPv4Protocol(IPv4 source,
                        IPv4 destination,
                        int IHL,
                        int typeOfService,
                        int identification,
                        int flags,
                        int ttl,
                        int protocol,
                        int MTU) throws IllegalArgumentException {
        logger.info("[" + CLS + "] instantiating: src="
                    + (source != null ? source.stringRepresentation() : "null")
                    + " dst="
                    + (destination != null ? destination.stringRepresentation() : "null")
                    + " IHL=" + IHL
                    + " TTL=" + ttl
                    + " MTU=" + MTU);

        if (source == null || destination == null) {
            logger.error("[" + CLS + "] source/destination cannot be null");
            throw new IllegalArgumentException("IP: source/destination cannot be null");
        }
        if (IHL < 5 || IHL > 15) {
            throw new IllegalArgumentException("IP: IHL must be between 5 and 15");
        }
        if (typeOfService < 0 || typeOfService > 255) {
            throw new IllegalArgumentException("IP: typeOfService must be between 0 and 255");
        }
        if (identification < 0 || identification > 0xFFFF) {
            throw new IllegalArgumentException("IP: identification must be between 0 and 65535");
        }
        if (flags < 0 || flags > 7) {
            throw new IllegalArgumentException("IP: flags must be between 0 and 7");
        }
        if (ttl < 0 || ttl > 0xFF) {
            throw new IllegalArgumentException("IP: TTL must be between 0 and 255");
        }
        if (MTU < IHL * 4) {
            throw new IllegalArgumentException("IP: MTU too small");
        }

        this.source        = source;
        this.destination   = destination;
        this.version       = 4;
        this.IHL           = IHL;
        this.typeOfService = typeOfService;
        this.identification= identification;
        this.flags         = flags;
        this.ttl           = ttl;
        this.protocol      = protocol;
        this.MTU           = MTU;
    }

    /**
     * Fragments and encapsulates the given payload into IPv4 packets.
     *
     * @param upperLayerPDU the payload bytes (non-null, non-empty)
     * @return concatenated byte array of all fragments
     * @throws IllegalArgumentException if payload is null or empty
     * @throws RuntimeException         if MTU too small for header
     */
    @Override
    public byte[] encapsulate(byte[] upperLayerPDU)
            throws IllegalArgumentException, RuntimeException {
        logger.info("[" + CLS + "] encapsulate called, data length="
                    + (upperLayerPDU != null ? upperLayerPDU.length : 0));

        if (upperLayerPDU == null || upperLayerPDU.length == 0) {
            throw new IllegalArgumentException("IP: upperLayerPDU cannot be null or empty");
        }

        int headerLen = this.IHL * 4;
        if (this.MTU < headerLen + 1) {
            throw new RuntimeException("IP: MTU too small for header + payload");
        }

        final int maxDataPerFragment = this.MTU - headerLen;
        final int fragmentUnit       = 8;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offsetBytes = 0;

        while (offsetBytes < upperLayerPDU.length) {
            int remaining          = upperLayerPDU.length - offsetBytes;
            int thisFragDataLen    = Math.min(remaining,
                                              (maxDataPerFragment / fragmentUnit) * fragmentUnit);
            int thisTotalLen       = headerLen + thisFragDataLen;
            int mfFlag             = ((offsetBytes + thisFragDataLen) < upperLayerPDU.length) ? 1 : 0;
            int fragOffset         = offsetBytes / fragmentUnit;
            int flagsValue         = (mfFlag << 1);

            byte[] fragmentData = Arrays.copyOfRange(
                upperLayerPDU, offsetBytes, offsetBytes + thisFragDataLen);
            IPv4Packet packet = new IPv4Packet(
                this.source,
                this.destination,
                this.version,
                this.IHL,
                this.typeOfService,
                thisTotalLen,
                this.identification,
                flagsValue,
                fragOffset,
                this.ttl,
                this.protocol,
                fragmentData
            );

            byte[] encoded = packet.toByte();
            out.write(encoded, 0, encoded.length);
            offsetBytes += thisFragDataLen;
        }

        byte[] result = out.toByteArray();
        logger.info("[" + CLS + "] encapsulate produced " + result.length + " bytes");
        return result;
    }

    /**
     * Reassembles IPv4 fragments from the given byte stream.
     *
     * @param lowerLayerPDU concatenated fragment bytes (non-null, non-empty)
     * @return reassembled payload bytes
     * @throws IllegalArgumentException if input is null or empty
     */
    @Override
    public byte[] decapsulate(byte[] lowerLayerPDU) throws IllegalArgumentException {
        logger.info("[" + CLS + "] decapsulate called, data length="
                    + (lowerLayerPDU != null ? lowerLayerPDU.length : 0));

        if (lowerLayerPDU == null || lowerLayerPDU.length == 0) {
            throw new IllegalArgumentException("IP: lowerLayerPDU cannot be null or empty");
        }

        final int MIN_IPV4_HEADER = 20;
        ByteArrayInputStream in = new ByteArrayInputStream(lowerLayerPDU);
        List<Fragment> fragments = new ArrayList<>();

        while (in.available() >= MIN_IPV4_HEADER) {
            in.mark(4);
            byte[] first4 = new byte[4];
            in.read(first4, 0, 4);
            in.reset();

            int ihl       = first4[0] & 0x0F;
            int headerLen = ihl * 4;

            byte[] header = new byte[headerLen];
            in.read(header, 0, headerLen);

            int totalLen         = ((header[2] & 0xFF) << 8) | (header[3] & 0xFF);
            int flagsAndOffset   = ((header[6] & 0xFF) << 8) | (header[7] & 0xFF);
            int fragOffsetBytes  = (flagsAndOffset & 0x1FFF) * 8;
            int dataLen          = totalLen - headerLen;

            byte[] chunk = new byte[dataLen];
            in.read(chunk, 0, dataLen);

            fragments.add(new Fragment(fragOffsetBytes, chunk));
        }

        fragments.sort((a, b) -> Integer.compare(a.offset, b.offset));
        int totalLen = fragments.stream()
                                .mapToInt(f -> f.offset + f.data.length)
                                .max()
                                .orElse(0);
        byte[] reassembled = new byte[totalLen];

        for (Fragment f : fragments) {
            System.arraycopy(f.data, 0, reassembled, f.offset, f.data.length);
        }

        logger.info("[" + CLS + "] decapsulate reassembled to " + reassembled.length + " bytes");
        return reassembled;
    }

    /**
     * Extracts the destination IPv4 address from a packet.
     *
     * @param packet the full IPv4 packet bytes
     * @return the configured destination address
     * @throws IllegalArgumentException if packet is too short
     */
    @Override
    public IPv4 extractDestination(byte[] packet) throws IllegalArgumentException {
        logger.debug("[" + CLS + "] extractDestination()");
        if (packet == null || packet.length < this.IHL * 4) {
            throw new IllegalArgumentException("IPv4Protocol.extractDestination: packet too short");
        }
        logger.debug("[" + CLS + "] destination=" + this.destination.stringRepresentation());
        return this.destination;
    }

    /**
     * Extracts the source IPv4 address from a packet.
     *
     * @param packet the full IPv4 packet bytes
     * @return the configured source address
     * @throws IllegalArgumentException if packet is too short
     */
    @Override
    public IPv4 extractSource(byte[] packet) throws IllegalArgumentException {
        logger.debug("[" + CLS + "] extractSource()");
        if (packet == null || packet.length < this.IHL * 4) {
            throw new IllegalArgumentException("IPv4Protocol.extractSource: packet too short");
        }
        logger.debug("[" + CLS + "] source=" + this.source.stringRepresentation());
        return this.source;
    }

    /**
     * Creates a copy of this protocol with identical settings.
     *
     * @return a new IPv4Protocol instance
     */
    @Override
    public Protocol copy() {
        logger.debug("[" + CLS + "] copy()");
        return new IPv4Protocol(
            this.source,
            this.destination,
            this.IHL,
            this.typeOfService,
            this.identification,
            this.flags,
            this.ttl,
            this.protocol,
            this.MTU
        );
    }

    // Internal holder for fragment data
    private static class Fragment {
        final int    offset;
        final byte[] data;

        Fragment(int offset, byte[] data) {
            this.offset = offset;
            this.data   = data;
        }
    }

    public IPv4 getSource()      { return this.source; }
    public IPv4 getDestination() { return this.destination; }
    public int  getTtl()         { return this.ttl; }
    public int  getIHL()         { return this.IHL; }
    public int  getTypeOfService()  { return this.typeOfService; }
    public int  getIdentification() { return this.identification; }
    public int  getFlags()          { return this.flags; }
    public int  getProtocol()       { return this.protocol; }
    public int  getMTU()            { return this.MTU; }
}