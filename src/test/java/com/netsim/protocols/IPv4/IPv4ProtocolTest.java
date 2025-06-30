
package com.netsim.protocols.IPv4;

import com.netsim.addresses.IPv4;
import org.junit.Test;
import static org.junit.Assert.*;

public class IPv4ProtocolTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullSource() {
        new IPv4Protocol(null, new IPv4("10.0.0.1", 24), 5, 0, 0, 0, 64, 17, 1500);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullDestination() {
        new IPv4Protocol(new IPv4("192.168.0.1", 24), null, 5, 0, 0, 0, 64, 17, 1500);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidIHL() {
        new IPv4Protocol(new IPv4("192.168.0.1", 24), new IPv4("10.0.0.1", 24), 4, 0, 0, 0, 64, 17, 1500);
    }

    @Test
    public void testEncapsulationAndDecapsulation() {
        IPv4 src = new IPv4("192.168.0.1", 24);
        IPv4 dst = new IPv4("10.0.0.1", 24);
        IPv4Protocol protocol = new IPv4Protocol(src, dst, 5, 0, 1234, 0, 64, 17, 100);

        byte[] payload = new byte[50];
        for (int i = 0; i < 50; i++) payload[i] = (byte) (i + 1);

        byte[] wire = protocol.encapsulate(payload);
        assertNotNull(wire);
        assertTrue(wire.length > 0);

        byte[] reassembled = protocol.decapsulate(wire);
        assertArrayEquals(payload, reassembled);
    }

    @Test
    public void testExtractSourceAndDestination() {
        IPv4 src = new IPv4("192.168.0.1", 24);
        IPv4 dst = new IPv4("10.0.0.1", 24);
        IPv4Protocol protocol = new IPv4Protocol(src, dst, 5, 0, 1, 0, 64, 17, 100);

        byte[] payload = new byte[20];
        byte[] wire = protocol.encapsulate(payload);

        IPv4 extractedSrc = protocol.extractSource(wire);
        IPv4 extractedDst = protocol.extractDestination(wire);

        assertEquals("192.168.0.1", extractedSrc.stringRepresentation());
        assertEquals("10.0.0.1", extractedDst.stringRepresentation());
    }
}