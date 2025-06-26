package com.netsim.network.router;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.Address;
import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.network.NetworkAdapter;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.IPv4.IPv4Protocol;
import com.netsim.protocols.SimpleDLL.SimpleDLLProtocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;

/**
 * Tests for {@link Router}, covering argument validation and correct forwarding behavior.
 */
public class RouterTest {
    private IPv4 knownDest;
    private byte[] payload;
    private RoutingInfo stubRoute;
    private ProtocolPipeline pipelineStub;
    private ArpTable arpDummy;

    @Before
    public void setUp() {
        // a "real" IPv4 we’ll use as the extracted destination
        knownDest = new IPv4("10.0.0.42", 24);
        payload   = new byte[]{0x01,0x02,0x03};

        // build a real RoutingInfo (device + nextHop)
        NetworkAdapter dummyAdapter = new NetworkAdapter("eth0", 1500, Mac.broadcast());
        stubRoute = new RoutingInfo(dummyAdapter, knownDest);

        // an ARP table is not used in Router
        arpDummy = new ArpTable();

        // stub out a SimpleDLLProtocol for the pipeline
        final SimpleDLLProtocol dll = new SimpleDLLProtocol(Mac.broadcast(), Mac.broadcast());
        pipelineStub = new ProtocolPipeline(Collections.singletonList(dll)) {
            @Override
            public <T extends Protocol> T getProtocolByClass(Class<T> clazz) {
                // Router.send() only ever asks for SimpleDLLProtocol.class
                assertEquals("Router should ask for exactly SimpleDLLProtocol",
                             SimpleDLLProtocol.class, clazz);
                // use Class.cast to avoid unchecked warning
                return clazz.cast(dll);
            }

            @Override
            public <T extends Protocol> Address extractDestinationFrom(Class<T> clazz, byte[] raw) {
                // Router.receive() only ever asks for IPv4Protocol.class
                assertEquals("Router should ask for exactly IPv4Protocol",
                             IPv4Protocol.class, clazz);
                return knownDest;
            }
        };
    }

    // ------------------------------------------------------
    // send(...) argument-validation
    // ------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void send_nullRoute_throws() {
        new Router("R", rtReturnsAny(), arpDummy, Collections.emptyList())
            .send(null, pipelineStub, payload);
    }

    @Test(expected = IllegalArgumentException.class)
    public void send_nullPipeline_throws() {
        new Router("R", rtReturnsAny(), arpDummy, Collections.emptyList())
            .send(stubRoute, null, payload);
    }

    @Test(expected = IllegalArgumentException.class)
    public void send_nullData_throws() {
        new Router("R", rtReturnsAny(), arpDummy, Collections.emptyList())
            .send(stubRoute, pipelineStub, null);
    }

    // ------------------------------------------------------
    // receive(...) argument-validation
    // ------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void receive_nullPipeline_throws() {
        new Router("R", rtReturnsAny(), arpDummy, Collections.emptyList())
            .receive(null, payload);
    }

    @Test(expected = IllegalArgumentException.class)
    public void receive_nullData_throws() {
        new Router("R", rtReturnsAny(), arpDummy, Collections.emptyList())
            .receive(pipelineStub, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void receive_emptyData_throws() {
        new Router("R", rtReturnsAny(), arpDummy, Collections.emptyList())
            .receive(pipelineStub, new byte[0]);
    }

    // ------------------------------------------------------
    // receive(...) -> known destination invokes send(...)
    // ------------------------------------------------------

    @Test
    public void receive_knownDestination_invokesSend() {
        final AtomicBoolean sendCalled = new AtomicBoolean(false);
        Router r = new Router("R", rtReturns(stubRoute), arpDummy, Collections.emptyList()) {
            @Override
            public void send(RoutingInfo route, ProtocolPipeline protos, byte[] data) {
                sendCalled.set(true);
                assertSame("route passed through should be exactly stubRoute",
                           stubRoute, route);
                assertSame("pipeline passed through should be exactly our stub",
                           pipelineStub, protos);
                assertArrayEquals("payload should be passed unchanged",
                                  payload, data);
            }
        };

        r.receive(pipelineStub, payload);
        assertTrue("Router.receive must call send(...) when lookup succeeds",
                   sendCalled.get());
    }

    // ------------------------------------------------------
    // receive(...) -> lookup throws NPE, packet is dropped
    // ------------------------------------------------------

    @Test
    public void receive_unknownDestination_dropsSilently() {
        final AtomicBoolean sendCalled = new AtomicBoolean(false);
        Router r = new Router("R", rtThrowsNPE(), arpDummy, Collections.emptyList()) {
            @Override
            public void send(RoutingInfo route, ProtocolPipeline protos, byte[] data) {
                sendCalled.set(true);
            }
        };

        // should not throw, and send() should never be invoked
        r.receive(pipelineStub, payload);
        assertFalse("Router.receive should not call send(...) when lookup throws",
                    sendCalled.get());
    }

    // ------------------------------------------------------
    // helper RoutingTable stubs
    // ------------------------------------------------------

    /** never used by send(), so throws if called */
    private static RoutingTable rtReturnsAny() {
        return new RoutingTable() {
            @Override
            public RoutingInfo lookup(IPv4 dest) {
                throw new AssertionError("should not be called in this test");
            }
        };
    }

    /** always return the given RoutingInfo */
    private static RoutingTable rtReturns(final RoutingInfo info) {
        return new RoutingTable() {
            @Override
            public RoutingInfo lookup(IPv4 dest) {
                return info;
            }
        };
    }

    /** always throw NPE to simulate "no route" */
    private static RoutingTable rtThrowsNPE() {
        return new RoutingTable() {
            @Override
            public RoutingInfo lookup(IPv4 dest) {
                throw new NullPointerException("no route");
            }
        };
    }
}