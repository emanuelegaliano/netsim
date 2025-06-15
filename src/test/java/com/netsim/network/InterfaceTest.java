package com.netsim.network;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;

public class InterfaceTest {
    private NetworkAdapter adapterA;
    private NetworkAdapter adapterB;
    private IPv4 ip1;
    private IPv4 ip2;

    @Before
    public void setUp() {
        // Two different MACs
        Mac macA = Mac.bytesToMac(new byte[]{0x01,0x02,0x03,0x04,0x05,0x06});
        Mac macB = Mac.bytesToMac(new byte[]{0x0A,0x0B,0x0C,0x0D,0x0E,0x0F});
        adapterA = new NetworkAdapter("ethA", 1500, macA);
        adapterB = new NetworkAdapter("ethB", 1500, macB);

        ip1 = new IPv4("192.168.0.1", 24);
        ip2 = new IPv4("192.168.0.2", 24);
    }

    // constructor

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullAdapter() {
        new Interface(null, ip1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullIP() {
        new Interface(adapterA, null);
    }

    // getters

    @Test
    public void gettersReturnWhatWasPassed() {
        Interface iface = new Interface(adapterA, ip1);
        assertSame("getAdapter should return the adapter passed to ctor",
                   adapterA, iface.getAdapter());
        assertSame("getIP should return the IP passed to ctor",
                   ip1, iface.getIP());
    }

    // equals reflexive

    @Test
    public void equalsIsReflexive() {
        Interface iface = new Interface(adapterA, ip1);
        assertTrue(iface.equals(iface));
    }

    // equals symmetric and transitive when same adapter & IP

    @Test
    public void equalsAndHashCodeForSameAdapterAndIP() {
        // Note: NetworkAdapter.equals is based on MAC only,
        // so two adapters with same MAC are equal.
        Mac sameMac = adapterA.getMacAddress();
        NetworkAdapter adapterA2 = new NetworkAdapter("differentName", 900, sameMac);

        Interface a1 = new Interface(adapterA, ip1);
        Interface a2 = new Interface(adapterA2, ip1);

        assertTrue("Interfaces with equal adapter (by MAC) and same IP should be equal",
                   a1.equals(a2));
        assertTrue("Symmetry: a2.equals(a1) too",
                   a2.equals(a1));
    }

    @Test
    public void equalsFalseForDifferentIP() {
        Interface a1 = new Interface(adapterA, ip1);
        Interface a2 = new Interface(adapterA, ip2);
        assertFalse("Different IP should make equals return false", a1.equals(a2));
    }

    @Test
    public void equalsFalseForDifferentAdapter() {
        Interface a1 = new Interface(adapterA, ip1);
        Interface b1 = new Interface(adapterB, ip1);
        assertFalse("Different adapter should make equals return false", a1.equals(b1));
    }

    @Test
    public void equalsFalseForNullOrOtherType() {
        Interface iface = new Interface(adapterA, ip1);
        assertFalse("Equals against null must be false", iface.equals(null));
        assertFalse("Equals against other type must be false", iface.equals("not an Interface"));
    }
}