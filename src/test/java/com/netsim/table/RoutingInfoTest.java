package com.netsim.table;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.network.CabledAdapter;
import com.netsim.network.NetworkAdapter;

public class RoutingInfoTest {
    private NetworkAdapter adapter1;
    private NetworkAdapter adapter2;
    private IPv4 nextHop1;
    private IPv4 nextHop2;

    @Before
    public void setUp() {
        adapter1 = new CabledAdapter("eth0", 1500, new Mac("aa:bb:cc:00:11:22"));
        adapter2 = new CabledAdapter("eth1", 1500, new Mac("aa:bb:cc:00:33:44"));
        nextHop1 = new IPv4("192.168.1.1", 32);
        nextHop2 = new IPv4("10.0.0.1", 32);
    }

    @Test
    public void constructorAndGettersValid() {
        RoutingInfo info = new RoutingInfo(adapter1, nextHop1);
        assertEquals("Device should match constructor argument", adapter1, info.getDevice());
        assertEquals("NextHop should match constructor argument", nextHop1, info.getNextHop());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullDevice() {
        new RoutingInfo(null, nextHop1);
    }

    @Test
    public void constructorAllowsNullNextHop() {
        RoutingInfo info = new RoutingInfo(adapter1, null);
        assertEquals(adapter1, info.getDevice());
        assertNull(info.getNextHop());
    }

    @Test
    public void setDeviceValid() {
        RoutingInfo info = new RoutingInfo(adapter1, nextHop1);
        info.setDevice(adapter2);
        assertEquals("Device should have been updated to adapter2", adapter2, info.getDevice());
        // NextHop remains unchanged
        assertEquals("NextHop should remain nextHop1", nextHop1, info.getNextHop());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setDeviceRejectsNull() {
        RoutingInfo info = new RoutingInfo(adapter1, nextHop1);
        info.setDevice(null);
    }

    @Test
    public void setNextHopValid() {
        RoutingInfo info = new RoutingInfo(adapter1, nextHop1);
        info.setNextHop(nextHop2);
        assertEquals("NextHop should have been updated to nextHop2", nextHop2, info.getNextHop());
        // Device remains unchanged
        assertEquals("Device should remain adapter1", adapter1, info.getDevice());
    }

    @Test
    public void setNextHopToNullAllowed() {
        RoutingInfo info = new RoutingInfo(adapter1, nextHop1);
        info.setNextHop(null);
        assertNull("NextHop should be null (directly connected)", info.getNextHop());
        // Device remains unchanged
        assertEquals("Device should remain adapter1", adapter1, info.getDevice());
    }
}