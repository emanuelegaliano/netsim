package com.netsim.protocols.SimpleDLL;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.Mac;
import com.netsim.networkstack.IdentityProtocol;

/**
 * Unit tests for SimpleDLLProtocol, now working over IPv4 packets
 * (header + payload), wrapping each packet in a 12-byte MAC header.
 */
public class SimpleDLLProtocolTest {
    private Mac srcMac;
    private Mac dstMac;
    private byte[] ipPayload;
    private SimpleDLLProtocol dllProto;

    @Before
    public void setUp() {
        srcMac    = new Mac("aa:bb:cc:00:11:22");
        dstMac    = new Mac("11:22:33:44:55:66");
        ipPayload = new byte[] { 0x10, 0x20, 0x30 };
        dllProto  = new SimpleDLLProtocol(srcMac, dstMac);
    }

    /**
     * Helper: builds a minimal IPv4 packet (no options) with the given payload.
     */
    private byte[] makeFakeIpPacket(byte[] payload) {
        int headerLen  = 5 * 4; // IHL=5 → 20 bytes
        int totalLen   = headerLen + payload.length;
        byte[] pkt     = new byte[totalLen];
        // Byte 0: version=4 (high nibble), IHL=5 (low nibble)
        pkt[0] = (byte) ((4 << 4) | 5);
        // Bytes 2–3: total length
        pkt[2] = (byte) ((totalLen >> 8) & 0xFF);
        pkt[3] = (byte) ( totalLen       & 0xFF);
        // leave other header fields =0
        // copy payload after header
        System.arraycopy(payload, 0, pkt, headerLen, payload.length);
        return pkt;
    }

    // —— Constructor tests —— //

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullSourceMac() {
        new SimpleDLLProtocol(null, dstMac);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullDestinationMac() {
        new SimpleDLLProtocol(srcMac, null);
    }

    // —— setNext / setPrevious tests —— //


    @Test(expected = IllegalArgumentException.class)
    public void setNextRejectsNull() {
        dllProto.setNext(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPreviousRejectsNull() {
        dllProto.setPrevious(null);
    }

    // —— encapsulate(...) tests —— //

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateRejectsNullInput() {
        dllProto.setNext(new IdentityProtocol());
        dllProto.encapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateRejectsEmptyInput() {
        dllProto.setNext(new IdentityProtocol());
        dllProto.encapsulate(new byte[0]);
    }

    @Test(expected = NullPointerException.class)
    public void encapsulateRequiresNextProtocol() {
        byte[] ipPkt = makeFakeIpPacket(ipPayload);
        dllProto.encapsulate(ipPkt);
    }

    @Test
    public void encapsulateWrapsSingleIpPacket() {
        IdentityProtocol identityNext = new IdentityProtocol();
        dllProto.setNext(identityNext);

        byte[] ipPkt    = makeFakeIpPacket(ipPayload);
        byte[] wire     = dllProto.encapsulate(ipPkt);

        // expect exactly one frame: 12-byte DLL header + ipPkt
        assertEquals(12 + ipPkt.length, wire.length);

        // DLL header: [dstMAC (6)] [srcMAC (6)]
        byte[] gotDst = new byte[6], gotSrc = new byte[6];
        System.arraycopy(wire, 0,      gotDst, 0, 6);
        System.arraycopy(wire, 6,      gotSrc, 0, 6);
        assertArrayEquals(dstMac.byteRepresentation(), gotDst);
        assertArrayEquals(srcMac.byteRepresentation(), gotSrc);

        // then the IP packet unchanged
        byte[] gotIp = new byte[ipPkt.length];
        System.arraycopy(wire, 12, gotIp, 0, ipPkt.length);
        assertArrayEquals(ipPkt, gotIp);
    }

    // —— decapsulate(...) tests —— //

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsNullInput() {
        dllProto.setPrevious(new IdentityProtocol());
        dllProto.decapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsTooShortInput() {
        dllProto.setPrevious(new IdentityProtocol());
        dllProto.decapsulate(new byte[11]);
    }

    @Test(expected = NullPointerException.class)
    public void decapsulateRequiresPreviousProtocol() {
        byte[] frame = new byte[12 + ipPayload.length];
        dllProto.decapsulate(frame);
    }

    @Test
    public void decapsulateUnwrapsSingleFrame() {
        // Build a valid single frame
        byte[] ipPkt = makeFakeIpPacket(ipPayload);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(dstMac.byteRepresentation(), 0, 6);
        out.write(srcMac.byteRepresentation(), 0, 6);
        out.write(ipPkt, 0, ipPkt.length);
        byte[] frame = out.toByteArray();

        IdentityProtocol identityPrev = new IdentityProtocol();
        dllProto.setPrevious(identityPrev);

        byte[] recoveredIp = dllProto.decapsulate(frame);
        assertArrayEquals("should strip DLL header and return original IP packet",
                          ipPkt, recoveredIp);
    }

    // —— Round-trip test —— //

    @Test
    public void roundTripEncapsulateThenDecapsulate() {
        IdentityProtocol identity = new IdentityProtocol();
        dllProto.setNext(identity);
        dllProto.setPrevious(identity);

        byte[] ipPkt     = makeFakeIpPacket(ipPayload);
        byte[] wire      = dllProto.encapsulate(ipPkt);
        byte[] recovered = dllProto.decapsulate(wire);

        assertArrayEquals("round-trip should recover original IP packet",
                          ipPkt, recovered);
    }
}