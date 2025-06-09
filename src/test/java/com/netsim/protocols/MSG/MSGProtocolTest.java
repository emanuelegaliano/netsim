package com.netsim.protocols.MSG;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.IdentityProtocol;
import com.netsim.addresses.Address;

public class MSGProtocolTest {
    private MSGProtocol proto;
    private byte[] payload;

    @Before
    public void setUp() {
        proto = new MSGProtocol("App");
        payload = "hello".getBytes(StandardCharsets.UTF_8);
    }

    // —— Constructor tests —— //

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullName() {
        new MSGProtocol(null);
    }

    // —— encapsulate(...) tests —— //

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateRejectsNullPayload() {
        proto.encapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateRejectsEmptyPayload() {
        proto.encapsulate(new byte[0]);
    }

    @Test
    public void encapsulateWithoutNextProducesHeader() {
        byte[] out = proto.encapsulate(payload);
        String s = new String(out, StandardCharsets.UTF_8);
        assertEquals("App: hello", s);
    }

    @Test
    public void encapsulateForwardsToNextProtocol() {
        // stub next protocol that upper‐prepends "NEXT:" then returns
        Protocol stubNext = new Protocol() {
            @Override public byte[] encapsulate(byte[] pdu) {
                String in = new String(pdu, StandardCharsets.UTF_8);
                return ("NEXT:" + in).getBytes(StandardCharsets.UTF_8);
            }
            @Override public byte[] decapsulate(byte[] pdu) { return pdu; }
            @Override public void setNext(Protocol next) { }
            @Override public void setPrevious(Protocol prev) { }
            @Override public Address getSource() { return null; }
            @Override public Address getDestination() { return null; }
            @Override public Address extractSource(byte[] pdu) { return null; }
            @Override public Address extractDestination(byte[] pdu) { return null; }
        };
        proto.setNext(stubNext);

        byte[] out = proto.encapsulate(payload);
        String s = new String(out, StandardCharsets.UTF_8);
        assertEquals("NEXT:App: hello", s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNextRejectsNull() {
        proto.setNext(null);
    }

    // —— getSource/getDestination/extractors —— //

    @Test
    public void getSourceAndDestinationAreNull() {
        assertNull(proto.getSource());
        assertNull(proto.getDestination());
        assertNull(proto.extractSource(payload));
        assertNull(proto.extractDestination(payload));
    }

    // —— decapsulate(...) tests —— //

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsNullInput() {
        proto.setPrevious(new IdentityProtocol());
        proto.decapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsEmptyInput() {
        proto.setPrevious(new IdentityProtocol());
        proto.decapsulate(new byte[0]);
    }

    @Test(expected = NullPointerException.class)
    public void decapsulateRequiresPreviousProtocol() {
        // without setting previousProtocol
        byte[] framed = "App: hello".getBytes(StandardCharsets.UTF_8);
        proto.decapsulate(framed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsWrongPrefix() {
        proto.setPrevious(new IdentityProtocol());
        byte[] bad = "Other: hello".getBytes(StandardCharsets.UTF_8);
        proto.decapsulate(bad);
    }

    @Test
    public void decapsulateStripsHeaderAndForwardsToPrevious() {
        IdentityProtocol identity = new IdentityProtocol();
        proto.setPrevious(identity);

        byte[] framed = "App: hello".getBytes(StandardCharsets.UTF_8);
        byte[] out = proto.decapsulate(framed);
        String s = new String(out, StandardCharsets.UTF_8);
        assertEquals("hello", s);
    }

    // —— Round-trip test —— //

    @Test
    public void roundTripEncapsulateThenDecapsulate() {
        IdentityProtocol identity = new IdentityProtocol();
        proto.setNext(identity);
        proto.setPrevious(identity);

        byte[] wire = proto.encapsulate(payload);
        byte[] recovered = proto.decapsulate(wire);
        assertArrayEquals("round-trip must yield original payload", payload, recovered);
    }

    // —— equals() and hashCode() —— //

    @Test
    public void equalsAndHashCodeBasedOnName() {
        MSGProtocol p1 = new MSGProtocol("X");
        MSGProtocol p2 = new MSGProtocol("X");
        MSGProtocol p3 = new MSGProtocol("Y");

        assertTrue(p1.equals(p2));
        assertEquals(p1.hashCode(), p2.hashCode());

        assertFalse(p1.equals(p3));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals(new Object()));
    }
}