package com.netsim.addresses;

import java.util.Arrays;
import com.netsim.utils.Logger;

/**
 * A 6‐byte MAC address.
 */
public class Mac extends Address {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = Mac.class.getSimpleName();

    /**
     * Parses and constructs a MAC from a string like "02:00:00:00:00:01".
     * @throws IllegalArgumentException if format is invalid
     */
    public Mac(String address) {
        super(address, 6);
        logger.info("[" + CLS + "] constructed " + stringRepresentation());
    }

    @Override
    protected byte[] parse(String address) {
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
                String msg = "Octet #" + (i+1) + " not valid hex: \"" + part + "\"";
                logger.error("[" + CLS + "] " + msg);
                logger.debug("[" + CLS + "] parse error detail: " + e.getMessage());
                throw new IllegalArgumentException(msg, e);
            }
            octets[i] = (byte) val;
        }
        logger.debug("[" + CLS + "] parsed \"" + address + "\" → " + Arrays.toString(octets));
        return octets;
    }

    @Override
    public void setAddress(String newAddress) {
        byte[] newBytes = this.parse(newAddress);
        if (newBytes.length != 6) {
            String msg = "setAddress failed: must be 6 bytes";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        super.setAddress(newBytes);
        logger.info("[" + CLS + "] address set to " + stringRepresentation());
    }

    @Override
    public String stringRepresentation() {
        byte[] octets = this.byteRepresentation();
        StringBuilder sb = new StringBuilder(17);
        for (int i = 0; i < octets.length; i++) {
            String hex = String.format("%02X", octets[i] & 0xFF);
            sb.append(hex);
            if (i < octets.length - 1) sb.append(':');
        }
        return sb.toString();
    }

    /**
     * @return the broadcast MAC ff:ff:ff:ff:ff:ff
     */
    public static Mac broadcast() {
        Mac bc = new Mac("FF:FF:FF:FF:FF:FF");
        logger.info("[" + Mac.class.getSimpleName() + "] broadcast address created");
        return bc;
    }

    /**
     * Build a Mac from a raw 6‐byte array.
     * @throws IllegalArgumentException if length != 6
     */
    public static Mac bytesToMac(byte[] sixBytes) {
        if (sixBytes == null || sixBytes.length != 6) {
            String msg = "bytesToMac: must pass exactly 6 bytes";
            Logger.getInstance().error("[" + Mac.class.getSimpleName() + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        StringBuilder sb = new StringBuilder(17);
        for (int i = 0; i < 6; i++) {
            sb.append(String.format("%02X", sixBytes[i] & 0xFF));
            if (i < 5) sb.append(':');
        }
        Mac result = new Mac(sb.toString());
        logger.info("[" + Mac.class.getSimpleName() + "] bytesToMac → " + result.stringRepresentation());
        return result;
    }
}