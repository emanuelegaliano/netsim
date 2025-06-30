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

public class UDPProtocol implements Protocol {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = UDPProtocol.class.getSimpleName();

    private final Port sourcePort;
    private final Port destinationPort;
    private final int MSS; // Max Segment Size

    public UDPProtocol(int MSS, Port source, Port destination) {
        logger.info("[" + CLS + "] constructing with MSS=" + MSS
                    + ", src=" + (source != null ? source : "null")
                    + ", dst=" + (destination != null ? destination : "null"));
        if (MSS <= 0) {
            logger.error("[" + CLS + "] MSS must be positive: " + MSS);
            throw new IllegalArgumentException("UDP: MSS must be positive");
        }
        if (source == null || destination == null) {
            logger.error("[" + CLS + "] ports cannot be null");
            throw new IllegalArgumentException("UDP: ports cannot be null");
        }
        this.MSS = MSS;
        this.sourcePort = source;
        this.destinationPort = destination;
    }

    @Override
    public byte[] encapsulate(byte[] upperLayerPDU) {
        logger.debug("[" + CLS + "] encapsulate called, payload length="
                     + (upperLayerPDU == null ? "null" : upperLayerPDU.length));
        if (upperLayerPDU == null || upperLayerPDU.length == 0) {
            logger.error("[" + CLS + "] empty payload");
            throw new IllegalArgumentException("UDP: empty payload");
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
                             + ", length=" + len);
            } catch (IOException e) {
                logger.error("[" + CLS + "] error during segment writing");
                throw new RuntimeException("UDP: Error during segment writing", e);
            }
        }

        byte[] out = baos.toByteArray();
        logger.info("[" + CLS + "] encapsulated total length=" + out.length);
        return out;
    }

    @Override
    public byte[] decapsulate(byte[] lowerLayerPDU) {
        logger.debug("[" + CLS + "] decapsulate called, input length="
                     + (lowerLayerPDU == null ? "null" : lowerLayerPDU.length));
        if (lowerLayerPDU == null || lowerLayerPDU.length == 0) {
            logger.error("[" + CLS + "] received empty data");
            throw new IllegalArgumentException("UDP: received empty data");
        }

        List<UDPSegment> segments = parseSegments(lowerLayerPDU);
        if (segments.isEmpty()) {
            logger.error("[" + CLS + "] no valid segments found");
            throw new IllegalArgumentException("UDP: no valid segments found in input");
        }

        segments.sort(Comparator.comparingInt(UDPSegment::getSequenceNumber));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (UDPSegment seg : segments) {
            try {
                baos.write(seg.getPayload());
                logger.debug("[" + CLS + "] reassembled segment seq="
                             + seg.getSequenceNumber()
                             + ", payload len=" + seg.getPayload().length);
            } catch (IOException e) {
                logger.error("[" + CLS + "] error reassembling payload");
                throw new RuntimeException("UDP: error reassembling payload", e);
            }
        }

        byte[] out = baos.toByteArray();
        logger.info("[" + CLS + "] decapsulated total length=" + out.length);
        return out;
    }

    private List<UDPSegment> parseSegments(byte[] data) {
        logger.debug("[" + CLS + "] parseSegments called, data length="
                     + (data == null ? "null" : data.length));
        if (data == null) {
            logger.error("[" + CLS + "] null input to parseSegments");
            throw new IllegalArgumentException("UDP: null input");
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
            if (lengthBits < HEADER_LEN * Byte.SIZE || lengthBits % Byte.SIZE != 0) {
                logger.error("[" + CLS + "] invalid segment length: " + lengthBits);
                throw new IllegalArgumentException("UDP: invalid segment length");
            }

            int totalBytes = lengthBits / Byte.SIZE;
            int payloadBytes = totalBytes - HEADER_LEN;
            if (payloadBytes > bb.remaining()) {
                logger.error("[" + CLS + "] truncated segment payload");
                throw new IllegalArgumentException("UDP: truncated segment payload");
            }

            byte[] payload = new byte[payloadBytes];
            bb.get(payload);

            byte[] fullSegment = new byte[totalBytes];
            System.arraycopy(header, 0, fullSegment, 0, HEADER_LEN);
            System.arraycopy(payload, 0, fullSegment, HEADER_LEN, payloadBytes);

            UDPSegment seg = UDPSegment.fromBytes(fullSegment);
            list.add(seg);
            logger.debug("[" + CLS + "] parsed segment seq="
                         + seg.getSequenceNumber() + ", totalBytes=" + totalBytes);
        }

        return list;
    }

    @Override
    public Port getSource() {
        return this.sourcePort;
    }

    @Override
    public Port getDestination() {
        return this.destinationPort;
    }

    public int getMSS() {
        return this.MSS;
    }

    @Override
    public Port extractSource(byte[] segment) {
        if (segment == null || segment.length < 4) {
            logger.error("[" + CLS + "] segment too short to extractSource");
            throw new IllegalArgumentException("UDP: segment too short");
        }
        int src = ((segment[0] & 0xFF) << 8) | (segment[1] & 0xFF);
        logger.debug("[" + CLS + "] extractSource port=" + src);
        return new Port(String.valueOf(src));
    }

    @Override
    public Port extractDestination(byte[] segment) {
        if (segment == null || segment.length < 4) {
            logger.error("[" + CLS + "] segment too short to extractDestination");
            throw new IllegalArgumentException("UDP: segment too short");
        }
        int dst = ((segment[2] & 0xFF) << 8) | (segment[3] & 0xFF);
        logger.debug("[" + CLS + "] extractDestination port=" + dst);
        return new Port(String.valueOf(dst));
    }

    @Override
    public Protocol copy() {
        logger.info("[" + CLS + "] copying UDPProtocol instance");
        return new UDPProtocol(this.MSS, this.sourcePort, this.destinationPort);
    }
}