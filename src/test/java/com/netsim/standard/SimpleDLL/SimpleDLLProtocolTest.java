package com.netsim.standard.SimpleDLL;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.Mac;
import com.netsim.networkstack.IdentityProtocol;
/**
 * Unit tests for SimpleDLLProtocol, using the built‐in IdentityProtocol.
 */
public class SimpleDLLProtocolTest {
    private Mac srcMac;
    private Mac dstMac;
    private byte[] payload;
    private SimpleDLLProtocol dllProto;

    @Before
    public void setUp() {
        srcMac = new Mac("aa:bb:cc:00:11:22");
        dstMac = new Mac("11:22:33:44:55:66");
        payload = new byte[] { 0x10, 0x20, 0x30 };
        dllProto = new SimpleDLLProtocol(srcMac, dstMac);
    }

    // —— Constructor tests —— //

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullSourceMac() {
        new SimpleDLLProtocol(null, dstMac);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullDestinationMac() {
        new SimpleDLLProtocol(srcMac, null);
    }

    // —— encapsulate(...) tests —— //

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateRejectsNullPayload() {
        // set a valid next so that exception is from payload check
        dllProto.setNext(new IdentityProtocol());
        dllProto.encapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateRejectsEmptyPayload() {
        dllProto.setNext(new IdentityProtocol());
        dllProto.encapsulate(new byte[0]);
    }

    @Test(expected = NullPointerException.class)
    public void encapsulateRequiresNextProtocol() {
        dllProto.encapsulate(payload);
    }

    @Test
    public void encapsulateProducesFrameAndForwardsToNext() {
        // Use IdentityProtocol as next: it will return exactly the frame bytes
        IdentityProtocol identityNext = new IdentityProtocol();
        dllProto.setNext(identityNext);

        byte[] result = dllProto.encapsulate(payload);

        // With identity next, result should be exactly header || payload
        assertEquals(12 + payload.length, result.length);

        // Check header: first 6 bytes == dstMac, next 6 == srcMac
        byte[] expectedDst = dstMac.byteRepresentation();
        byte[] expectedSrc = srcMac.byteRepresentation();
        for (int i = 0; i < 6; i++) {
            assertEquals(expectedDst[i], result[i]);
        }
        for (int i = 0; i < 6; i++) {
            assertEquals(expectedSrc[i], result[6 + i]);
        }
        // Check payload portion
        for (int i = 0; i < payload.length; i++) {
            assertEquals(payload[i], result[12 + i]);
        }
    }

    // —— decapsulate(...) tests —— //

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsNullInput() {
        dllProto.setPrevious(new IdentityProtocol());
        dllProto.decapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsTooShortInput() {
        dllProto.setPrevious(new IdentityProtocol());
        dllProto.decapsulate(new byte[11]);
    }

    @Test(expected = NullPointerException.class)
    public void decapsulateRequiresPreviousProtocol() {
        // Construct a valid frame: header (12 bytes) + payload
        byte[] frame = new byte[12 + payload.length];
        dllProto.decapsulate(frame);
    }

    @Test
    public void decapsulateStripsHeaderAndForwardsToPrevious() {
        // Build a frame: [dst(6)] [src(6)] [payload]
        byte[] frame = new byte[12 + payload.length];
        byte[] dstBytes = dstMac.byteRepresentation();
        byte[] srcBytes = srcMac.byteRepresentation();
        System.arraycopy(dstBytes, 0, frame, 0, 6);
        System.arraycopy(srcBytes, 0, frame, 6, 6);
        System.arraycopy(payload, 0, frame, 12, payload.length);

        // Use IdentityProtocol as previous: it returns exactly the stripped payload
        IdentityProtocol identityPrev = new IdentityProtocol();
        dllProto.setPrevious(identityPrev);

        byte[] result = dllProto.decapsulate(frame);

        // result should equal payload
        assertArrayEquals(payload, result);
    }

    // —— Round‐trip test —— //

    @Test
    public void roundTripEncapsulateThenDecapsulate() {
        IdentityProtocol identity = new IdentityProtocol();
        dllProto.setNext(identity);
        dllProto.setPrevious(identity);

        byte[] wire = dllProto.encapsulate(payload);
        byte[] recovered = dllProto.decapsulate(wire);
        assertArrayEquals("Round‐trip should recover original payload", payload, recovered);
    }
}