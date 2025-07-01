package com.netsim.protocols.MSG;

import com.netsim.networkstack.Protocol;
import com.netsim.addresses.Address;
import com.netsim.addresses.Port;
import com.netsim.utils.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A simple application‐level protocol that serializes a payload as
 * "name: message" and decapsulates by stripping off the leading "name: " header.
 * <p>
 * Uses port 9696.
 * </p>
 */
public class MSGProtocol implements Protocol {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = MSGProtocol.class.getSimpleName();
    private final static int    port   = 9696;

    private static final int MAX_HEADER_LENGTH = 20;
    private final String name;

    /**
     * Constructs a new MSGProtocol instance.
     *
     * @param name the sender name (non-null, max 20 chars)
     * @throws IllegalArgumentException if name is null or too long
     */
    public MSGProtocol(String name) throws IllegalArgumentException {
        logger.info("[" + CLS + "] constructing with name=\"" + name + "\"");
        if (name == null) {
            logger.error("[" + CLS + "] name cannot be null");
            throw new IllegalArgumentException("MSGProtocol: name cannot be null");
        }
        if (name.length() > MAX_HEADER_LENGTH) {
            logger.error("[" + CLS + "] name is too long: " + name.length() + " > " + MAX_HEADER_LENGTH);
            throw new IllegalArgumentException("MSGProtocol: name is too long (max " + MAX_HEADER_LENGTH + " chars)");
        }
        this.name = name;
    }

    /**
     * Encapsulates the given application payload by prefixing "name: ".
     *
     * @param upperLayerPDU the application payload bytes (non-null, non-empty)
     * @return the encapsulated bytes
     * @throws IllegalArgumentException if payload is null or empty
     */
    @Override
    public byte[] encapsulate(byte[] upperLayerPDU) throws IllegalArgumentException {
        logger.debug("[" + CLS + "] encapsulate called, payload length=" +
                     (upperLayerPDU == null ? "null" : upperLayerPDU.length));
        if (upperLayerPDU == null || upperLayerPDU.length == 0) {
            logger.error("[" + CLS + "] payload cannot be null or empty");
            throw new IllegalArgumentException("MSGProtocol: payload cannot be null or empty");
        }

        String message = new String(upperLayerPDU, StandardCharsets.UTF_8);
        String framed  = this.name + ": " + message;
        byte[] out     = framed.getBytes(StandardCharsets.UTF_8);
        logger.info("[" + CLS + "] encapsulated length=" + out.length);
        return out;
    }

    /**
     * Decapsulates the incoming bytes by removing the "name: " prefix.
     *
     * @param lowerLayerPDU the received bytes (non-null, non-empty)
     * @return the original payload bytes
     * @throws IllegalArgumentException if input is null, empty, or missing prefix
     */
    @Override
    public byte[] decapsulate(byte[] lowerLayerPDU) throws IllegalArgumentException {
        logger.debug("[" + CLS + "] decapsulate called, input length=" +
                     (lowerLayerPDU == null ? "null" : lowerLayerPDU.length));
        if (lowerLayerPDU == null || lowerLayerPDU.length == 0) {
            logger.error("[" + CLS + "] input cannot be null or empty");
            throw new IllegalArgumentException("MSGProtocol: input cannot be null or empty");
        }

        String full   = new String(lowerLayerPDU, StandardCharsets.UTF_8);
        String prefix = this.name + ": ";
        if (!full.startsWith(prefix)) {
            logger.error("[" + CLS + "] missing prefix \"" + prefix + "\"");
            throw new IllegalArgumentException("MSGProtocol: expected prefix \"" + prefix + "\"");
        }

        String message = full.substring(prefix.length());
        byte[] out     = message.getBytes(StandardCharsets.UTF_8);
        logger.info("[" + CLS + "] decapsulated length=" + out.length);
        return out;
    }

    /**
     * Retrieves the user name associated with this protocol.
     *
     * @return the sender name
     */
    public String getUser() {
        return this.name;
    }

    /**
     * Returns the well‐known port for MSGProtocol.
     *
     * @return a Port instance for this protocol
     */
    public static Port port() {
        return new Port(Integer.toString(port));
    }

    @Override
    public Address getSource() {
        return null;
    }

    @Override
    public Address getDestination() {
        return null;
    }

    @Override
    public Address extractSource(byte[] pdu) {
        return null;
    }

    @Override
    public Address extractDestination(byte[] pdu) {
        return null;
    }

    /**
     * Two MSGProtocol instances are equal if their names match.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MSGProtocol)) {
            return false;
        }
        MSGProtocol that = (MSGProtocol) obj;
        boolean eq = Objects.equals(this.name, that.name);
        logger.debug("[" + CLS + "] equals() → " + eq);
        return eq;
    }

    /**
     * Hash code based on the protocol name.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int h = Objects.hash(this.name);
        logger.debug("[" + CLS + "] hashCode() = " + h);
        return h;
    }

    /**
     * Creates a copy of this protocol instance.
     *
     * @return a new MSGProtocol with the same name
     */
    @Override
    public Protocol copy() {
        logger.info("[" + CLS + "] copying protocol instance");
        return new MSGProtocol(this.name);
    }
}