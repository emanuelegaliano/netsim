package com.netsim.network.server;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import com.netsim.addresses.IPv4;
import com.netsim.app.App;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;

/**
 * Minimal tests for {@link Server}, covering constructor and method argument validation.
 */
public class ServerTest {
    private Server<App> server;

    /**
     * Simple App stub for test instantiation.
     */
    private static class DummyApp extends App {
        DummyApp() { super("dummy", "user"); }
        @Override public void start() { }
        @Override public void receive(IPv4 src, byte[] data) { }
        @Override public void printAppMessage(String m) { }
    }

    /**
     * Create a valid Server before each test.
     */
    @Before
    public void setUp() {
        server = new Server<>(
            "srv",                 // valid name
            new RoutingTable(),    // non-null routing table
            new ArpTable(),        // non-null ARP table
            Collections.emptyList(),// non-null interfaces list
            new DummyApp()         // non-null app
        );
    }

    // -------- constructor argument validation --------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullName() {
        new Server<>(null, new RoutingTable(), new ArpTable(), Collections.emptyList(), new DummyApp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullRoutingTable() {
        new Server<>("s", null, new ArpTable(), Collections.emptyList(), new DummyApp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullArpTable() {
        new Server<>("s", new RoutingTable(), null, Collections.emptyList(), new DummyApp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullInterfaces() {
        new Server<>("s", new RoutingTable(), new ArpTable(), null, new DummyApp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullApp() {
        new Server<>("s", new RoutingTable(), new ArpTable(), Collections.emptyList(), null);
    }

    // -------- send(...) argument validation --------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsNullRoute() throws Exception {
        server.send(null, new ProtocolPipeline(Collections.emptyList()), new byte[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsNullPipeline() throws Exception {
        server.send(new RoutingInfo(null,null), null, new byte[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsNullData() throws Exception {
        server.send(new RoutingInfo(null,null), new ProtocolPipeline(Collections.emptyList()), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsEmptyData() throws Exception {
        server.send(new RoutingInfo(null,null), new ProtocolPipeline(Collections.emptyList()), new byte[0]);
    }

    // -------- receive(...) argument validation -----------------------------

    @Test(expected = IllegalArgumentException.class)
    public void receiveRejectsNullPipeline() {
        server.receive(null, new byte[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void receiveRejectsNullData() {
        server.receive(new ProtocolPipeline(Collections.emptyList()), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void receiveRejectsEmptyData() {
        server.receive(new ProtocolPipeline(Collections.emptyList()), new byte[0]);
    }
}