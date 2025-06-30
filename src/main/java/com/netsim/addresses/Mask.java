package com.netsim.addresses;

import com.netsim.utils.Logger;

/**
 * A subnet‐mask of length N bytes with a prefix, e.g. 255.255.255.0/24.
 */
public class Mask extends Address {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = Mask.class.getSimpleName();

    private int prefix;

    /**
     * Build from prefix length.
     */
    public Mask(int prefix, int bytes) throws IllegalArgumentException {
        super(buildMaskString(prefix, bytes), bytes);
        this.prefix = prefix;
        logger.info("[" + CLS + "] constructed mask=" + stringRepresentation() + " (/" + prefix + ")");
    }

    /**
     * Build from dotted mask string.
     */
    public Mask(String address, int bytes) throws IllegalArgumentException {
        super(address, bytes);
        // validate contiguity and compute prefix
        boolean zeroFound = false;
        int computed = 0;
        for (byte b : this.address) {
            int unsigned = b & 0xFF;
            for (int bit = 7; bit >= 0; bit--) {
                boolean one = ((unsigned >> bit) & 1) == 1;
                if (one) {
                    if (zeroFound) {
                        String msg = "Non‐contiguous mask: " + address;
                        logger.error("[" + CLS + "] " + msg);
                        throw new IllegalArgumentException(msg);
                    }
                    computed++;
                } else {
                    zeroFound = true;
                }
            }
        }
        this.prefix = computed;
        logger.info("[" + CLS + "] parsed mask=" + stringRepresentation() + " (/" + prefix + ")");
    }

    /**
     * Build dotted‐decimal mask from prefix.
     */
    public static String buildMaskString(int prefix, int bytes) {
        int fullBytes = prefix / 8, rem = prefix % 8;
        StringBuilder sb = new StringBuilder(bytes * 4);
        for (int i = 0; i < bytes; i++) {
            int octet;
            if (i < fullBytes) {
                octet = 0xFF;
            } else if (i == fullBytes) {
                octet = ((0xFF << (8 - rem)) & 0xFF);
            } else {
                octet = 0;
            }
            sb.append(octet);
            if (i < bytes - 1) sb.append('.');
        }
        logger.debug("[Mask] buildMaskString -> " + sb.toString());
        return sb.toString();
    }

    @Override
    public void setAddress(String newAddress) throws IllegalArgumentException {
        super.setAddress(parse(newAddress));
        logger.info("[" + CLS + "] address set to " + stringRepresentation());
    }

    /**
     * Change the prefix length only.
     */
    public void setPrefix(int newPrefix) {
        logger.info("[" + CLS + "] prefix changed from /" + this.prefix + " to /" + newPrefix);
        this.prefix = newPrefix;
    }

    @Override
    protected byte[] parse(String address) {
        if (address == null) {
            String msg = "parse failed: address string is null";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        String[] parts = address.trim().split("\\.");
        if (parts.length != this.bytesLen) {
            String msg = "Invalid mask format: expected " + this.bytesLen
                       + " octets, got " + parts.length + " in \"" + address + "\"";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        byte[] octets = new byte[this.bytesLen];
        for (int i = 0; i < this.bytesLen; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                String msg = "Octet #" + (i+1) + " is empty in \"" + address + "\"";
                logger.error("[" + CLS + "] " + msg);
                throw new IllegalArgumentException(msg);
            }
            int v;
            try {
                v = Integer.parseInt(part);
            } catch (NumberFormatException e) {
                String msg = "Octet #" + (i+1) + " not a valid integer: \"" + part + "\"";
                logger.error("[" + CLS + "] " + msg);
                throw new IllegalArgumentException(msg, e);
            }
            if (v < 0 || v > 255) {
                String msg = "Octet #" + (i+1) + " out of range (0–255): " + v;
                logger.error("[" + CLS + "] " + msg);
                throw new IllegalArgumentException(msg);
            }
            octets[i] = (byte) v;
        }
        logger.debug("[" + CLS + "] parsed \"" + address + "\" → " + java.util.Arrays.toString(octets));
        return octets;
    }

    public int getPrefix() {
        return this.prefix;
    }
}