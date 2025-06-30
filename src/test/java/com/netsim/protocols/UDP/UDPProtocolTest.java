package com.netsim.protocols.UDP;

import com.netsim.addresses.Port;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UDPProtocolTest {

    private Port srcPort;
    private Port dstPort;
    private UDPProtocol udp;

    @Before
    public void setUp() {
        srcPort = new Port("1234");
        dstPort = new Port("5678");
        udp = new UDPProtocol(10, srcPort, dstPort); // small MSS to force segmentation
    }

    private byte[] samplePayload(int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) data[i] = (byte) (i % 256);
        return data;
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullSource() {
        new UDPProtocol(10, null, dstPort);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullDestination() {
        new UDPProtocol(10, srcPort, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNonPositiveMSS() {
        new UDPProtocol(0, srcPort, dstPort);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateRejectsNullPayload() {
        udp.encapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateRejectsEmptyPayload() {
        udp.encapsulate(new byte[0]);
    }

    @Test
    public void encapsulateAndDecapsulateRoundTrip() {
        byte[] payload = samplePayload(25); // will be split into 3 segments
        byte[] encoded = udp.encapsulate(payload);
        byte[] decoded = udp.decapsulate(encoded);
        assertArrayEquals("Decoded payload must match original", payload, decoded);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsNull() {
        udp.decapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsEmpty() {
        udp.decapsulate(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsMalformedSegment() {
        // too short to be valid segment
        byte[] badSegment = new byte[4];
        udp.decapsulate(badSegment);
    }

    @Test
    public void testExtractSourceAndDestination() {
        byte[] payload = samplePayload(8);
        byte[] encoded = udp.encapsulate(payload);

        Port src = udp.extractSource(encoded);
        Port dst = udp.extractDestination(encoded);

        assertEquals("Source port should match", srcPort, src);
        assertEquals("Destination port should match", dstPort, dst);
    }

    @Test
    public void testGetSourceAndDestination() {
        assertEquals("Get source should match", srcPort, udp.getSource());
        assertEquals("Get destination should match", dstPort, udp.getDestination());
    }

    @Test
    public void testCopyCreatesEqualButDistinctProtocol() {
        UDPProtocol copy = (UDPProtocol) udp.copy();
        assertNotSame("Copy should not be the same instance", udp, copy);
        assertEquals("Source should match", udp.getSource(), copy.getSource());
        assertEquals("Destination should match", udp.getDestination(), copy.getDestination());
        assertEquals("MSS should match", udp.getMSS(), copy.getMSS());
    }
}