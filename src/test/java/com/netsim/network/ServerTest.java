package com.netsim.network;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.IPv4;
import com.netsim.app.App;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.SimpleDLL.SimpleDLLProtocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;

public class ServerTest {
    private Server<MyApp> server;
    private RoutingTable   routingTable;
    private ArpTable       arpTable;
    private NetworkAdapter a, b;
    private Interface      ifaceA, ifaceB;
    private MyApp          app;

    /** A minimal App stub that records receive calls */
    private static class MyApp extends App {
        AtomicBoolean started = new AtomicBoolean(false);
        IPv4 lastSource;
        byte[] lastData;

        MyApp() { super("myapp",""); }

        @Override public void start()   { started.set(true); }

        @Override
        public void receive(IPv4 source, byte[] data) {
            lastSource = source;
            lastData = data;
        }

        @Override public void printAppMessage(String m) {}
    }

    /** A minimal DLL‐protocol stub: identity encapsulate/decapsulate, always broadcast */
    private static class StubDLL implements Protocol {
        public byte[] encapsulate(byte[] pdu)   { return pdu; }
        public byte[] decapsulate(byte[] pdu) { return pdu; }
        public void setNext(Protocol next)    {}
        public void setPrevious(Protocol prev){}
        public Object getSource()             { return null; }
        public Object getDestination()        { return null; }
        public Object extractSource(byte[] pdu)      { return null; }
        public Object extractDestination(byte[] pdu) { return Mac.broadcast(); }
        public Protocol copy()                { return this; }
    }

    /** A minimal pipeline that just holds our StubDLL */
    private ProtocolPipeline pipeline = new ProtocolPipeline(
        Collections.<Protocol>singletonList(new StubDLL())
    );

    /** A minimal RoutingInfo stub */
    private static class StubRoute implements RoutingInfo {
        private final NetworkAdapter dev;
        StubRoute(NetworkAdapter d) { dev = d; }
        @Override public NetworkAdapter getDevice() { return dev; }
    }

    @Before
    public void setUp() {
        // dummy ARP table
        arpTable = new ArpTable();
        // we'll override lookup only when needed
        routingTable = new RoutingTable();

        // two adapters point‐to‐point
        a = new NetworkAdapter("A", 1500, Mac.bytesToMac(new byte[]{1,1,1,1,1,1}));
        b = new NetworkAdapter("B", 1500, Mac.bytesToMac(new byte[]{2,2,2,2,2,2}));
        a.setRemoteAdapter(b);
        b.setRemoteAdapter(a);

        // two interfaces, only ifaceA on server
        ifaceA = new Interface(a, new IPv4("10.0.0.1", 24));
        ifaceB = new Interface(b, new IPv4("10.0.0.2", 24));

        app    = new MyApp();
        server = new Server<>(
            "S",
            routingTable,
            arpTable,
            Collections.singletonList(ifaceA),
            app
        );
    }

    //–– constructor validation ––––––––––––––––––––––––––––––––––––––––––

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullApp() {
        new Server<>("X", routingTable, arpTable, Collections.emptyList(), null);
    }

    //–– send(…) validation ––––––––––––––––––––––––––––––––––––––––––––––

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsNullRoute() {
        server.send(null, pipeline, new byte[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsNullPipeline() {
        server.send(new StubRoute(a), null, new byte[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsNullData() {
        server.send(new StubRoute(a), pipeline, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsEmptyData() {
        server.send(new StubRoute(a), pipeline, new byte[0]);
    }

    //–– send(…) happy‐path forwards to remote.receive(…) ––––––––––––––––––––––––––

    @Test
    public void sendForwardsToRemoteReceive() {
        byte[] payload = {10,20,30};
        // let routingTable always return StubRoute(a)
        routingTable = new RoutingTable() {
            @Override public RoutingInfo lookup(IPv4 d) { return new StubRoute(a); }
        };
        server = new Server<>("S", routingTable, arpTable,
                              Collections.singletonList(ifaceA), app);

        // stub out the remote Node to capture receive
        AtomicBoolean called = new AtomicBoolean(false);
        Node remoteNode = new Node() {
            @Override
            public void receive(ProtocolPipeline p, byte[] data) {
                assertSame("pipeline passed", pipeline, p);
                assertArrayEquals("data passed", payload, data);
                called.set(true);
            }
            @Override public void send(RoutingInfo r, ProtocolPipeline p, byte[] d) {}
        };
        a.setOwner(remoteNode);

        // act
        server.send(new StubRoute(a), pipeline, payload);

        assertTrue("remote.receive(...) must have been invoked", called.get());
    }

    //–– receive(…) validation ––––––––––––––––––––––––––––––––––––––––––––––

    @Test(expected = IllegalArgumentException.class)
    public void receiveRejectsNullPipeline() {
        server.receive(null, new byte[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void receiveRejectsNullData() {
        server.receive(pipeline, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void receiveRejectsEmptyData() {
        server.receive(pipeline, new byte[0]);
    }

    //–– receive(…) with non‐matching destination does nothing –––––––––––––––––—

    @Test
    public void receiveDoesNothingIfDestinationNotMine() {
        // pipeline.extractDestinationFrom will produce ifaceB.getIP()
        ProtocolPipeline p2 = new ProtocolPipelineBuilder()
            .addProtocol(new StubDLL())
            .build();
        // override extractDestinationFrom
        ProtocolPipeline robbed = new ProtocolPipeline(Collections.singletonList(new StubDLL())) {
            @Override
            public IPv4 extractDestinationFrom(Class<IPv4Protocol> c, byte[] d) {
                return ifaceB.getIP();
            }
            @Override
            public IPv4 extractSourceFrom(Class<IPv4Protocol> c, byte[] d) {
                return ifaceB.getIP();
            }
        };
        server.receive(robbed, new byte[]{1,2,3});
        // app.receive should never have been called
        assertNull("no app.receive", app.lastSource);
    }

    //–– receive(…) with matching destination but no app throws –––––––––––––––––––—

    @Test(expected = RuntimeException.class)
    public void receiveThrowsIfAppNotSet() {
        server = new Server<>("S", routingTable, arpTable,
                             Collections.singletonList(ifaceA), null);
        // need a pipeline that returns ifaceA.getIP()
        ProtocolPipeline rob = new ProtocolPipeline(Collections.singletonList(new StubDLL())) {
            @Override
            public IPv4 extractDestinationFrom(Class<IPv4Protocol> c, byte[] d) {
                return ifaceA.getIP();
            }
            @Override
            public IPv4 extractSourceFrom(Class<IPv4Protocol> c, byte[] d) {
                return ifaceA.getIP();
            }
        };
        server.receive(rob, new byte[]{1,2,3});
    }

    //–– receive(…) happy‐path invokes app.receive(...) –––––––––––––––––––––––––––—

    @Test
    public void receiveInvokesAppReceiveWhenDestMatches() {
        byte[] rawData = {42,43,44};
        // pipeline that yields ifaceA IP and a known source
        final IPv4 srcIP = new IPv4("10.0.0.99", 24);
        ProtocolPipeline rob = new ProtocolPipeline(Collections.singletonList(new StubDLL())) {
            @Override
            public IPv4 extractDestinationFrom(Class<IPv4Protocol> c, byte[] d) {
                return ifaceA.getIP();
            }
            @Override
            public IPv4 extractSourceFrom(Class<IPv4Protocol> c, byte[] d) {
                return srcIP;
            }
        };

        server.receive(rob, rawData);
        assertEquals("source passed", srcIP, app.lastSource);
        assertArrayEquals("payload passed", rawData, app.lastData);
    }
}
