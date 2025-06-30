package com.netsim.protocols.IPv4;

import com.netsim.networkstack.Protocol;
import com.netsim.addresses.IPv4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class IPv4Protocol implements Protocol {
    private final IPv4 source;
    private final IPv4 destination;
    private final int version;
    private final int IHL;
    private final int typeOfService;
    private final int identification;
    private final int flags;
    private final int ttl;
    private final int protocol;
    private final int MTU;

    public IPv4Protocol(
            IPv4 source,
            IPv4 destination,
            int IHL,
            int typeOfService,
            int identification,
            int flags,
            int ttl,
            int protocol,
            int MTU
    ) {
        if (source == null || destination == null)
            throw new IllegalArgumentException("IP: source/destination cannot be null");
        if (IHL < 5 || IHL > 15)
            throw new IllegalArgumentException("IP: IHL must be between 5 and 15");
        if (typeOfService < 0 || typeOfService > 255)
            throw new IllegalArgumentException("IP: typeOfService must be between 0 and 255");
        if (identification < 0 || identification > 65535)
            throw new IllegalArgumentException("IP: identification must be between 0 and 65535");
        if (flags < 0 || flags > 7)
            throw new IllegalArgumentException("IP: flags must be between 0 and 7");
        if (ttl < 0 || ttl > 255)
            throw new IllegalArgumentException("IP: TTL must be between 0 and 255");
        if (MTU < IHL * 4)
            throw new IllegalArgumentException("IP: MTU too small");

        this.source = source;
        this.destination = destination;
        this.version = 4;
        this.IHL = IHL;
        this.typeOfService = typeOfService;
        this.identification = identification;
        this.flags = flags;
        this.ttl = ttl;
        this.protocol = protocol;
        this.MTU = MTU;
    }

    public byte[] encapsulate(byte[] upperLayerPDU) {
        if (upperLayerPDU == null || upperLayerPDU.length == 0)
            throw new IllegalArgumentException("IP: upperLayerPDU cannot be null or empty");

        int headerLen = IHL * 4;
        if (MTU < headerLen + 1)
            throw new RuntimeException("IP: MTU too small for header + payload");

        final int maxDataPerFragment = MTU - headerLen;
        final int fragmentUnit = 8;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offsetBytes = 0;

        while (offsetBytes < upperLayerPDU.length) {
            int remaining = upperLayerPDU.length - offsetBytes;
            int thisFragmentDataLen = Math.min(remaining, (maxDataPerFragment / fragmentUnit) * fragmentUnit);
            int thisTotalLen = headerLen + thisFragmentDataLen;
            int mfFlag = ((offsetBytes + thisFragmentDataLen) < upperLayerPDU.length) ? 1 : 0;
            int fragOffset = offsetBytes / fragmentUnit;
            int flagsValue = (mfFlag << 1);

            byte[] fragmentData = Arrays.copyOfRange(upperLayerPDU, offsetBytes, offsetBytes + thisFragmentDataLen);
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
                    ttl,
                    protocol,
                    fragmentData
            );

            byte[] encoded = packet.toByte();
            out.write(encoded, 0, encoded.length);
            offsetBytes += thisFragmentDataLen;
        }

        return out.toByteArray();
    }

    public byte[] decapsulate(byte[] lowerLayerPDU) {
        if (lowerLayerPDU == null || lowerLayerPDU.length == 0)
            throw new IllegalArgumentException("IP: lowerLayerPDU cannot be null or empty");

        final int MIN_IPV4_HEADER = 20;
        ByteArrayInputStream in = new ByteArrayInputStream(lowerLayerPDU);
        List<Fragment> fragments = new ArrayList<>();

        while (in.available() >= MIN_IPV4_HEADER) {
            in.mark(4);
            byte[] first4 = new byte[4];
            in.read(first4, 0, 4);
            in.reset();
            int ihl = first4[0] & 0x0F;
            int headerLen = ihl * 4;

            byte[] header = new byte[headerLen];
            in.read(header, 0, headerLen);

            int totalLen = ((header[2] & 0xFF) << 8) | (header[3] & 0xFF);
            int flagsAndOffset = ((header[6] & 0xFF) << 8) | (header[7] & 0xFF);
            int fragOffsetBytes = (flagsAndOffset & 0x1FFF) * 8;
            int dataLen = totalLen - headerLen;

            byte[] chunk = new byte[dataLen];
            in.read(chunk, 0, dataLen);

            fragments.add(new Fragment(fragOffsetBytes, chunk));
        }

        fragments.sort((a, b) -> Integer.compare(a.offset, b.offset));
        int totalLen = fragments.stream().mapToInt(f -> f.offset + f.data.length).max().orElse(0);
        byte[] reassembled = new byte[totalLen];

        for (Fragment f : fragments) {
            System.arraycopy(f.data, 0, reassembled, f.offset, f.data.length);
        }

        return reassembled;
    }

    public IPv4 extractSource(byte[] packet) {
        if (packet == null || packet.length < 20)
            throw new IllegalArgumentException("IPv4Protocol.extractSource: packet too short");

        byte[] src = Arrays.copyOfRange(packet, 12, 16);
        String dotted = String.format("%d.%d.%d.%d", src[0] & 0xFF, src[1] & 0xFF, src[2] & 0xFF, src[3] & 0xFF);
        return new IPv4(dotted, 0);
    }

    public IPv4 extractDestination(byte[] packet) {
        if (packet == null || packet.length < 20)
            throw new IllegalArgumentException("IPv4Protocol.extractDestination: packet too short");

        byte[] dst = Arrays.copyOfRange(packet, 16, 20);
        String dotted = String.format("%d.%d.%d.%d", dst[0] & 0xFF, dst[1] & 0xFF, dst[2] & 0xFF, dst[3] & 0xFF);
        return new IPv4(dotted, 0);
    }

    public Protocol copy() {
        return new IPv4Protocol(source, destination, IHL, typeOfService, identification, flags, ttl, protocol, MTU);
    }

    private static class Fragment {
        int offset;
        byte[] data;

        Fragment(int offset, byte[] data) {
            this.offset = offset;
            this.data = data;
        }
    }

    public IPv4 getSource() { return source; }
    public IPv4 getDestination() { return destination; }
    public int getTtl() { return ttl; }
    public int getIHL() { return IHL; }
    public int getTypeOfService() { return typeOfService; }
    public int getIdentification() { return identification; }
    public int getFlags() { return flags; }
    public int getProtocol() { return protocol; }
    public int getMTU() { return MTU; }
}