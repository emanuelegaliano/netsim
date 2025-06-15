package com.netsim.network;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import com.netsim.addresses.IPv4;
import com.netsim.app.App;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;

public class HostTest {
    private RoutingTable rt;
    private ArpTable arp;
    private List<Interface> ifaces;

    @Before
    public void setUp() {
        // for argument‐validation tests we can supply empty tables/lists
        rt = new RoutingTable();
        arp = new ArpTable();
        ifaces = Collections.emptyList();
    }

    // -------- constructor --------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullName() {
        new Host(null, rt, arp, ifaces);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullRoutingTable() {
        new Host("h1", null, arp, ifaces);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullArpTable() {
        new Host("h1", rt, null, ifaces);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullInterfaces() {
        new Host("h1", rt, arp, null);
    }

    // -------- setApp / runApp ----------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void runAppWithoutSettingThrows() {
        Host h = new Host("h1", rt, arp, ifaces);
        h.runApp();
    }

    @Test
    public void runAppInvokesAppStart() {
        Host h = new Host("h1", rt, arp, ifaces);

        class TestApp extends App {
            boolean started = false;
            TestApp() { super("t","u"); }
            @Override public void start() { started = true; }
            @Override public void receive(IPv4 s, byte[] d) { }
            @Override public void printAppMessage(String m) { }
        }

        TestApp a = new TestApp();
        h.setApp(a);
        h.runApp();
        assertTrue("runApp() must call App.start()", a.started);
    }

    // -------- send(...) argument validation -------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsNullRoute() {
        Host h = new Host("h1", rt, arp, ifaces);
        h.send(null, new ProtocolPipeline(Collections.emptyList()), new byte[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsNullPipeline() {
        Host h = new Host("h1", rt, arp, ifaces);
        h.send(new RoutingInfo(null,null), null, new byte[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsNullData() {
        Host h = new Host("h1", rt, arp, ifaces);
        h.send(new RoutingInfo(null,null), new ProtocolPipeline(Collections.emptyList()), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsEmptyData() {
        Host h = new Host("h1", rt, arp, ifaces);
        h.send(new RoutingInfo(null,null),
               new ProtocolPipeline(Collections.emptyList()),
               new byte[0]);
    }

    // -------- receive(...) argument validation ----------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void receiveRejectsNullPipeline() {
        Host h = new Host("h1", rt, arp, ifaces);
        h.receive(null, new byte[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void receiveRejectsNullData() {
        Host h = new Host("h1", rt, arp, ifaces);
        h.receive(new ProtocolPipeline(Collections.emptyList()), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void receiveRejectsEmptyData() {
        Host h = new Host("h1", rt, arp, ifaces);
        h.receive(new ProtocolPipeline(Collections.emptyList()), new byte[0]);
    }
}