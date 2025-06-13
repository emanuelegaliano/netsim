package com.netsim.networkstack;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.Address;

import java.util.*;

/**
 * Unit tests for ProtocolPipeline.
 */
public class ProtocolPipelineTest {
    private byte[] sampleData;

    /** A minimal spy‐protocol that records whether its methods were invoked. */
    private static class SpyProtocol implements Protocol {
        boolean enc = false, dec = false;

        @Override public byte[] encapsulate(byte[] pdu) {
            enc = true; return pdu;
        }
        @Override public byte[] decapsulate(byte[] pdu) {
            dec = true; return pdu;
        }
        @Override public void setNext(Protocol next)        { /* no‐op */ }
        @Override public void setPrevious(Protocol prev)    { /* no‐op */ }
        @Override public Address getSource()                { return null; }
        @Override public Address getDestination()           { return null; }
        @Override public Address extractSource(byte[] pdu)  { return null; }
        @Override public Address extractDestination(byte[] pdu) { return null; }
        /** Required by getProtocolAt() in your implementation */
        public SpyProtocol copy() { return new SpyProtocol(); }
    }

    private static class FakeProtocol implements Protocol {
        @Override public byte[] encapsulate(byte[] pdu)          { return pdu; }
        @Override public byte[] decapsulate(byte[] pdu)         { return pdu; }
        @Override public void setNext(Protocol next)            { }
        @Override public void setPrevious(Protocol prev)        { }
        @Override public Address getSource()                    { return null; }
        @Override public Address getDestination()               { return null; }
        @Override public Address extractSource(byte[] pdu)      { return null; }
        @Override public Address extractDestination(byte[] pdu) { return null; }
        @Override public FakeProtocol copy() { return null; }
    }

    @Before
    public void setUp() {
        sampleData = new byte[] { 0x01, 0x02, 0x03 };
    }

    //–– constructor tests –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullList() {
        new ProtocolPipeline(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsEmptyList() {
        new ProtocolPipeline(Collections.<Protocol>emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullElement() {
        List<Protocol> list = Arrays.asList(new SpyProtocol(), null);
        new ProtocolPipeline(list);
    }

    //–– size() tests ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

    @Test
    public void sizeSingleProtocol() {
        ProtocolPipeline pp = new ProtocolPipeline(
            Collections.singletonList(new SpyProtocol())
        );
        assertEquals(1, pp.size());
    }

    @Test
    public void sizeMultipleProtocols() {
        ProtocolPipeline pp = new ProtocolPipeline(
            Arrays.asList(new SpyProtocol(), new SpyProtocol(), new SpyProtocol())
        );
        assertEquals(3, pp.size());
    }

    //–– encapsulate(...) tests ––––––––––––––––––––––––––––––––––––––––––––––––––––––––

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateRejectsNullData() {
        ProtocolPipeline pp = new ProtocolPipeline(
            Collections.singletonList(new SpyProtocol())
        );
        pp.encapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateRejectsEmptyData() {
        ProtocolPipeline pp = new ProtocolPipeline(
            Collections.singletonList(new SpyProtocol())
        );
        pp.encapsulate(new byte[0]);
    }

    @Test
    public void encapsulateInvokesFirstReal() {
        SpyProtocol spy = new SpyProtocol();
        ProtocolPipeline pp = new ProtocolPipeline(
            Collections.singletonList(spy)
        );
        byte[] out = pp.encapsulate(sampleData);
        assertTrue("first real protocol must be called", spy.enc);
        assertArrayEquals("encapsulate must return input when spy echoes", sampleData, out);
    }

    //–– decapsulate(...) tests ––––––––––––––––––––––––––––––––––––––––––––––––––––––––

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsNullData() {
        ProtocolPipeline pp = new ProtocolPipeline(
            Collections.singletonList(new SpyProtocol())
        );
        pp.decapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsEmptyData() {
        ProtocolPipeline pp = new ProtocolPipeline(
            Collections.singletonList(new SpyProtocol())
        );
        pp.decapsulate(new byte[0]);
    }

    @Test
    public void decapsulateInvokesOnlyIdentity() {
        SpyProtocol spy = new SpyProtocol();
        ProtocolPipeline pp = new ProtocolPipeline(
            Collections.singletonList(spy)
        );
        byte[] out = pp.decapsulate(sampleData);
        assertFalse("real protocol must NOT see decapsulate", spy.dec);
        assertArrayEquals("decapsulate must return input unchanged", sampleData, out);
    }

    //–– getProtocolAt(int) tests ––––––––––––––––––––––––––––––––––––––––––––––––––––––

    @Test
    public void getProtocolAtValidIndexes() {
        SpyProtocol a = new SpyProtocol();
        SpyProtocol b = new SpyProtocol();
        ProtocolPipeline pp = new ProtocolPipeline(Arrays.asList(a, b));

        Protocol p0 = pp.getProtocolAt(0);
        Protocol p1 = pp.getProtocolAt(1);

        assertTrue(p0 instanceof SpyProtocol);
        assertTrue(p1 instanceof SpyProtocol);
        assertNotSame(a, p0);
        assertNotSame(b, p1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProtocolAtNegativeIndex() {
        ProtocolPipeline pp = new ProtocolPipeline(
            Collections.singletonList(new SpyProtocol())
        );
        pp.getProtocolAt(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProtocolAtTooLargeIndex() {
        ProtocolPipeline pp = new ProtocolPipeline(
            Collections.singletonList(new SpyProtocol())
        );
        pp.getProtocolAt(1);
    }

    //–– getProtocolsRange(int) tests ––––––––––––––––––––––––––––––––––––––––––––––––––

    @Test
    public void getProtocolsRangeHappyPath() {
        SpyProtocol a = new SpyProtocol();
        SpyProtocol b = new SpyProtocol();
        SpyProtocol c = new SpyProtocol();
        ProtocolPipeline pp = new ProtocolPipeline(Arrays.asList(a, b, c));

        // take only first two real protocols [a,b]
        ProtocolPipeline sub = pp.getProtocolsRange(2);

        // sub.encapsulate invokes sub’s first real → that is a
        byte[] out = sub.encapsulate(sampleData);
        assertTrue("sub‐pipeline must call a", a.enc);
        assertFalse("b must not be called when only range=2", b.enc);
        assertArrayEquals(sampleData, out);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProtocolsRangeRejectsZero() {
        ProtocolPipeline pp = new ProtocolPipeline(
            Collections.singletonList(new SpyProtocol())
        );
        pp.getProtocolsRange(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProtocolsRangeRejectsTooLarge() {
        ProtocolPipeline pp = new ProtocolPipeline(
            Collections.singletonList(new SpyProtocol())
        );
        pp.getProtocolsRange(2);
    }


    @Test
    public void getProtocolByClassReturnsCorrectInstance() {
        SpyProtocol a = new SpyProtocol();
        SpyProtocol b = new SpyProtocol();
        ProtocolPipeline pp = new ProtocolPipeline(Arrays.asList(a, b));

        // deve restituire proprio 'a', essendo il primo SpyProtocol nella catena
        SpyProtocol found = pp.getProtocolByClass(SpyProtocol.class);
        assertSame("Should return the very same SpyProtocol instance", a, found);
    }

    @Test(expected = RuntimeException.class)
    public void getProtocolByClassThrowsWhenMissing() {
        SpyProtocol a = new SpyProtocol();
        ProtocolPipeline pp = new ProtocolPipeline(Collections.singletonList(a));

        // FakeProtocol NON è nella catena → RuntimeException
        pp.getProtocolByClass(FakeProtocol.class);
    }
}