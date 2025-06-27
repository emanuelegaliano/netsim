package com.netsim.network.router;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;
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
    private IPv4             knownDest;
    private IPv4             knownSrc;
    private byte[]           payload;
    private RoutingInfo      stubRoute;
    private ProtocolPipeline pipelineStub;
    private List<Interface>  ifaces;
    private ArpTable         arpDummy;

    @Before
    public void setUp() {
        knownDest = new IPv4("10.0.0.42", 24);
        knownSrc  = new IPv4("10.0.0.1", 24);
        payload   = new byte[]{0x01, 0x02, 0x03};

        NetworkAdapter adapter = new NetworkAdapter("eth0", 1500, Mac.broadcast());
        Interface iface = new Interface(adapter, knownDest);
        ifaces = Collections.singletonList(iface);
        stubRoute = new RoutingInfo(adapter, knownDest);
        arpDummy   = new ArpTable();

        IPv4Protocol stubIp = new IPv4Protocol(knownSrc, knownDest, 5, 0, 0, 0, 64, 0, 1500) {
            @Override public IPv4 extractDestination(byte[] pdu) { return knownDest; }
            @Override public IPv4 extractSource(byte[] pdu)      { return knownSrc; }
            @Override public byte[] decapsulate(byte[] pdu)       { return pdu; }
        };
        SimpleDLLProtocol stubDll = new SimpleDLLProtocol(Mac.broadcast(), Mac.broadcast()) {
            @Override public byte[] decapsulate(byte[] frame) { return frame; }
        };

        pipelineStub = new ProtocolPipeline();
        pipelineStub.push(stubIp);
        pipelineStub.push(stubDll);
    }

    // send(...) argument validation

    @Test(expected = IllegalArgumentException.class)
    public void sendNullRouteThrows() {
        new Router("R", rtReturnsAny(), arpDummy, ifaces)
            .send(null, pipelineStub, payload);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendNullPipelineThrows() {
        new Router("R", rtReturnsAny(), arpDummy, ifaces)
            .send(stubRoute, null, payload);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendNullDataThrows() {
        new Router("R", rtReturnsAny(), arpDummy, ifaces)
            .send(stubRoute, pipelineStub, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendEmptyDataThrows() {
        new Router("R", rtReturnsAny(), arpDummy, ifaces)
            .send(stubRoute, pipelineStub, new byte[0]);
    }

    // receive(...) argument validation

    @Test(expected = IllegalArgumentException.class)
    public void receiveNullPipelineThrows() {
        new Router("R", rtReturnsAny(), arpDummy, ifaces)
            .receive(null, payload);
    }

    @Test(expected = IllegalArgumentException.class)
    public void receiveNullDataThrows() {
        new Router("R", rtReturnsAny(), arpDummy, ifaces)
            .receive(pipelineStub, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void receiveEmptyDataThrows() {
        new Router("R", rtReturnsAny(), arpDummy, ifaces)
            .receive(pipelineStub, new byte[0]);
    }

    // receive(...) → known destination invokes send(...)

    @Test
    public void receiveKnownDestinationInvokesSend() {
        AtomicBoolean called = new AtomicBoolean(false);
        Router r = new Router("R", rtReturns(stubRoute), arpDummy, ifaces) {
            @Override
            public void send(RoutingInfo route, ProtocolPipeline stack, byte[] data) {
                called.set(true);
                assertSame("route should be stubRoute", stubRoute, route);
                assertSame("pipeline should be pipelineStub", pipelineStub, stack);
                assertArrayEquals("payload should be unchanged", payload, data);
            }
        };

        r.receive(pipelineStub, payload);
        assertTrue("send(...) must be invoked for known destination", called.get());
    }

    // receive(...) → lookup throws NPE, packet dropped silently

    @Test
    public void receiveUnknownDestinationDropsSilently() {
        AtomicBoolean called = new AtomicBoolean(false);
        Router r = new Router("R", rtThrowsNpe(), arpDummy, ifaces) {
            @Override
            public void send(RoutingInfo route, ProtocolPipeline stack, byte[] data) {
                called.set(true);
            }
        };

        r.receive(pipelineStub, payload);
        assertFalse("send(...) should not be invoked when no route", called.get());
    }

    // helper stubs

    private static RoutingTable rtReturnsAny() {
        return new RoutingTable() {
            @Override public RoutingInfo lookup(IPv4 dest) {
                throw new AssertionError("lookup should not be called");
            }
        };
    }

    private static RoutingTable rtReturns(final RoutingInfo info) {
        return new RoutingTable() {
            @Override public RoutingInfo lookup(IPv4 dest) {
                return info;
            }
        };
    }

    private static RoutingTable rtThrowsNpe() {
        return new RoutingTable() {
            @Override public RoutingInfo lookup(IPv4 dest) {
                throw new NullPointerException("no route");
            }
        };
    }
}