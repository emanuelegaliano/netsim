package com.netsim.addresses;

import com.netsim.utils.Logger;
import java.util.Arrays;

/**
 * Represents an IPv4/IPv6 subnet mask with a prefix length.
 */
public class Mask extends Address {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = Mask.class.getSimpleName();

    private int prefix;

    /**
     * Constructs a Mask from a prefix length.
     *
     * @param prefix the subnet prefix length (0–8*bytes)
     * @param bytes  number of bytes (e.g., 4 for IPv4)
     * @throws IllegalArgumentException if prefix or bytes yield invalid mask
     */
    public Mask(int prefix, int bytes) throws IllegalArgumentException {
        super(buildMaskString(prefix, bytes), bytes);
        this.prefix = prefix;
        logger.info("[" + CLS + "] constructed mask=" + this.stringRepresentation() + " (/" + this.prefix + ")");
    }

    /**
     * Constructs a Mask from a dotted‐decimal mask string.
     *
     * @param maskString dotted mask (e.g. "255.255.255.0")
     * @param bytes      number of bytes (e.g., 4 for IPv4)
     * @throws IllegalArgumentException if maskString is invalid or non‐contiguous
     */
    public Mask(String maskString, int bytes) throws IllegalArgumentException {
        super(maskString, bytes);
        boolean zeroFound = false;
        int computed = 0;
        for (byte b : this.address) {
            int unsigned = b & 0xFF;
            for (int bit = 7; bit >= 0; bit--) {
                boolean one = ((unsigned >> bit) & 1) == 1;
                if (one) {
                    if (zeroFound) {
                        String msg = "Non‐contiguous mask: " + maskString;
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
        logger.info("[" + CLS + "] parsed mask=" + this.stringRepresentation() + " (/" + this.prefix + ")");
    }

    /**
     * Builds a dotted‐decimal mask string from a prefix length.
     *
     * @param prefix the subnet prefix length
     * @param bytes  number of bytes
     * @return dotted‐decimal mask string
     */
    public static String buildMaskString(int prefix, int bytes) {
        int fullBytes = prefix / 8;
        int rem       = prefix % 8;
        StringBuilder sb = new StringBuilder(bytes * 4);
        for (int i = 0; i < bytes; i++) {
            int octet;
            if (i < fullBytes) {
                octet = 0xFF;
            } else if (i == fullBytes) {
                octet = (0xFF << (8 - rem)) & 0xFF;
            } else {
                octet = 0;
            }
            sb.append(octet);
            if (i < bytes - 1) {
                sb.append('.');
            }
        }
        logger.debug("[" + CLS + "] buildMaskString -> " + sb.toString());
        return sb.toString();
    }

    /**
     * Updates this Mask's address from a dotted‐decimal string.
     *
     * @param newAddress dotted mask string
     * @throws IllegalArgumentException if parse fails
     */
    @Override
    public void setAddress(String newAddress) throws IllegalArgumentException {
        super.setAddress(this.parse(newAddress));
        logger.info("[" + CLS + "] address set to " + this.stringRepresentation());
    }

    /**
     * Changes only the prefix length.
     *
     * @param newPrefix new subnet prefix length
     */
    public void setPrefix(int newPrefix) {
        logger.info("[" + CLS + "] prefix changed from /" + this.prefix + " to /" + newPrefix);
        this.prefix = newPrefix;
    }

    /**
     * Parses a dotted‐decimal mask string into bytes.
     *
     * @param address dotted mask string
     * @return raw byte array
     * @throws IllegalArgumentException if format is invalid
     */
    @Override
    protected byte[] parse(String address) throws IllegalArgumentException {
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
                String msg = "Octet #" + (i + 1) + " is empty in \"" + address + "\"";
                logger.error("[" + CLS + "] " + msg);
                throw new IllegalArgumentException(msg);
            }
            int v;
            try {
                v = Integer.parseInt(part);
            } catch (NumberFormatException e) {
                String msg = "Octet #" + (i + 1) + " not a valid integer: \"" + part + "\"";
                logger.error("[" + CLS + "] " + msg);
                throw new IllegalArgumentException(msg, e);
            }
            if (v < 0 || v > 255) {
                String msg = "Octet #" + (i + 1) + " out of range (0–255): " + v;
                logger.error("[" + CLS + "] " + msg);
                throw new IllegalArgumentException(msg);
            }
            octets[i] = (byte) v;
        }
        logger.debug("[" + CLS + "] parsed \"" + address + "\" → " + Arrays.toString(octets));
        return octets;
    }

    /**
     * Retrieves the current prefix length.
     *
     * @return subnet prefix length
     */
    public int getPrefix() {
        return this.prefix;
    }
}