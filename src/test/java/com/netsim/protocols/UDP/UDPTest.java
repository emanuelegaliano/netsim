package com.netsim.protocols.UDP;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.Port;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.IdentityProtocol;

public class UDPTest {
    private static final Port SRC = new Port("1000");
    private static final Port DST = new Port("2000");
    private byte[] payload;
    private UDPProtocol udp;

    @Before
    public void setUp() {
        payload = "HELLOWORLD".getBytes(StandardCharsets.US_ASCII);
        udp = new UDPProtocol(1024, SRC, DST);  // MSS large enough for single segment
    }

    // —— Constructor tests —— //

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsZeroMSS() {
        new UDPProtocol(0, SRC, DST);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNegativeMSS() {
        new UDPProtocol(-1, SRC, DST);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullSource() {
        new UDPProtocol(100, null, DST);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullDestination() {
        new UDPProtocol(100, SRC, null);
    }

    @Test
    public void gettersReturnCorrectValues() {
        assertSame("sourcePort", SRC, udp.getSourcePort());
        assertSame("destinationPort", DST, udp.getDestinationPort());
        assertEquals("MSS", 1024, udp.getMSS());
    }

    // —— encapsulate(...) tests —— //

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateThrowsOnNull() {
        udp.encapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateThrowsOnEmpty() {
        udp.encapsulate(new byte[0]);
    }

    @Test(expected = RuntimeException.class)
    public void encapsulateThrowsWhenNextNotDefined() {
        udp.encapsulate(payload);
    }

    @Test(expected = NullPointerException.class)
    public void setNextRejectsNull() {
        udp.setNext(null);
    }

    @Test
    public void encapsulateSingleSegment() {
        // stub next protocol echoes input
        Protocol stubNext = new IdentityProtocol();
        udp.setNext(stubNext);

        byte[] datagram = udp.encapsulate(payload);
        // Only one UDPSegment created
        UDPSegment seg = new UDPSegment(SRC, DST, 0, payload);
        assertArrayEquals("single-segment datagram", seg.toByte(), datagram);
    }

    @Test
    public void encapsulateMultipleSegments() {
        // set MSS small to force fragmentation
        udp = new UDPProtocol(4, SRC, DST);

        Protocol stubNext = new IdentityProtocol();
        udp.setNext(stubNext);

        byte[] datagram = udp.encapsulate(payload);
        // compute expected fragments
        int expectedChunks = (int)Math.ceil((double)payload.length / 4);
        int index = 0;
        for (int seq = 0; seq < expectedChunks; seq++) {
            int chunkLen = Math.min(4, payload.length - seq * 4);
            byte[] chunk = Arrays.copyOfRange(payload, seq * 4, seq * 4 + chunkLen);
            UDPSegment expected = new UDPSegment(SRC, DST, seq, chunk);
            byte[] expectedBytes = expected.toByte();
            byte[] actualBytes = Arrays.copyOfRange(datagram, index, index + expectedBytes.length);
            assertArrayEquals("fragment " + seq, expectedBytes, actualBytes);
            index += expectedBytes.length;
        }
        // no leftover
        assertEquals("total datagram length", index, datagram.length);
    }

    // —— decapsulate(...) tests —— //

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateThrowsOnNull() {
        udp.decapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateThrowsOnEmpty() {
        udp.decapsulate(new byte[0]);
    }

    @Test(expected = RuntimeException.class)
    public void decapsulateThrowsWhenPreviousNotDefined() {
        // build a minimal length‐prefixed buffer
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(0);
        udp.decapsulate(buf.array());
    }

    @Test(expected = NullPointerException.class)
    public void setPreviousRejectsNull() {
        udp.setPrevious(null);
    }

    @Test
    public void decapsulateReassemblesPayload() throws Exception {
        // prepare two segments (out of order)
        byte[] part1 = "ABCDEFG".getBytes(StandardCharsets.US_ASCII);
        byte[] part2 = "12345".getBytes(StandardCharsets.US_ASCII);

        UDPSegment s1 = new UDPSegment(SRC, DST, 1, part1);
        UDPSegment s0 = new UDPSegment(SRC, DST, 0, part2);

        byte[] b1 = s1.toByte(), b0 = s0.toByte();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(b1);
        out.write(b0);

        // stub previous protocol that just echoes the payload
        Protocol stubPrev = new IdentityProtocol();
        udp.setPrevious(stubPrev);

        byte[] reassembled = udp.decapsulate(out.toByteArray());

        // Should come back in order s0 then s1:
        byte[] expected = new byte[part2.length + part1.length];
        System.arraycopy(part2, 0, expected, 0, part2.length);
        System.arraycopy(part1, 0, expected, part2.length, part1.length);

        assertArrayEquals("payload reassembled in order", expected, reassembled);
    }

    // —— Tests for extracSource(...) and extractDestination(...) —— //

    @Test(expected = IllegalArgumentException.class)
    public void extracSourceRejectsNull() {
        udp.extractSource(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void extracSourceRejectsTooShort() {
        udp.extractSource(new byte[3]);
    }

    @Test
    public void extracSourceReturnsCorrectPort() {
        byte[] data = "DATA".getBytes(StandardCharsets.US_ASCII);
        UDPSegment seg = new UDPSegment(SRC, DST, 5, data);
        byte[] raw = seg.toByte();

        Port extracted = udp.extractSource(raw);
        assertEquals("should extract original source port",
                     SRC.getPort(), extracted.getPort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void extractDestinationRejectsNull() {
        udp.extractDestination(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void extractDestinationRejectsTooShort() {
        udp.extractDestination(new byte[3]);
    }

    @Test
    public void extractDestinationReturnsCorrectPort() {
        byte[] data = "DATA".getBytes(StandardCharsets.US_ASCII);
        UDPSegment seg = new UDPSegment(SRC, DST, 7, data);
        byte[] raw = seg.toByte();

        Port extracted = udp.extractDestination(raw);
        assertEquals("should extract original destination port",
                     DST.getPort(), extracted.getPort());
    }
}