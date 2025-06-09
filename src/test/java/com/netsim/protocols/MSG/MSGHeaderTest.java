package com.netsim.protocols.MSG;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

/**
 * Unit tests for {@link MSGHeader}.
 */
public class MSGHeaderTest {

    // —— Constructor argument validation —— //

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullName() {
        new MSGHeader(null, "payload");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullMessage() {
        new MSGHeader("HeaderName", null);
    }

    // —— Getter methods —— //

    @Test
    public void getNameStringReturnsName() {
        MSGHeader hdr = new MSGHeader("MyApp", "Hello");
        assertEquals("MyApp", hdr.getNameString());
    }

    @Test
    public void getMessageStringReturnsMessage() {
        MSGHeader hdr = new MSGHeader("MyApp", "Hello");
        assertEquals("Hello", hdr.getMessageString());
    }

    // —— Serialization tests —— //

    @Test
    public void getHeaderReturnsNameBytesOnly() {
        String name = "Test";
        String msg = "Ignored";
        MSGHeader hdr = new MSGHeader(name, msg);

        byte[] headerBytes = hdr.getHeader();
        assertArrayEquals(
            name.getBytes(StandardCharsets.UTF_8),
            headerBytes
        );
    }

    @Test
    public void toByteReturnsNameColonSpaceMessage() {
        String name = "App";
        String msg  = "Payload";
        MSGHeader hdr = new MSGHeader(name, msg);

        String expected = "App: Payload";
        byte[] actual = hdr.toByte();
        assertArrayEquals(
            expected.getBytes(StandardCharsets.UTF_8),
            actual
        );
    }

    // —— Equals and hashCode —— //

    @Test
    public void equalsAndHashCodeForSameContent() {
        MSGHeader a = new MSGHeader("A", "B");
        MSGHeader b = new MSGHeader("A", "B");
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void notEqualsWhenNameDiffers() {
        MSGHeader a = new MSGHeader("A", "B");
        MSGHeader b = new MSGHeader("X", "B");
        assertFalse(a.equals(b));
    }

    @Test
    public void notEqualsWhenMessageDiffers() {
        MSGHeader a = new MSGHeader("A", "B");
        MSGHeader b = new MSGHeader("A", "Y");
        assertFalse(a.equals(b));
    }

    @Test
    public void notEqualsDifferentTypeOrNull() {
        MSGHeader hdr = new MSGHeader("A", "B");
        assertFalse(hdr.equals(null));
        assertFalse(hdr.equals("some string"));
    }

    // —— Inherited PDU behavior —— //

    @Test
    public void getSourceAndDestinationAreNull() {
        MSGHeader hdr = new MSGHeader("Name", "Msg");
        assertNull("PDU.getSource should be null", hdr.getSource());
        assertNull("PDU.getDestination should be null", hdr.getDestination());
    }
}