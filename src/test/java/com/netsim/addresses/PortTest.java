package com.netsim.addresses;

import org.junit.Test;

import static org.junit.Assert.*;

public class PortTest {

    @Test
    public void testValidConstructor() {
        Port p = new Port("8080");
        assertEquals(8080, p.getPort());
    }

    @Test
    public void testSetAddressInt() {
        Port p = new Port("1234");
        p.setAddress(4321);
        assertEquals(4321, p.getPort());
    }

    @Test
    public void testSetAddressStringValid() {
        Port p = new Port("1000");
        p.setAddress("2000");
        assertEquals(2000, p.getPort());
    }

    @Test
    public void testSetAddressStringInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Port("abc");
        });
    }

    @Test
    public void testSetAddressOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Port("70000");
        });
    }

    @Test
    public void testParseValid() {
        Port p = new Port("0");
        byte[] bytes = p.parse("65535");
        assertEquals((byte) 0xFF, bytes[0]);
        assertEquals((byte) 0xFF, bytes[1]);
        assertEquals(65535, p.getPort());
    }

    @Test
    public void testParseInvalidFormat() {
        Port p = new Port("0");
        assertThrows(IllegalArgumentException.class, () -> {
            p.parse("invalid");
        });
    }

    @Test
    public void testParseOutOfRange() {
        Port p = new Port("0");
        assertThrows(IllegalArgumentException.class, () -> {
            p.parse("99999");
        });
    }

    @Test
    public void testFromBytesValid() {
        // 0x1F90 = 8080 in big-endian
        byte[] data = new byte[] { (byte) 0x1F, (byte) 0x90 };
        Port p = Port.fromBytes(data);
        assertEquals(8080, p.getPort());
        // Verifica che l'array interno corrisponda
        assertArrayEquals(data, p.byteRepresentation());
    }

}
