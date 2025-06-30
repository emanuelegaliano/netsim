package com.netsim.addresses;

import java.util.Arrays;
import com.netsim.utils.Logger;

/**
 * A 6‐byte MAC address.
 */
public class Mac extends Address {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = Mac.class.getSimpleName();

    /**
     * Parses and constructs a MAC from a string like "02:00:00:00:00:01".
     *
     * @param address textual MAC address
     * @throws IllegalArgumentException if format is invalid
     */
    public Mac(String address) throws IllegalArgumentException {
        super(address, 6);
        logger.info("[" + CLS + "] constructed " + this.stringRepresentation());
    }

    /**
     * Parses a colon‐separated hex MAC string into 6 bytes.
     *
     * @param address textual MAC address
     * @return 6‐byte array
     * @throws IllegalArgumentException if the input is null or malformed
     */
    @Override
    protected byte[] parse(String address) throws IllegalArgumentException {
        if (address == null) {
            String msg = "parse failed: input string is null";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        String[] parts = address.trim().split(":");
        if (parts.length != 6) {
            String msg = "Invalid MAC format: expected 6 octets, got " + parts.length;
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }

        byte[] octets = new byte[6];
        for (int i = 0; i < 6; i++) {
            String part = parts[i];
            if (part.length() != 2) {
                String msg = "Invalid octet length at index " + i + ": \"" + part + "\"";
                logger.error("[" + CLS + "] " + msg);
                throw new IllegalArgumentException(msg);
            }
            int val;
            try {
                val = Integer.parseInt(part, 16);
            } catch (NumberFormatException e) {
                String msg = "Octet #" + (i + 1) + " not valid hex: \"" + part + "\"";
                logger.error("[" + CLS + "] " + msg);
                logger.debug("[" + CLS + "] parse error detail: " + e.getMessage());
                throw new IllegalArgumentException(msg, e);
            }
            octets[i] = (byte) val;
        }
        logger.debug("[" + CLS + "] parsed \"" + address + "\" → " + Arrays.toString(octets));
        return octets;
    }

    /**
     * Updates this MAC to a new textual value.
     *
     * @param newAddress new MAC string
     * @throws IllegalArgumentException if parsing fails or length ≠ 6
     */
    @Override
    public void setAddress(String newAddress) throws IllegalArgumentException {
        byte[] newBytes = this.parse(newAddress);
        if (newBytes.length != 6) {
            String msg = "setAddress failed: must be 6 bytes";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        super.setAddress(newBytes);
        logger.info("[" + CLS + "] address set to " + this.stringRepresentation());
    }

    /**
     * Formats this MAC as "XX:XX:XX:XX:XX:XX".
     *
     * @return colon‐separated hex string
     */
    @Override
    public String stringRepresentation() {
        byte[] octets = this.byteRepresentation();
        StringBuilder sb = new StringBuilder(17);
        for (int i = 0; i < octets.length; i++) {
            sb.append(String.format("%02X", octets[i] & 0xFF));
            if (i < octets.length - 1) {
                sb.append(':');
            }
        }
        return sb.toString();
    }

    /**
     * Returns the broadcast MAC address FF:FF:FF:FF:FF:FF.
     *
     * @return broadcast MAC
     */
    public static Mac broadcast() {
        Mac bc = new Mac("FF:FF:FF:FF:FF:FF");
        logger.info("[" + CLS + "] broadcast address created");
        return bc;
    }

    /**
     * Builds a Mac from a raw 6‐byte array.
     *
     * @param sixBytes raw 6 bytes
     * @return corresponding Mac instance
     * @throws IllegalArgumentException if sixBytes is null or length ≠ 6
     */
    public static Mac bytesToMac(byte[] sixBytes) throws IllegalArgumentException {
        if (sixBytes == null || sixBytes.length != 6) {
            String msg = "bytesToMac: must pass exactly 6 bytes";
            Logger.getInstance().error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        StringBuilder sb = new StringBuilder(17);
        for (int i = 0; i < 6; i++) {
            sb.append(String.format("%02X", sixBytes[i] & 0xFF));
            if (i < 5) {
                sb.append(':');
            }
        }
        Mac result = new Mac(sb.toString());
        logger.info("[" + CLS + "] bytesToMac → " + result.stringRepresentation());
        return result;
    }
}