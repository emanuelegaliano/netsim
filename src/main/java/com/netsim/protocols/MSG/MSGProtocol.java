package com.netsim.protocols.MSG;

import com.netsim.networkstack.Protocol;
import com.netsim.addresses.Address;
import com.netsim.addresses.Port;
import com.netsim.utils.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A simple application-level protocol that serializes a payload as
 *   [name][": "][payload]
 * and decapsulates by stripping off the leading name+": " header.
 * 
 * Defined in port 9696.
 */
public class MSGProtocol implements Protocol {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = MSGProtocol.class.getSimpleName();

    public static final int port = 9696;
    private final String name;
    private final int maxHeaderLength = 20;

    /**
     * Constructs a new MSGProtocol instance.
     * 
     * @param name the sender name (non-null, max 20 chars)
     */
    public MSGProtocol(String name) {
        logger.info("[" + CLS + "] constructing with name=\"" + name + "\"");
        if (name == null) {
            logger.error("[" + CLS + "] name cannot be null");
            throw new IllegalArgumentException("MSGProtocol: name cannot be null");
        }
        if (name.length() > maxHeaderLength) {
            logger.error("[" + CLS + "] name is too long: " + name.length() + " > " + maxHeaderLength);
            throw new IllegalArgumentException("MSGProtocol: name is too long (max 20 chars)");
        }
        this.name = name;
    }

    @Override
    public byte[] encapsulate(byte[] upperLayerPDU) {
        logger.debug("[" + CLS + "] encapsulate called, payload length=" +
                     (upperLayerPDU == null ? "null" : upperLayerPDU.length));
        if (upperLayerPDU == null || upperLayerPDU.length == 0) {
            logger.error("[" + CLS + "] payload cannot be null or empty");
            throw new IllegalArgumentException("MSGProtocol: payload cannot be null or empty");
        }
        String message = new String(upperLayerPDU, StandardCharsets.UTF_8);
        String framed = this.name + ": " + message;
        byte[] out = framed.getBytes(StandardCharsets.UTF_8);
        logger.info("[" + CLS + "] encapsulated length=" + out.length);
        return out;
    }

    @Override
    public byte[] decapsulate(byte[] lowerLayerPDU) {
        logger.debug("[" + CLS + "] decapsulate called, input length=" +
                     (lowerLayerPDU == null ? "null" : lowerLayerPDU.length));
        if (lowerLayerPDU == null || lowerLayerPDU.length == 0) {
            logger.error("[" + CLS + "] input cannot be null or empty");
            throw new IllegalArgumentException("MSGProtocol: input cannot be null or empty");
        }
        String full = new String(lowerLayerPDU, StandardCharsets.UTF_8);
        String prefix = this.name + ": ";
        if (!full.startsWith(prefix)) {
            logger.error("[" + CLS + "] missing prefix \"" + prefix + "\"");
            throw new IllegalArgumentException("MSGProtocol: expected prefix \"" + prefix + "\"");
        }
        String message = full.substring(prefix.length());
        byte[] out = message.getBytes(StandardCharsets.UTF_8);
        logger.info("[" + CLS + "] decapsulated length=" + out.length);
        return out;
    }

    public String getUser() {
        return this.name;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MSGProtocol))
            return false;
        return Objects.equals(this.name, ((MSGProtocol) obj).name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public Protocol copy() {
        logger.info("[" + CLS + "] copying protocol instance");
        return new MSGProtocol(this.name);
    }
}