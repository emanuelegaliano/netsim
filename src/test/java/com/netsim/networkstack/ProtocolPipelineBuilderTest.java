package com.netsim.networkstack;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Test;

import com.netsim.addresses.Address;

/**
 * Unit tests for ProtocolPipelineBuilder.
 */
public class ProtocolPipelineBuilderTest {

    /**
     * Dummy Protocol that records calls to encapsulate()/decapsulate().
     */
    private static class DummyProtocol implements Protocol {
        private boolean encapsulateCalled = false;
        private boolean decapsulateCalled = false;

        public byte[] encapsulate(byte[] pdu) {
            encapsulateCalled = true;
            return pdu;
        }

        public byte[] decapsulate(byte[] pdu) {
            decapsulateCalled = true;
            return pdu;
        }

        public void setNext(Protocol next) {}
        public void setPrevious(Protocol prev) {}

        public Address getSource() { return null; }
        public Address getDestination() { return null; }

        public Address extractSource(byte[] pdu) { return null; }
        public Address extractDestination(byte[] pdu) { return null; }

        public boolean wasEncapsulateCalled() { return encapsulateCalled; }
        public boolean wasDecapsulateCalled() { return decapsulateCalled; }
    }

    /** 
     * Builder starts with exactly one IdentityProtocol. 
     */
    @Test
    public void constructor_initializesWithIdentityOnly() throws Exception {
        ProtocolPipelineBuilder b = new ProtocolPipelineBuilder();
        Field f = ProtocolPipelineBuilder.class.getDeclaredField("protocols");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Protocol> list = (List<Protocol>) f.get(b);

        assertEquals(1, list.size());
        assertTrue(list.get(0) instanceof IdentityProtocol);
    }

    /** addProtocol(null) must throw. */
    @Test(expected = IllegalArgumentException.class)
    public void addProtocol_null_throws() {
        new ProtocolPipelineBuilder().addProtocol(null);
    }

    /** addProtocol(p) appends p after the initial identity. */
    @Test
    public void addProtocol_appends() throws Exception {
        ProtocolPipelineBuilder b = new ProtocolPipelineBuilder();
        DummyProtocol p = new DummyProtocol();
        b.addProtocol(p);

        Field f = ProtocolPipelineBuilder.class.getDeclaredField("protocols");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Protocol> list = (List<Protocol>) f.get(b);

        assertEquals(2, list.size());
        assertTrue(list.get(0) instanceof IdentityProtocol);
        assertSame(p, list.get(1));
    }

    /** build().encapsulate(...) should call encapsulate on the added protocol. */
    @Test
    public void build_encapsulateInvokesAddedProtocol() {
        ProtocolPipelineBuilder b = new ProtocolPipelineBuilder();
        DummyProtocol p = new DummyProtocol();
        b.addProtocol(p);
        ProtocolPipeline pipeline = b.build();

        byte[] data = {0x01, 0x02};
        byte[] out = pipeline.encapsulate(data);

        assertTrue("Added protocol should have been invoked", p.wasEncapsulateCalled());
        assertArrayEquals(data, out);
    }

    /** build().decapsulate(...) should call decapsulate on the last protocol in list (the added one). */
    @Test
    public void build_decapsulateInvokesAddedProtocol() throws Exception {
        ProtocolPipelineBuilder b = new ProtocolPipelineBuilder();
        DummyProtocol p = new DummyProtocol();
        b.addProtocol(p);
        ProtocolPipeline pipeline = b.build();

        byte[] data = {0x0A};
        byte[] back = pipeline.decapsulate(data);

        assertTrue("Added protocol should have been invoked for decapsulate", p.wasDecapsulateCalled());
        assertArrayEquals(data, back);
    }
}