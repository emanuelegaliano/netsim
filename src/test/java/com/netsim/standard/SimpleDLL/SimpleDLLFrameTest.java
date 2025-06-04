package com.netsim.standard.SimpleDLL;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.Mac;

/**
 * Unit tests for SimpleDLLFrame after payload changed to byte[].
 */
public class SimpleDLLFrameTest {
    private Mac srcMac;
    private Mac dstMac;
    private byte[] payloadBytes;

    @Before
    public void setUp() {
        srcMac = new Mac("aa:bb:cc:00:11:22");
        dstMac = new Mac("11:22:33:44:55:66");
        payloadBytes = new byte[] { 0x01, 0x02, 0x03, 0x04 };
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullSrcMac() {
        new SimpleDLLFrame(null, dstMac, payloadBytes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullDstMac() {
        new SimpleDLLFrame(srcMac, null, payloadBytes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullPayload() {
        new SimpleDLLFrame(srcMac, dstMac, null);
    }

    @Test
    public void getHeaderProducesDstThenSrcBytes() {
        SimpleDLLFrame frame = new SimpleDLLFrame(srcMac, dstMac, payloadBytes);
        byte[] header = frame.getHeader();
        // Header must be exactly 12 bytes: 6 bytes of dstMac, then 6 bytes of srcMac
        assertEquals(12, header.length);

        byte[] expectedDst = dstMac.byteRepresentation();
        byte[] expectedSrc = srcMac.byteRepresentation();
        // Compare first 6 bytes to dstMac.toByte()
        for (int i = 0; i < 6; i++) {
            assertEquals("DST MAC byte mismatch at index " + i,
                         expectedDst[i], header[i]);
        }
        // Compare next 6 bytes to srcMac.toByte()
        for (int i = 0; i < 6; i++) {
            assertEquals("SRC MAC byte mismatch at index " + i,
                         expectedSrc[i], header[6 + i]);
        }
    }

    @Test
    public void toByteConcatenatesHeaderAndPayload() {
        SimpleDLLFrame frame = new SimpleDLLFrame(srcMac, dstMac, payloadBytes);
        byte[] wire = frame.toByte();

        // The wire‐format should be [12‐byte header][payloadBytes]
        assertEquals(12 + payloadBytes.length, wire.length);

        // Verify header portion
        byte[] expectedDst = dstMac.byteRepresentation();
        byte[] expectedSrc = srcMac.byteRepresentation();
        for (int i = 0; i < 6; i++) {
            assertEquals("Header byte " + i + " should match dstMac",
                         expectedDst[i], wire[i]);
        }
        for (int i = 0; i < 6; i++) {
            assertEquals("Header byte " + (6 + i) + " should match srcMac",
                         expectedSrc[i], wire[6 + i]);
        }
        // Verify payload portion
        for (int i = 0; i < payloadBytes.length; i++) {
            assertEquals("Payload byte mismatch at index " + i,
                         payloadBytes[i], wire[12 + i]);
        }
    }
}