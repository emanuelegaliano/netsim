package com.netsim.protocols.UDP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.netsim.addresses.Port;
import com.netsim.networkstack.Protocol;
import com.netsim.utils.Logger;

/**
 * Implements UDP segmentation and reassembly over an unreliable byte stream.
 */
public class UDPProtocol implements Protocol {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = UDPProtocol.class.getSimpleName();

    private final int   MSS;
    private final Port  sourcePort;
    private final Port  destinationPort;

    /**
     * Constructs a UDP protocol handler.
     *
     * @param MSS         maximum segment size in bytes (must be > 0)
     * @param source      source port (non-null)
     * @param destination destination port (non-null)
     * @throws IllegalArgumentException if MSS ≤ 0 or any port is null
     */
    public UDPProtocol(int MSS, Port source, Port destination) throws IllegalArgumentException {
        logger.info("[" + CLS + "] constructing with MSS=" + MSS
                    + ", src=" + (source != null ? source : "null")
                    + ", dst=" + (destination != null ? destination : "null"));
        if (MSS <= 0) {
            logger.error("[" + CLS + "] MSS must be positive: " + MSS);
            throw new IllegalArgumentException("UDPProtocol: MSS must be positive");
        }
        if (source == null || destination == null) {
            logger.error("[" + CLS + "] source or destination port is null");
            throw new IllegalArgumentException("UDPProtocol: ports cannot be null");
        }
        this.MSS             = MSS;
        this.sourcePort      = source;
        this.destinationPort = destination;
    }

    /**
     * Segments the upper‐layer payload into UDP segments.
     *
     * @param upperLayerPDU the payload bytes (non-null, non-empty)
     * @return concatenated UDP segment bytes
     * @throws IllegalArgumentException if payload is null or empty
     */
    @Override
    public byte[] encapsulate(byte[] upperLayerPDU) throws IllegalArgumentException {
        logger.debug("[" + CLS + "] encapsulate called, payload length="
                     + (upperLayerPDU == null ? "null" : upperLayerPDU.length));
        if (upperLayerPDU == null || upperLayerPDU.length == 0) {
            logger.error("[" + CLS + "] payload cannot be null or empty");
            throw new IllegalArgumentException("UDPProtocol: payload cannot be null or empty");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int sequenceNumber = 0;

        for (int offset = 0; offset < upperLayerPDU.length; offset += this.MSS) {
            int len = Math.min(this.MSS, upperLayerPDU.length - offset);
            byte[] chunk = new byte[len];
            System.arraycopy(upperLayerPDU, offset, chunk, 0, len);

            UDPSegment segment = new UDPSegment(
                this.sourcePort,
                this.destinationPort,
                sequenceNumber++,
                chunk
            );

            try {
                baos.write(segment.toByte());
                logger.debug("[" + CLS + "] wrote segment seq=" + (sequenceNumber - 1)
                             + ", payloadLen=" + len);
            } catch (IOException e) {
                logger.error("[" + CLS + "] error during segment writing");
                throw new RuntimeException("UDPProtocol: error during segment writing", e);
            }
        }

        byte[] out = baos.toByteArray();
        logger.info("[" + CLS + "] encapsulated total length=" + out.length);
        return out;
    }

    /**
     * Reassembles and returns the concatenated payload from UDP segments.
     *
     * @param lowerLayerPDU the raw UDP segment bytes (non-null, non-empty)
     * @return reassembled payload bytes
     * @throws IllegalArgumentException if input is null, empty, or contains no valid segments
     */
    @Override
    public byte[] decapsulate(byte[] lowerLayerPDU) throws IllegalArgumentException {
        logger.debug("[" + CLS + "] decapsulate called, input length="
                     + (lowerLayerPDU == null ? "null" : lowerLayerPDU.length));
        if (lowerLayerPDU == null || lowerLayerPDU.length == 0) {
            logger.error("[" + CLS + "] received empty data");
            throw new IllegalArgumentException("UDPProtocol: received empty data");
        }

        List<UDPSegment> segments = parseSegments(lowerLayerPDU);
        if (segments.isEmpty()) {
            logger.error("[" + CLS + "] no valid segments found");
            throw new IllegalArgumentException("UDPProtocol: no valid segments found");
        }

        segments.sort(Comparator.comparingInt(UDPSegment::getSequenceNumber));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (UDPSegment seg : segments) {
            try {
                baos.write(seg.getPayload());
                logger.debug("[" + CLS + "] reassembled segment seq="
                             + seg.getSequenceNumber()
                             + ", payloadLen=" + seg.getPayload().length);
            } catch (IOException e) {
                logger.error("[" + CLS + "] error reassembling payload");
                throw new RuntimeException("UDPProtocol: error reassembling payload", e);
            }
        }

        byte[] out = baos.toByteArray();
        logger.info("[" + CLS + "] decapsulated total length=" + out.length);
        return out;
    }

    /**
     * Parses raw bytes into individual UDPSegment objects.
     *
     * @param data the concatenated segment bytes (non-null)
     * @return list of UDPSegment instances (possibly empty)
     * @throws IllegalArgumentException if data is null or malformed
     */
    private List<UDPSegment> parseSegments(byte[] data) throws IllegalArgumentException {
        logger.debug("[" + CLS + "] parseSegments called, data length="
                     + (data == null ? "null" : data.length));
        if (data == null) {
            logger.error("[" + CLS + "] null input to parseSegments");
            throw new IllegalArgumentException("UDPProtocol: null input");
        }

        List<UDPSegment> list = new ArrayList<>();
        ByteBuffer bb = ByteBuffer.wrap(data);
        final int HEADER_LEN = 8;

        while (bb.remaining() >= HEADER_LEN) {
            byte[] header = new byte[HEADER_LEN];
            bb.get(header);

            ByteBuffer hbuf = ByteBuffer.wrap(header);
            hbuf.position(6);
            short lengthBits = hbuf.getShort();
            if (lengthBits < HEADER_LEN * Byte.SIZE || (lengthBits % Byte.SIZE) != 0) {
                logger.error("[" + CLS + "] invalid segment length: " + lengthBits);
                throw new IllegalArgumentException("UDPProtocol: invalid segment length");
            }

            int totalBytes   = lengthBits / Byte.SIZE;
            int payloadBytes = totalBytes - HEADER_LEN;
            if (payloadBytes > bb.remaining()) {
                logger.error("[" + CLS + "] truncated segment payload");
                throw new IllegalArgumentException("UDPProtocol: truncated segment payload");
            }

            byte[] fullSegment = new byte[totalBytes];
            System.arraycopy(header, 0, fullSegment, 0, HEADER_LEN);
            bb.get(fullSegment, HEADER_LEN, payloadBytes);

            UDPSegment seg = UDPSegment.fromBytes(fullSegment);
            list.add(seg);
            logger.debug("[" + CLS + "] parsed segment seq="
                         + seg.getSequenceNumber()
                         + ", totalBytes=" + totalBytes);
        }

        return list;
    }

    /** @return the source port */
    @Override public Port getSource()      { return this.sourcePort; }

    /** @return the destination port */
    @Override public Port getDestination() { return this.destinationPort; }

    /** @return the maximum segment size */
    public int getMSS() { return this.MSS; }

    /**
     * Extracts the source port from a raw UDP segment.
     *
     * @param segment the raw segment bytes (non-null, length ≥4)
     * @return the source Port
     * @throws IllegalArgumentException if segment is null or too short
     */
    @Override
    public Port extractSource(byte[] segment) throws IllegalArgumentException {
        if (segment == null || segment.length < 4) {
            logger.error("[" + CLS + "] segment too short to extractSource");
            throw new IllegalArgumentException("UDPProtocol: segment too short");
        }
        int src = ((segment[0] & 0xFF) << 8) | (segment[1] & 0xFF);
        logger.debug("[" + CLS + "] extractSource port=" + src);
        return new Port(Integer.toString(src));
    }

    /**
     * Extracts the destination port from a raw UDP segment.
     *
     * @param segment the raw segment bytes (non-null, length ≥4)
     * @return the destination Port
     * @throws IllegalArgumentException if segment is null or too short
     */
    @Override
    public Port extractDestination(byte[] segment) throws IllegalArgumentException {
        if (segment == null || segment.length < 4) {
            logger.error("[" + CLS + "] segment too short to extractDestination");
            throw new IllegalArgumentException("UDPProtocol: segment too short");
        }
        int dst = ((segment[2] & 0xFF) << 8) | (segment[3] & 0xFF);
        logger.debug("[" + CLS + "] extractDestination port=" + dst);
        return new Port(Integer.toString(dst));
    }

    /**
     * Creates a copy of this protocol instance.
     *
     * @return a new UDPProtocol with the same MSS and ports
     */
    @Override
    public Protocol copy() {
        logger.debug("[" + CLS + "] copy()");
        return new UDPProtocol(this.MSS, this.sourcePort, this.destinationPort);
    }
}