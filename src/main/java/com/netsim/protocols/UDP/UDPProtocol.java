package com.netsim.protocols.UDP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.netsim.addresses.Port;
import com.netsim.networkstack.Protocol;

public class UDPProtocol implements Protocol {
    private final Port sourcePort;
    private final Port destinationPort;
    private final int MSS; // Max Segment Size

    public UDPProtocol(int MSS, Port source, Port destination) {
        if (MSS <= 0)
            throw new IllegalArgumentException("UDP: MSS must be positive");
        if (source == null || destination == null)
            throw new IllegalArgumentException("UDP: ports cannot be null");

        this.MSS = MSS;
        this.sourcePort = source;
        this.destinationPort = destination;
    }

    @Override
    public byte[] encapsulate(byte[] upperLayerPDU) {
        if (upperLayerPDU == null || upperLayerPDU.length == 0)
            throw new IllegalArgumentException("UDP: empty payload");

        int sequenceNumber = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

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
            } catch (IOException e) {
                throw new RuntimeException("UDP: Error during segment writing", e);
            }
        }

        return baos.toByteArray();
    }

    @Override
    public byte[] decapsulate(byte[] lowerLayerPDU) {
        if (lowerLayerPDU == null || lowerLayerPDU.length == 0)
            throw new IllegalArgumentException("UDP: received empty data");

        List<UDPSegment> segments = parseSegments(lowerLayerPDU);
        if (segments.isEmpty())
            throw new IllegalArgumentException("UDP: no valid segments found in input");

        segments.sort(Comparator.comparingInt(UDPSegment::getSequenceNumber));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (UDPSegment seg : segments) {
            try {
                baos.write(seg.getPayload());
            } catch (IOException e) {
                throw new RuntimeException("UDP: error reassembling payload", e);
            }
        }

        return baos.toByteArray();
    }


    private List<UDPSegment> parseSegments(byte[] data) {
        if (data == null)
            throw new IllegalArgumentException("UDP: null input");

        List<UDPSegment> list = new ArrayList<>();
        ByteBuffer bb = ByteBuffer.wrap(data);
        final int HEADER_LEN = 8;

        while (bb.remaining() >= HEADER_LEN) {
            byte[] header = new byte[HEADER_LEN];
            bb.get(header);

            ByteBuffer hbuf = ByteBuffer.wrap(header);
            hbuf.position(6);
            short lengthBits = hbuf.getShort();
            if (lengthBits < HEADER_LEN * Byte.SIZE || lengthBits % Byte.SIZE != 0)
                throw new IllegalArgumentException("UDP: invalid segment length");

            int totalBytes = lengthBits / Byte.SIZE;
            int payloadBytes = totalBytes - HEADER_LEN;

            if (payloadBytes > bb.remaining())
                throw new IllegalArgumentException("UDP: truncated segment payload");

            byte[] payload = new byte[payloadBytes];
            bb.get(payload);

            byte[] fullSegment = new byte[totalBytes];
            System.arraycopy(header, 0, fullSegment, 0, HEADER_LEN);
            System.arraycopy(payload, 0, fullSegment, HEADER_LEN, payloadBytes);

            list.add(UDPSegment.fromBytes(fullSegment));
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
        if (segment == null || segment.length < 4)
            throw new IllegalArgumentException("UDP: segment too short");
        int src = ((segment[0] & 0xFF) << 8) | (segment[1] & 0xFF);
        return new Port(String.valueOf(src));
    }

    @Override
    public Port extractDestination(byte[] segment) {
        if (segment == null || segment.length < 4)
            throw new IllegalArgumentException("UDP: segment too short");
        int dst = ((segment[2] & 0xFF) << 8) | (segment[3] & 0xFF);
        return new Port(String.valueOf(dst));
    }

    @Override
    public Protocol copy() {
        return new UDPProtocol(this.MSS, this.sourcePort, this.destinationPort);
    }
}