package com.netsim.network;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.Address;
import com.netsim.addresses.Mac;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.table.RoutingInfo;

public class NetworkAdapterTest {
    private NetworkAdapter adapter;
    private NetworkAdapter remote;
    private Mac macA;
    private Mac macB;

    /**
     * A Protocol stub that always returns the same MAC as its destination
     * and echoes the input bytes unchanged.  Also provides a copy() method.
     */
    private static class StubProtocol implements Protocol {
        private final Mac macToReturn;
        StubProtocol(Mac mac) { this.macToReturn = mac; }
        public byte[] encapsulate(byte[] pdu) { return pdu; }
        public byte[] decapsulate(byte[] pdu) { return pdu; }
        public void setNext(Protocol next) {}
        public void setPrevious(Protocol prev) {}
        public Address getSource() { return null; }
        public Address getDestination() { return null; }
        public Address extractSource(byte[] pdu) { return null; }
        public Address extractDestination(byte[] pdu) { return macToReturn; }
        // required by tests
        public Protocol copy() { return new StubProtocol(macToReturn); }
    }

    @Before
    public void setUp() {
        macA = Mac.bytesToMac(new byte[]{1,1,1,1,1,1});
        macB = Mac.bytesToMac(new byte[]{2,2,2,2,2,2});
        adapter = new NetworkAdapter("eth0", 5, macA);
        remote  = new NetworkAdapter("eth1", 5, macB);
    }

    // ------------------------------------------------------------------------
    // Constructor and basic getters/setters
    // ------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullName() {
        new NetworkAdapter(null, 1500, macA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullMac() {
        new NetworkAdapter("ethX", 1500, null);
    }

    @Test
    public void gettersWork() {
        assertEquals("eth0", adapter.getName());
        assertEquals(5, adapter.getMTU());
        assertSame(macA, adapter.getMacAddress());
        assertTrue(adapter.isUp());
    }

    @Test
    public void setUpDown() {
        adapter.setDown();
        assertFalse(adapter.isUp());
        adapter.setUp();
        assertTrue(adapter.isUp());
    }

    // ------------------------------------------------------------------------
    // Owner (Node) of the adapter
    // ------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void setOwnerRejectsNull() {
        adapter.setOwner(null);
    }

    @Test(expected = NullPointerException.class)
    public void getNodeWithoutOwnerThrows() {
        adapter.getNode();
    }

    @Test
    public void setAndGetOwner() {
        Node dummy = new Node() {
            public void send(RoutingInfo r, ProtocolPipeline p, byte[] d) {}
            public void receive(ProtocolPipeline p, byte[] d) {}
        };
        adapter.setOwner(dummy);
        assertSame(dummy, adapter.getNode());
    }

    // ------------------------------------------------------------------------
    // Remote adapter (point-to-point link)
    // ------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void setRemoteRejectsNull() {
        adapter.setRemoteAdapter(null);
    }

    @Test(expected = NullPointerException.class)
    public void getLinkedAdapterWithoutSetThrows() {
        adapter.getLinkedAdapter();
    }

    @Test
    public void setAndGetLinkedAdapter() {
        adapter.setRemoteAdapter(remote);
        assertSame(remote, adapter.getLinkedAdapter());
    }

    // ------------------------------------------------------------------------
    // collectFrames, sendFrames, and releaseFrames
    // ------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void collectFramesRejectsNullProtocol() {
        adapter.collectFrames(null, new byte[]{0x00});
    }

    @Test(expected = IllegalArgumentException.class)
    public void collectFramesRejectsNullData() {
        adapter.collectFrames(new StubProtocol(macB), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void collectFramesRejectsEmptyData() {
        adapter.collectFrames(new StubProtocol(macB), new byte[0]);
    }

    @Test(expected = RuntimeException.class)
    public void collectFramesWhenDownThrows() {
        adapter.setDown();
        adapter.collectFrames(new StubProtocol(macB), new byte[]{0x01});
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendFramesWithoutRemoteThrows() {
        // no collect, and remote not set
        adapter.sendFrames(new StubProtocol(macB));
    }

    @Test(expected = RuntimeException.class)
    public void sendFramesWhenDownThrows() {
        adapter.setRemoteAdapter(remote);
        adapter.setDown();
        adapter.sendFrames(new StubProtocol(macB));
    }

    @Test(expected = RuntimeException.class)
    public void sendFramesWhenNothingToSendThrows() {
        adapter.setRemoteAdapter(remote);
        adapter.sendFrames(new StubProtocol(macB));
    }

    @Test(expected = IllegalArgumentException.class)
    public void releaseFramesRejectsNullProtocol() {
        adapter.releaseFrames(null);
    }

    @Test(expected = RuntimeException.class)
    public void releaseFramesWhenEmptyThrows() {
        adapter.releaseFrames(new StubProtocol(macB));
    }

    @Test
    public void collectSendAndReleaseSingleFragment() {
        // link A→B and B→A
        adapter.setRemoteAdapter(remote);
        remote.setRemoteAdapter(adapter);

        byte[] data = new byte[]{10,20,30};
        StubProtocol p = new StubProtocol(macB);

        adapter.collectFrames(p, data);
        adapter.sendFrames(p);
        byte[] out = remote.releaseFrames(p);

        assertArrayEquals(data, out);
    }

    @Test
    public void collectSendAndReleaseMultipleFragments() {
        // MTU=5, data length=12 ⇒ fragments of size 5,5,2
        adapter  = new NetworkAdapter("eth0", 5, macA);
        remote   = new NetworkAdapter("eth1", 5, macB);
        adapter.setRemoteAdapter(remote);
        remote.setRemoteAdapter(adapter);

        byte[] data = new byte[12];
        for(int i = 0; i < 12; i++) data[i] = (byte)i;
        StubProtocol p = new StubProtocol(macB);

        adapter.collectFrames(p, data);
        adapter.sendFrames(p);
        byte[] out = remote.releaseFrames(p);

        assertArrayEquals(data, out);
    }

    @Test(expected = RuntimeException.class)
    public void collectFramesFiltersWrongDestination() {
        adapter.setRemoteAdapter(remote);
        // protocol that always returns macA ⇒ should collect nothing
        StubProtocol wrong = new StubProtocol(macA);
        byte[] data = new byte[]{1,2,3,4,5};
        adapter.collectFrames(wrong, data);
        adapter.sendFrames(wrong);
        fail("No frames collected, should have thrown RuntimeException");
    }

    // ------------------------------------------------------------------------
    // receiveFrame
    // ------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void receiveFrameRejectsNullProtocol() {
        adapter.receiveFrame(null, new byte[12]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void receiveFrameRejectsNullFrame() {
        adapter.receiveFrame(new StubProtocol(macA), null);
    }

    @Test(expected = RuntimeException.class)
    public void receiveFrameWhenDownThrows() {
        adapter.setDown();
        adapter.receiveFrame(new StubProtocol(macA), new byte[12]);
    }

    @Test
    public void receiveFrameAcceptsOwnAndBroadcast() {
        byte[] frame = new byte[12];
        StubProtocol pOwn   = new StubProtocol(macA);
        StubProtocol pBcast = new StubProtocol(Mac.broadcast());
        // collect two copies in internal buffer
        adapter.receiveFrame(pOwn,   frame);
        adapter.receiveFrame(pBcast, frame);
        // now release both
        byte[] out = adapter.releaseFrames(pOwn);
        assertEquals(24, out.length);
        assertArrayEquals(
            Arrays.copyOfRange(out,  0, 12),
            Arrays.copyOfRange(out, 12, 24)
        );
    }

    // ------------------------------------------------------------------------
    // equals() / hashCode()
    // ------------------------------------------------------------------------

    @Test
    public void equalsAndHashCodeBasedOnMac() {
        NetworkAdapter a1 = new NetworkAdapter("x", 150, macA);
        NetworkAdapter a2 = new NetworkAdapter("y", 200, macA);
        NetworkAdapter a3 = new NetworkAdapter("z", 150, macB);
        assertTrue( a1.equals(a2) );
        assertEquals( a1.hashCode(), a2.hashCode() );
        assertFalse( a1.equals(a3) );
        assertFalse( a1.equals(null) );
        assertFalse( a1.equals("something") );
    }
}