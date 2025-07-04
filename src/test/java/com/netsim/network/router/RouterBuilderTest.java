package com.netsim.network.router;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.CabledAdapter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RouterBuilderTest {

    private RouterBuilder builder;
    private IPv4 subnet;
    private IPv4 nextHop;
    private IPv4 localIP;
    private NetworkAdapter adapter;

    @Before
    public void setUp() {
        builder = new RouterBuilder();
        subnet = new IPv4("192.168.0.0", 24);
        nextHop = new IPv4("192.168.0.1", 32);
        localIP = new IPv4("192.168.0.10", 32);
        adapter = new CabledAdapter("eth0", 1500, new Mac("aa:bb:cc:00:11:22"));
    }

    @Test(expected = RuntimeException.class)
    public void buildFailsWithEmptyRoutingTable() {
        builder.addArpEntry(nextHop, adapter.getMacAddress())
               .addInterface(new Interface(adapter, localIP))
               .build(); // missing route
    }

    @Test(expected = RuntimeException.class)
    public void buildFailsWithEmptyArpTable() {
        builder.addInterface(new Interface(adapter, localIP))
               .addRoute(subnet, "eth0", nextHop)
               .build(); // missing ARP
    }

    @Test(expected = RuntimeException.class)
    public void buildFailsWithNoInterfaces() {
        builder.addRoute(subnet, "eth0", nextHop)
               .addArpEntry(nextHop, adapter.getMacAddress())
               .build(); // missing interface
    }

    @Test
    public void buildSucceedsWithAllRequiredFields() {
        Router router = builder.setName("Router1")
                .addInterface(new Interface(adapter, localIP))
                .addRoute(subnet, "eth0", nextHop)
                .addArpEntry(nextHop, adapter.getMacAddress())
                .build();

        assertNotNull(router);
        assertEquals("Router1", router.getName());
        assertEquals(1, router.getInterfaces().size());
    }
}