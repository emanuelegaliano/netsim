package com.netsim.networkstack;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.netsim.addresses.Address;

/**
 * Unit tests for ProtocolPipeline.
 */
public class ProtocolPipelineTest {
    private byte[] sampleData;

    /**
     * A minimal Protocol stub that records whether its methods were invoked.
     */
    private static class SpyProtocol implements Protocol {
        boolean encapsulateCalled = false;
        boolean decapsulateCalled = false;

        public byte[] encapsulate(byte[] pdu) {
            encapsulateCalled = true;
            return pdu;
        }

        public byte[] decapsulate(byte[] pdu) {
            decapsulateCalled = true;
            return pdu;
        }

        public Address getSource() { return null; }
        public Address getDestination() { return null; }

        public void setNext(Protocol next) { /* no-op */ }
        public void setPrevious(Protocol prev) { /* no-op */ }

        public Address extractSource(byte[] pdu) {
            return null;
        }

        public Address extractDestination(byte[] pdu) {
            return null;
        }
    }

    @Before
    public void setUp() {
        sampleData = new byte[] { 0x01, 0x02, 0x03 };
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullList() {
        new ProtocolPipeline(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsEmptyList() {
        new ProtocolPipeline(Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateRejectsNullData() {
        List<Protocol> list = Arrays.asList(new IdentityProtocol(), new SpyProtocol());
        ProtocolPipeline pipeline = new ProtocolPipeline(list);
        pipeline.encapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateRejectsEmptyData() {
        List<Protocol> list = Arrays.asList(new IdentityProtocol(), new SpyProtocol());
        ProtocolPipeline pipeline = new ProtocolPipeline(list);
        pipeline.encapsulate(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsNullData() {
        List<Protocol> list = Arrays.asList(new IdentityProtocol(), new SpyProtocol());
        ProtocolPipeline pipeline = new ProtocolPipeline(list);
        pipeline.decapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsEmptyData() {
        List<Protocol> list = Arrays.asList(new IdentityProtocol(), new SpyProtocol());
        ProtocolPipeline pipeline = new ProtocolPipeline(list);
        pipeline.decapsulate(new byte[0]);
    }

    @Test
    public void encapsulateInvokesSecondProtocol() {
        SpyProtocol spy = new SpyProtocol();
        List<Protocol> list = Arrays.asList(new IdentityProtocol(), spy);
        ProtocolPipeline pipeline = new ProtocolPipeline(list);

        byte[] out = pipeline.encapsulate(sampleData);

        assertTrue("Second protocol should handle encapsulate", spy.encapsulateCalled);
        assertArrayEquals("Output should equal input", sampleData, out);
    }

    @Test
    public void decapsulateInvokesLastProtocol() {
        SpyProtocol spy1 = new SpyProtocol();
        SpyProtocol spy2 = new SpyProtocol();
        List<Protocol> list = Arrays.asList(new IdentityProtocol(), spy1, spy2);
        ProtocolPipeline pipeline = new ProtocolPipeline(list);

        byte[] out = pipeline.decapsulate(sampleData);

        assertTrue("Last protocol should handle decapsulate", spy2.decapsulateCalled);
        assertFalse("Middle protocol should not be called on decapsulate", spy1.decapsulateCalled);
        assertArrayEquals("Output should equal input", sampleData, out);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void encapsulateWithSingleProtocolThrows() {
        // Only identity in list → no protocol at index 1
        ProtocolPipeline pipeline = new ProtocolPipeline(
            Collections.singletonList(new IdentityProtocol())
        );
        pipeline.encapsulate(sampleData);
    }

    @Test
    public void decapsulateWithSingleProtocolPassesThrough() {
        // Only identity in list → decapsulate returns data unchanged
        ProtocolPipeline pipeline = new ProtocolPipeline(
            Collections.singletonList(new IdentityProtocol())
        );
        byte[] out = pipeline.decapsulate(sampleData);
        assertArrayEquals("With only identity, decapsulate should pass through", sampleData, out);
    }
}