package com.netsim.addresses;

import java.util.Arrays;
import com.netsim.utils.Logger;

/**
 * IPv4 address implementation.
 */
public class IPv4 extends IP {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = IPv4.class.getSimpleName();

    /**
     * Dotted‐decimal + mask string ctor.
     */
    public IPv4(String addressString, String maskString) {
        super(addressString, maskString, 4);
        logger.info("[" + CLS + "] constructed " + stringRepresentation() + " mask=" + maskString);
    }

    /**
     * Dotted‐decimal + prefix length ctor.
     */
    public IPv4(String addressString, int maskPrefix) {
        super(addressString, maskPrefix, 4);
        logger.info("[" + CLS + "] constructed " 
                    + stringRepresentation() + "/" + maskPrefix);
    }

    @Override
    protected byte[] parse(String address) {
        if (address == null) {
            logger.error("[" + CLS + "] parse failed: address string is null");
            throw new IllegalArgumentException("Address string cannot be null");
        }

        String[] parts = address.trim().split("\\.", -1);
        if (parts.length != 4) {
            String msg = "Invalid IPv4 format, got " + parts.length + " parts in \"" + address + "\"";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }

        byte[] octets = new byte[4];
        for (int i = 0; i < 4; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                String msg = "Octet #" + (i+1) + " is empty in \"" + address + "\"";
                logger.error("[" + CLS + "] " + msg);
                throw new IllegalArgumentException(msg);
            }
            int val;
            try {
                val = Integer.parseInt(part);
            } catch (NumberFormatException e) {
                String msg = "Octet #" + (i+1) + " not a valid integer: \"" + part + "\"";
                logger.error("[" + CLS + "] " + msg);
                throw new IllegalArgumentException(msg, e);
            }
            if (val < 0 || val > 255) {
                String msg = "Octet #" + (i+1) + " out of range (0–255): " + val;
                logger.error("[" + CLS + "] " + msg);
                throw new IllegalArgumentException(msg);
            }
            octets[i] = (byte) val;
        }
        logger.debug("[" + CLS + "] parsed \"" + address + "\" → " + Arrays.toString(octets));
        return octets;
    }

    @Override
    public boolean isLoopback() {
        boolean result = isInSubnet("127.0.0.0", 8);
        logger.debug("[" + CLS + "] isLoopback() → " + result);
        return result;
    }

    @Override
    public boolean isMulticast() {
        boolean result = isInSubnet("224.0.0.0", 4);
        logger.debug("[" + CLS + "] isMulticast() → " + result);
        return result;
    }

    @Override
    public boolean isBroadcast() {
        for (byte b : this.address) {
            if ((b & 0xFF) != 0xFF) {
                logger.debug("[" + CLS + "] isBroadcast() → false");
                return false;
            }
        }
        logger.debug("[" + CLS + "] isBroadcast() → true");
        return true;
    }

    @Override
    public boolean isPrivate() {
        boolean result = isInSubnet("10.0.0.0", 8)
                      || isInSubnet("172.16.0.0", 12)
                      || isInSubnet("192.168.0.0", 16);
        logger.debug("[" + CLS + "] isPrivate() → " + result);
        return result;
    }

    @Override
    public boolean isLinkLocal() {
        boolean result = isInSubnet("169.254.0.0", 16);
        logger.debug("[" + CLS + "] isLinkLocal() → " + result);
        return result;
    }

    @Override
    public boolean isUnspecified() {
        for (byte b : address) {
            if (b != 0) {
                logger.debug("[" + CLS + "] isUnspecified() → false");
                return false;
            }
        }
        logger.debug("[" + CLS + "] isUnspecified() → true");
        return true;
    }

    @Override
    public boolean isSubnet() {
        int prefix = this.mask.getPrefix();
        if (prefix < 0 || prefix > 32) {
            String msg = "Invalid mask prefix: " + prefix;
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalStateException(msg);
        }
        byte[] maskBytes = new Mask(prefix, 4).byteRepresentation();
        byte[] addrBytes = this.byteRepresentation();
        for (int i = 0; i < 4; i++) {
            if ((addrBytes[i] & ~maskBytes[i]) != 0) {
                logger.debug("[" + CLS + "] isSubnet() → false");
                return false;
            }
        }
        logger.debug("[" + CLS + "] isSubnet() → true");
        return true;
    }

    /**
     * @return broadcast based on the current subnet (e.g. x.x.x.255)
     */
    public IPv4 subnetBroadcast() {
        String[] ipParts = this.stringRepresentation().split("\\.");
        int ipInt = 0;
        for (int i = 0; i < 4; i++) {
            ipInt |= (Integer.parseInt(ipParts[i]) << (24 - (8 * i)));
        }
        int subnetMask = (mask.getPrefix() == 0)
                       ? 0
                       : (~0) << (32 - this.mask.getPrefix());
        int broadcastInt = ipInt | ~subnetMask;

        String bStr = String.format(
            "%d.%d.%d.%d",
            (broadcastInt >> 24) & 0xFF,
            (broadcastInt >> 16) & 0xFF,
            (broadcastInt >> 8) & 0xFF,
            broadcastInt & 0xFF
        );
        IPv4 bc = new IPv4(bStr, mask.getPrefix());
        logger.info("[" + CLS + "] subnetBroadcast() → " + bc.stringRepresentation());
        return bc;
    }

    @Override public boolean equals(Object o) { return super.equals(o); }
    @Override public int     hashCode()      { return super.hashCode(); }
}