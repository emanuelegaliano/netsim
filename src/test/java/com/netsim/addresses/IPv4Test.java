package com.netsim.addresses;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IPv4Test {
    @Test
    public void testSubnetWithString() {
        int mask = 24;
        IPv4 ip = new IPv4("192.168.1.100", 24);

        assertTrue(
            "192.168.1.100 should be in 192.168.1.0/24",
            ip.isInSubnet("192.168.1.0", mask)
        );
        assertFalse(
            "192.168.1.100 should not be in 192.168.2.0/24",
            ip.isInSubnet("192.168.2.0", mask)
        );

        mask = 16;
        ip.setAddress("172.16.5.10", mask);
            assertTrue(
            "172.16.5.10 should be in 172.16.0.0/16",
            ip.isInSubnet("172.16.0.0", mask)
        );
        assertFalse(
            "172.16.5.10 should NOT be in 172.17.0.0/16",
            ip.isInSubnet("172.17.0.0", mask)
        );

        mask = 30;
        ip.setAddress("10.0.0.2", mask);
        assertTrue(
            "10.0.0.2 should be in 10.0.0.0/30",
            ip.isInSubnet("10.0.0.0", mask)
        );
        assertFalse(
            "10.0.0.2 should NOT be in 10.0.0.4/30",
            ip.isInSubnet("10.0.0.4", mask)
        );
    }

    @Test
    public void testLoopback() {
        IPv4 ipLoop = new IPv4("127.0.0.1", 32);
        assertTrue("127.0.0.1 should be detected as loopback", ipLoop.isLoopback());

        IPv4 ipNonLoop = new IPv4("128.0.0.1", 32);
        assertFalse("128.0.0.1 should NOT be detected as loopback", ipNonLoop.isLoopback());
    }

    @Test
    public void testMulticast() {
        IPv4 ipMc = new IPv4("224.0.0.1", 32);
        assertTrue("224.0.0.1 should be detected as multicast", ipMc.isMulticast());

        IPv4 ipNonMc = new IPv4("223.255.255.255", 32);
        assertFalse("223.255.255.255 should NOT be detected as multicast", ipNonMc.isMulticast());
    }

    @Test
    public void testBroadcast() {
        IPv4 ipBc = new IPv4("255.255.255.255", 32);
        assertTrue("255.255.255.255 should be detected as broadcast", ipBc.isBroadcast());

        IPv4 ipNonBc = new IPv4("255.255.255.254", 32);
        assertFalse("255.255.255.254 should NOT be detected as broadcast", ipNonBc.isBroadcast());
    }

    @Test
    public void testPrivate() {
        // private addresses
        IPv4 ip10 = new IPv4("10.0.0.1", 32);
        assertTrue("10.0.0.1 should be private", ip10.isPrivate());

        IPv4 ip172 = new IPv4("172.16.0.1", 32);
        assertTrue("172.16.0.1 should be private", ip172.isPrivate());

        IPv4 ip192 = new IPv4("192.168.1.1", 32);
        assertTrue("192.168.1.1 should be private", ip192.isPrivate());

        // non-private addresses
        IPv4 ip8 = new IPv4("8.8.8.8", 32);
        assertFalse("8.8.8.8 should NOT be private", ip8.isPrivate());

        IPv4 ip172bad = new IPv4("172.32.0.1", 32);
        assertFalse("172.32.0.1 should NOT be private", ip172bad.isPrivate());

        IPv4 ip192bad = new IPv4("192.167.1.1", 32);
        assertFalse("192.167.1.1 should NOT be private", ip192bad.isPrivate());
    }

    @Test
    public void testLinkLocal() {
        IPv4 ipLink = new IPv4("169.254.0.1", 32);
        assertTrue("169.254.0.1 should be link-local", ipLink.isLinkLocal());

        IPv4 ipNonLink = new IPv4("169.255.0.1", 32);
        assertFalse("169.255.0.1 should NOT be link-local", ipNonLink.isLinkLocal());
    }

    @Test
    public void testUnspecified() {
        IPv4 ipUnspec = new IPv4("0.0.0.0", 32);
        assertTrue("0.0.0.0 should be unspecified", ipUnspec.isUnspecified());

        IPv4 ipNonUnspec = new IPv4("0.0.0.1", 32);
        assertFalse("0.0.0.1 should NOT be unspecified", ipNonUnspec.isUnspecified());
    }
        
    @Test
    public void testAddressByteLength() {
        IPv4 ip = new IPv4("192.168.1.100", 24);
        byte[] bytes = ip.byteRepresentation();
        // should be represented by 4 bytes (32 bits)
        assertEquals("Address byte array should have length 4", 4, bytes.length);
        assertEquals("Address should be represented in 32 bits", 32, bytes.length * 8);
    }
    
}
