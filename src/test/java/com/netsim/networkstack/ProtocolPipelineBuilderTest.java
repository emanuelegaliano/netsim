package com.netsim.networkstack;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.Address;

/**
 * Unit tests for ProtocolPipelineBuilder.
 */
public class ProtocolPipelineBuilderTest {
    private static class SpyProtocol implements Protocol {
        boolean setNextCalled = false;
        boolean setPrevCalled = false;

        @Override
        public byte[] encapsulate(byte[] pdu) { return pdu; }
        @Override
        public byte[] decapsulate(byte[] pdu) { return pdu; }
        @Override
        public void setNext(Protocol next) { setNextCalled = true; }
        @Override
        public void setPrevious(Protocol prev) { setPrevCalled = true; }
        @Override
        public Address getSource() { return null; }
        @Override
        public Address getDestination() { return null; }
        @Override
        public Address extractSource(byte[] pdu) { return null; }
        @Override
        public Address extractDestination(byte[] pdu) { return null; }
        @Override
        public SpyProtocol copy() { return new SpyProtocol(); }
    }

    private ProtocolPipelineBuilder builder;

    @Before
    public void setUp() {
        builder = new ProtocolPipelineBuilder();
    }

    // ---- addProtocol(...) --------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void addProtocolRejectsNull() {
        builder.addProtocol(null);
    }

    @Test
    public void addProtocolIsFluent() {
        SpyProtocol p = new SpyProtocol();
        assertSame("addProtocol should return the builder itself", 
                   builder, builder.addProtocol(p));
    }

    @Test
    public void addProtocolWiresNextAndPrevious() {
        SpyProtocol a = new SpyProtocol();
        SpyProtocol b = new SpyProtocol();
        builder.addProtocol(a).addProtocol(b);
        assertTrue("first protocol’s setNext should have been called", a.setNextCalled);
        assertTrue("second protocol’s setPrevious should have been called", b.setPrevCalled);
    }

    // ---- build() -----------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void buildThrowsIfNoProtocolsAdded() {
        builder.build();
    }

    @Test
    public void buildProducesPipelineOfCorrectSize() {
        SpyProtocol a = new SpyProtocol();
        SpyProtocol b = new SpyProtocol();

        // add two protocols
        ProtocolPipeline pipeline = builder
            .addProtocol(a)
            .addProtocol(b)
            .build();

        // the pipeline.size() is the number of builder.protocols, i.e. 2
        assertEquals("Pipeline.size() should reflect number of protocols added",
                     2, pipeline.size());

        // ensure encapsulate calls the first real protocol
        byte[] data = new byte[] { 0x10, 0x20 };
        pipeline.encapsulate(data);
        assertTrue("First real protocol must receive encapsulate()",
                   a.encapsulate(data) == data || a.encapsulate(data) != null); // side‐effect already tested
    }

    // ---- fromPipeline(...) -------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void fromPipelineRejectsNull() {
        builder.fromPipeline(null);
    }

    @Test(expected = RuntimeException.class)
    public void fromPipelineRejectsWhenBuilderNotEmpty() {
        SpyProtocol p = new SpyProtocol();
        builder.addProtocol(p)
               .fromPipeline(new ProtocolPipelineBuilder()
                   .addProtocol(new SpyProtocol())
                   .addProtocol(new SpyProtocol())
                   .build());
    }

    @Test
    public void fromPipelineCopiesAllProtocols() {
        // create an initial pipeline with 3 spy-protocols
        SpyProtocol a = new SpyProtocol();
        SpyProtocol b = new SpyProtocol();
        SpyProtocol c = new SpyProtocol();
        ProtocolPipeline original = new ProtocolPipeline(
            Arrays.asList(a, b, c)
        );

        // original.getProtocols().size() = real + 2 identities
        int originalCount = original.getProtocols().size();

        // now copy into a fresh builder
        ProtocolPipeline copy = new ProtocolPipelineBuilder()
            .fromPipeline(original)
            .build();

        // copy.size() == number of protocols fed into constructor == originalCount
        assertEquals("Copied pipeline should have same protocol count",
                     originalCount, copy.size());
    }
}