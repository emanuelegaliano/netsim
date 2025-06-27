package com.netsim.network.host;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import com.netsim.addresses.Mac;
import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.app.CommandFactory;
import com.netsim.network.Interface;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.SimpleDLL.SimpleDLLProtocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;

public class HostTest {
    private RoutingTable rt;
    private ArpTable arp;
    private List<Interface> ifaces;
    private Mac dummyMac;

    @Before
    public void setUp() {
        // for argument‐validation tests we can supply empty tables/lists
        rt = new RoutingTable();
        arp = new ArpTable();
        ifaces = Collections.emptyList();
        dummyMac = Mac.bytesToMac(new byte[]{0,0,0,0,0,0});
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
    public void setAppRejectsNull() {
        Host h = new Host("h1", rt, arp, ifaces);
        h.setApp(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void runAppWithoutSettingThrows() {
        Host h = new Host("h1", rt, arp, ifaces);
        h.runApp();
    }

    @Test
    public void runAppInvokesAppStart() {
        Host h = new Host("h1", rt, arp, ifaces);
        CommandFactory fakeFactory = new CommandFactory() {
            public Command get(String key) {
                return null;
            }
        };

        class TestApp extends App {
            boolean started = false;
            TestApp() { super("t","u", fakeFactory, h); }
            @Override public void start(NetworkNode n) { started = true; }
            @Override public void receive(byte[] data) { }
            @Override public void printAppMessage(String message) { }
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
        ProtocolPipeline pipeline = new ProtocolPipeline();
        pipeline.push(new SimpleDLLProtocol(dummyMac, dummyMac));

        h.send(null, pipeline, new byte[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsNullPipeline() {
        Host h = new Host("h1", rt, arp, ifaces);
        h.send(new RoutingInfo(null,null), null, new byte[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsNullData() {
        Host h = new Host("h1", rt, arp, ifaces);
        ProtocolPipeline pipeline = new ProtocolPipeline();
        pipeline.push(new SimpleDLLProtocol(dummyMac, dummyMac));

        h.send(new RoutingInfo(null,null), pipeline, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsEmptyData() {
        Host h = new Host("h1", rt, arp, ifaces);
        ProtocolPipeline pipeline = new ProtocolPipeline();
        pipeline.push(new SimpleDLLProtocol(dummyMac, dummyMac));

        h.send(new RoutingInfo(null,null), pipeline, new byte[0]);
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
        ProtocolPipeline pipeline = new ProtocolPipeline();
        pipeline.push(new SimpleDLLProtocol(dummyMac, dummyMac));

        h.receive(pipeline, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void receiveRejectsEmptyData() {
        Host h = new Host("h1", rt, arp, ifaces);
        ProtocolPipeline pipeline = new ProtocolPipeline();
        pipeline.push(new SimpleDLLProtocol(dummyMac, dummyMac));

        h.receive(pipeline, new byte[0]);
    }
}