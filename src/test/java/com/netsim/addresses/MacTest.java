package com.netsim.addresses;

import static org.junit.Assert.*;

import org.junit.Test;

public class MacTest {

    @Test
    public void testParseValidColonSeparated() {
        Mac mac = new Mac("AA:BB:CC:DD:EE:FF");
        byte[] expected = new byte[] {
            (byte)0xAA, (byte)0xBB, (byte)0xCC,
            (byte)0xDD, (byte)0xEE, (byte)0xFF
        };
        assertArrayEquals("Parsed bytes should match expected", expected, mac.byteRepresentation());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWrongSeparator() {
        new Mac("AA-BB-CC-DD-EE-FF");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTooFewOctets() {
        new Mac("AA:BB:CC:DD:EE");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTooManyOctets() {
        new Mac("AA:BB:CC:DD:EE:FF:11");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidHex() {
        new Mac("GG:HH:CC:DD:EE:FF");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidLengthOctet() {
        new Mac("A:BB:CC:DD:EE:FF");
    }

    @Test
    public void testSetAddressValid() {
        Mac mac = new Mac("00:11:22:33:44:55");
        mac.setAddress("FE:DC:BA:98:76:54");
        byte[] expected = new byte[] {
            (byte)0xFE, (byte)0xDC, (byte)0xBA,
            (byte)0x98, (byte)0x76, (byte)0x54
        };
        assertArrayEquals("setAddress should update the internal bytes", expected, mac.byteRepresentation());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetAddressInvalid() {
        Mac mac = new Mac("00:11:22:33:44:55");
        mac.setAddress("00:11:22:33:44"); // too few octets
    }

    @Test
    public void testStringRepresentation() {
        Mac mac = new Mac("0A:0b:0C:0d:0E:0f");
        String repr = mac.stringRepresentation();
        assertEquals("String representation should be uppercase two-digit hex", 
                     "0A:0B:0C:0D:0E:0F", repr);
    }

    @Test
    public void testRoundTripStringRepresentation() {
        String original = "12:34:56:78:9A:BC";
        Mac mac = new Mac(original);
        String repr = mac.stringRepresentation();
        assertEquals("Round-trip of stringRepresentation and parse should preserve value", original, repr);
    }
}
