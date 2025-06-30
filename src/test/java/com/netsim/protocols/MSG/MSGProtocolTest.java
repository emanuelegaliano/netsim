package com.netsim.protocols.MSG;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class MSGProtocolTest {

    @Test
    public void testEncapsulationAndDecapsulation() {
        MSGProtocol protocol = new MSGProtocol("Alice");
        String inputMessage = "Hello, world!";
        byte[] encoded = protocol.encapsulate(inputMessage.getBytes(StandardCharsets.UTF_8));
        byte[] decoded = protocol.decapsulate(encoded);

        assertEquals("Decoded message should match original",
                inputMessage, new String(decoded, StandardCharsets.UTF_8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullName() {
        new MSGProtocol(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsLongName() {
        new MSGProtocol("ThisNameIsWayTooLongToBeValid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncapsulateRejectsNullPayload() {
        MSGProtocol protocol = new MSGProtocol("Bob");
        protocol.encapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncapsulateRejectsEmptyPayload() {
        MSGProtocol protocol = new MSGProtocol("Charlie");
        protocol.encapsulate(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecapsulateRejectsNullPayload() {
        MSGProtocol protocol = new MSGProtocol("Dan");
        protocol.decapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecapsulateRejectsEmptyPayload() {
        MSGProtocol protocol = new MSGProtocol("Eve");
        protocol.decapsulate(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecapsulateRejectsInvalidPrefix() {
        MSGProtocol protocol = new MSGProtocol("Frank");
        String invalid = "Mallory: Malicious message";
        protocol.decapsulate(invalid.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testGetUserReturnsCorrectName() {
        MSGProtocol protocol = new MSGProtocol("Grace");
        assertEquals("Grace", protocol.getUser());
    }

    @Test
    public void testEqualsAndHashCode() {
        MSGProtocol a = new MSGProtocol("Henry");
        MSGProtocol b = new MSGProtocol("Henry");
        MSGProtocol c = new MSGProtocol("Isaac");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    public void testCopyCreatesEqualObject() {
        MSGProtocol original = new MSGProtocol("Jack");
        MSGProtocol copy = (MSGProtocol) original.copy();

        assertNotSame("Copy should not be same object", original, copy);
        assertEquals("Copy should be equal in content", original, copy);
    }
}