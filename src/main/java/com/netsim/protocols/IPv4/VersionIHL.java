package com.netsim.protocols.IPv4;

import com.netsim.utils.Logger;

/**
 * Encapsulates a 4-bit IP version and 4-bit IHL (Internet Header Length) in one byte.
 */
public final class VersionIHL {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = VersionIHL.class.getSimpleName();

    private byte b;

    /**
     * Constructs a VersionIHL from separate version and IHL values.
     *
     * @param version 4-bit version number (0…15)
     * @param ihl     4-bit header length in 32-bit words (5…15)
     * @throws IllegalArgumentException if version or ihl is out of range
     */
    public VersionIHL(int version, int ihl) throws IllegalArgumentException {
        if (version < 0 || version > 0xF) {
            logger.error("[" + CLS + "] version out of range: " + version);
            throw new IllegalArgumentException("VersionIHL: version must be 0…15");
        }
        if (ihl < 5 || ihl > 0xF) {
            logger.error("[" + CLS + "] IHL out of range: " + ihl);
            throw new IllegalArgumentException("VersionIHL: IHL must be 5…15");
        }
        this.b = (byte) ((version << 4) | (ihl & 0xF));
        logger.info("[" + CLS + "] constructed byte=0x" + String.format("%02X", this.b));
    }

    /**
     * Extracts the 4-bit version field.
     *
     * @return the version (0…15)
     */
    public int getVersion() {
        int version = (this.b >>> 4) & 0xF;
        logger.debug("[" + CLS + "] getVersion() → " + version);
        return version;
    }

    /**
     * Extracts the 4-bit IHL field.
     *
     * @return the IHL in 32-bit words (0…15)
     */
    public int getIhl() {
        int ihl = this.b & 0xF;
        logger.debug("[" + CLS + "] getIhl() → " + ihl);
        return ihl;
    }

    /**
     * Returns the raw combined byte for serialization.
     *
     * @return the byte combining version and IHL
     */
    public byte toByte() {
        logger.debug("[" + CLS + "] toByte() → 0x" + String.format("%02X", this.b));
        return this.b;
    }

    /**
     * Reconstructs a VersionIHL from a raw byte.
     *
     * @param raw the byte containing version and IHL
     * @return a new VersionIHL instance
     * @throws IllegalArgumentException if extracted values are invalid
     */
    public static VersionIHL fromByte(byte raw) throws IllegalArgumentException {
        int version = (raw >>> 4) & 0xF;
        int ihl     = raw & 0xF;
        logger.debug("[" + CLS + "] fromByte(raw=0x" + String.format("%02X", raw) +
                     ") → version=" + version + ", IHL=" + ihl);
        return new VersionIHL(version, ihl);
    }
}