package com.netsim.protocols.SimpleDLL;

import com.netsim.addresses.Mac;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleDLLProtocolTest {

    private Mac srcMac;
    private Mac dstMac;
    private SimpleDLLProtocol protocol;

    @Before
    public void setUp() {
        srcMac = new Mac("aa:bb:cc:dd:ee:ff");
        dstMac = new Mac("11:22:33:44:55:66");
        protocol = new SimpleDLLProtocol(srcMac, dstMac);
    }

    private byte[] sampleIPv4Packet() {
        byte[] packet = new byte[40];
        packet[0] = 0x45; // Version=4, IHL=5
        packet[2] = 0x00; // Total length high byte
        packet[3] = 0x28; // Total length = 40
        return packet;
    }

    @Test
    public void testEncapsulateAndDecapsulate() {
        byte[] ip = sampleIPv4Packet();
        byte[] framed = protocol.encapsulate(ip);
        byte[] unframed = protocol.decapsulate(framed);

        assertArrayEquals("Decapsulated data should match original IP", ip, unframed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncapsulateRejectsNull() {
        protocol.encapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncapsulateRejectsEmpty() {
        protocol.encapsulate(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncapsulateRejectsInvalidIHL() {
        byte[] ip = sampleIPv4Packet();
        ip[0] = 0x41; // IHL = 1 (invalid)
        protocol.encapsulate(ip);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncapsulateRejectsInvalidLength() {
        byte[] ip = sampleIPv4Packet();
        ip[2] = 0x00;
        ip[3] = 0x10; // total length = 16 < header
        protocol.encapsulate(ip);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecapsulateRejectsNull() {
        protocol.decapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecapsulateRejectsTooShort() {
        protocol.decapsulate(new byte[10]);
    }

    @Test
    public void testExtractSourceAndDestination() {
        byte[] ip = sampleIPv4Packet();
        byte[] framed = protocol.encapsulate(ip);

        Mac extractedDst = protocol.extractDestination(framed);
        Mac extractedSrc = protocol.extractSource(framed);

        assertEquals("Extracted source MAC should match", srcMac, extractedSrc);
        assertEquals("Extracted destination MAC should match", dstMac, extractedDst);
    }

    @Test
    public void testGettersAndCopy() {
        assertEquals(srcMac, protocol.getSource());
        assertEquals(dstMac, protocol.getDestination());

        SimpleDLLProtocol copy = (SimpleDLLProtocol) protocol.copy();
        assertNotSame(protocol, copy);
        assertEquals(protocol.getSource(), copy.getSource());
        assertEquals(protocol.getDestination(), copy.getDestination());
    }
}