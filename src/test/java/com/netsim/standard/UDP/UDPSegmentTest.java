package com.netsim.standard.UDP;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.netsim.addresses.Port;

public class UDPSegmentTest {
    private static final Port SRC = new Port("1000");
    private static final Port DST = new Port("2000");
    private static final byte[] PAYLOAD = new byte[] { 1, 2, 3, 4, 5 };

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullSource() {
        new UDPSegment(null, DST, 0, PAYLOAD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullDestination() {
        new UDPSegment(SRC, null, 0, PAYLOAD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullPayload() {
        new UDPSegment(SRC, DST, 0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsEmptyPayload() {
        new UDPSegment(SRC, DST, 0, new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsTooLargeSequenceNumber() {
        // Short.MAX_VALUE + 1
        new UDPSegment(SRC, DST, Short.MAX_VALUE + 1, PAYLOAD);
    }

    @Test
    public void gettersReturnCorrectValues() {
        int seq = 12345;
        UDPSegment seg = new UDPSegment(SRC, DST, seq, PAYLOAD);
        assertEquals("sequenceNumber should match", (short) seq, seg.getSequenceNumber());
        // length in bits = (headerBytes (8) + payloadBytes (5)) * 8 = 104
        assertEquals("length should be header+payload bits", (short)104, seg.getLength());
    }

    @Test
    public void headerIsEightBytesAndInNetworkOrder() {
        int seq = 0x1234;
        UDPSegment seg = new UDPSegment(SRC, DST, seq, PAYLOAD);
        byte[] header = seg.getHeader();
        assertEquals("header length must be 8 bytes", 8, header.length);

        ByteBuffer buf = ByteBuffer.wrap(header);
        // ports
        byte[] srcBytes = SRC.byteRepresentation();
        byte[] dstBytes = DST.byteRepresentation();
        byte[] readSrc = new byte[2];
        byte[] readDst = new byte[2];
        buf.get(readSrc);
        buf.get(readDst);
        assertArrayEquals("source port bytes", srcBytes, readSrc);
        assertArrayEquals("destination port bytes", dstBytes, readDst);
        // sequence number
        assertEquals("sequence number", (short) seq, buf.getShort());
        // length bits
        assertEquals("length bits", seg.getLength(), buf.getShort());
    }

    @Test
    public void toByteConcatenatesHeaderAndPayload() {
        UDPSegment seg = new UDPSegment(SRC, DST, 1, PAYLOAD);
        byte[] raw = seg.toByte();
        byte[] header = seg.getHeader();
        assertEquals("raw starts with header", header.length + PAYLOAD.length, raw.length);

        // header part
        for (int i = 0; i < header.length; i++) {
            assertEquals("raw header byte at " + i, header[i], raw[i]);
        }
        // payload part
        for (int i = 0; i < PAYLOAD.length; i++) {
            assertEquals("raw payload byte at " + i,
                         PAYLOAD[i], raw[header.length + i]);
        }
    }

    @Test
    public void fromBytesRoundTrips() {
        UDPSegment original = new UDPSegment(SRC, DST, 55, new byte[] {9,8,7});
        byte[] data = original.toByte();
        UDPSegment parsed = UDPSegment.fromBytes(data);

        // same sequence
        assertEquals(original.getSequenceNumber(), parsed.getSequenceNumber());
        // same length
        assertEquals(original.getLength(), parsed.getLength());
        // same payload
        byte[] origPayload = original.toByte();
        byte[] parsedRaw   = parsed.toByte();
        // compare full raw bytes
        assertArrayEquals("full segment round-trip", origPayload, parsedRaw);
        // ports
        assertArrayEquals("parsed source port",
                          SRC.byteRepresentation(),
                          parsed.getSource().byteRepresentation());
        assertArrayEquals("parsed dest port",
                          DST.byteRepresentation(),
                          parsed.getDestination().byteRepresentation());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromBytesRejectsNull() {
        UDPSegment.fromBytes(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromBytesRejectsTooShort() {
        UDPSegment.fromBytes(new byte[7]); // header is 8 bytes
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromBytesRejectsLengthMismatch() {
        UDPSegment seg = new UDPSegment(SRC, DST, 1, PAYLOAD);
        byte[] data = seg.toByte();
        // corrupt the length field (bytes 6-7)
        data[6] = 0;
        data[7] = 0;
        UDPSegment.fromBytes(data);
    }
}
