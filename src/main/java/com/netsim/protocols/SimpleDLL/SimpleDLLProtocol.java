package com.netsim.protocols.SimpleDLL;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import com.netsim.addresses.Mac;
import com.netsim.networkstack.Protocol;

/**
 * Simple Data Link Layer Protocol - encapsulates raw IP packets into Ethernet-like frames.
 */
public class SimpleDLLProtocol implements Protocol {
    private final Mac source;
    private final Mac destination;

    /**
     * @param source source MAC address
     * @param destination destination MAC address
     * @throws IllegalArgumentException if either argument is null
     */
    public SimpleDLLProtocol(Mac source, Mac destination) {
        if (source == null || destination == null)
            throw new IllegalArgumentException("SimpleDLLProtocol: MAC addresses cannot be null");

        this.source = source;
        this.destination = destination;
    }

    @Override
    public byte[] encapsulate(byte[] ipPackets) {
        if (ipPackets == null || ipPackets.length == 0)
            throw new IllegalArgumentException("SimpleDLLProtocol: packets required");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;

        while (offset < ipPackets.length) {
            if (offset + 4 > ipPackets.length)
                throw new IllegalArgumentException("SimpleDLLProtocol: truncated IP packet");

            int ihl = ipPackets[offset] & 0x0F;
            int headerBytes = ihl * 4;

            if (ihl < 5 || offset + headerBytes > ipPackets.length)
                throw new IllegalArgumentException("SimpleDLLProtocol: invalid IHL or incomplete header");

            int totalLen = ((ipPackets[offset + 2] & 0xFF) << 8)
                         |  (ipPackets[offset + 3] & 0xFF);

            if (totalLen < headerBytes || offset + totalLen > ipPackets.length)
                throw new IllegalArgumentException("SimpleDLLProtocol: invalid totalLen");

            byte[] ipPkt = Arrays.copyOfRange(ipPackets, offset, offset + totalLen);
            SimpleDLLFrame frame = new SimpleDLLFrame(this.source, this.destination, ipPkt);
            byte[] frameBytes = frame.toByte();

            out.write(frameBytes, 0, frameBytes.length);
            offset += totalLen;
        }

        return out.toByteArray();
    }

    @Override
    public byte[] decapsulate(byte[] frames) {
        if (frames == null || frames.length < 12)
            throw new IllegalArgumentException("SimpleDLLProtocol: frames too short");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;

        while (offset + 12 <= frames.length) {
            int ipOffset = offset + 12;

            if (ipOffset + 4 > frames.length)
                throw new IllegalArgumentException("SimpleDLLProtocol: truncated IP header");

            int ihl = frames[ipOffset] & 0x0F;
            int headerBytes = ihl * 4;

            if (ihl < 5 || ipOffset + headerBytes > frames.length)
                throw new IllegalArgumentException("SimpleDLLProtocol: invalid IP header");

            int totalLen = ((frames[ipOffset + 2] & 0xFF) << 8)
                         |  (frames[ipOffset + 3] & 0xFF);

            if (totalLen < headerBytes || ipOffset + totalLen > frames.length)
                throw new IllegalArgumentException("SimpleDLLProtocol: invalid totalLen");

            out.write(frames, ipOffset, totalLen);
            offset += 12 + totalLen;
        }

        return out.toByteArray();
    }

    @Override
    public Mac getSource() {
        return this.source;
    }

    @Override
    public Mac getDestination() {
        return this.destination;
    }

    @Override
    public Mac extractSource(byte[] frame) {
        if (frame == null || frame.length < 12)
            throw new IllegalArgumentException("SimpleDLLProtocol: frame too short");
        return Mac.bytesToMac(Arrays.copyOfRange(frame, 6, 12));
    }

    @Override
    public Mac extractDestination(byte[] frame) {
        if (frame == null || frame.length < 6)
            throw new IllegalArgumentException("SimpleDLLProtocol: frame too short");
        return Mac.bytesToMac(Arrays.copyOfRange(frame, 0, 6));
    }

    @Override
    public Protocol copy() {
        return new SimpleDLLProtocol(this.source, this.destination);
    }
}