package com.netsim.protocols.MSG;

import com.netsim.networkstack.PDU;
import com.netsim.utils.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Represents a simple application‐level PDU whose serialization
 * is "name: message".
 */
public class MSGHeader extends PDU {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = MSGHeader.class.getSimpleName();

    private final String name;
    private final String message;

    /**
     * Constructs a MSGHeader with the given name and message.
     *
     * @param name    the header name (non-null)
     * @param message the payload message (non-null)
     * @throws IllegalArgumentException if name or message is null
     */
    public MSGHeader(String name, String message) throws IllegalArgumentException {
        super(null, null);
        logger.info("[" + CLS + "] constructing header for name=\"" + name + "\" message=\"" + message + "\"");
        if (name == null || message == null) {
            logger.error("[" + CLS + "] name or message is null");
            throw new IllegalArgumentException("MSGHeader: name and message must be non-null");
        }
        this.name    = name;
        this.message = message;
    }

    /**
     * Returns the header portion (just the name) as bytes.
     *
     * @return the header name bytes
     */
    @Override
    public byte[] getHeader() {
        logger.debug("[" + CLS + "] getHeader()");
        byte[] hdr = this.name.getBytes(StandardCharsets.UTF_8);
        logger.info("[" + CLS + "] header length=" + hdr.length);
        return hdr;
    }

    /**
     * Serializes the entire PDU as "name: message".
     *
     * @return the full PDU bytes
     */
    @Override
    public byte[] toByte() {
        logger.debug("[" + CLS + "] toByte()");
        String line = this.name + ": " + this.message;
        byte[] full = line.getBytes(StandardCharsets.UTF_8);
        logger.info("[" + CLS + "] full PDU length=" + full.length);
        return full;
    }

    /**
     * Retrieves the header name.
     *
     * @return the header name string
     */
    public String getNameString() {
        return this.name;
    }

    /**
     * Retrieves the payload message.
     *
     * @return the payload message string
     */
    public String getMessageString() {
        return this.message;
    }

    /**
     * Two MSGHeaders are equal if both name and message match.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MSGHeader)) {
            logger.debug("[" + CLS + "] equals: not an MSGHeader");
            return false;
        }
        MSGHeader that = (MSGHeader) o;
        boolean eq = this.name.equals(that.name) && this.message.equals(that.message);
        logger.debug("[" + CLS + "] equals() → " + eq);
        return eq;
    }

    /**
     * Computes hash code based on name and message.
     */
    @Override
    public int hashCode() {
        int h = Objects.hash(this.name, this.message);
        logger.debug("[" + CLS + "] hashCode() = " + h);
        return h;
    }
}