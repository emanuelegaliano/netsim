package com.netsim.addresses;

import java.util.Arrays;
import com.netsim.utils.Logger;

/**
 * Abstract base class representing an IP address (e.g., IPv4, IPv6).
 * It encapsulates the address bytes and a subnet mask, and provides
 * common methods for subnet operations and address classification.
 */
public abstract class IP extends Address {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = IP.class.getSimpleName();

    protected Mask mask;

    /**
     * Constructs an IP address with the given address string and prefix length.
     *
     * @param addressString the IP address in string format (e.g., "192.168.0.1")
     * @param prefix the subnet prefix length
     * @param bytes number of bytes for the address (4 for IPv4, 16 for IPv6)
     * @throws IllegalArgumentException by super constructor
     */
    protected IP(String addressString, int prefix, int bytes) {
        super(addressString, bytes);
        this.mask = new Mask(prefix, bytes);
        logger.info("[" + CLS + "] constructed " + stringRepresentation() + "/" + prefix);
    }

    /**
     * Constructs an IP address with the given address and mask strings.
     *
     * @param addressString the IP address
     * @param maskString the subnet mask
     * @param bytes number of bytes in the address
     * @throws IllegalArgumentException by super constructor
     */
    protected IP(String addressString, String maskString, int bytes) {
        super(addressString, bytes);
        this.mask = new Mask(maskString, bytes);
        logger.info("[" + CLS + "] constructed " + stringRepresentation() + " mask=" + maskString);
    }

    /**
     * Checks if this IP address belongs to a given subnet.
     *
     * @param networkString the network address as string (e.g., "192.168.1.0")
     * @param mask the subnet prefix length
     * @return true if this IP is within the subnet, false otherwise
     * @throws IllegalArgumentException if the network address or mask is invalid
     */
    public boolean isInSubnet(String networkString, int mask) {
        if (networkString == null) {
            String msg = "Network string cannot be null";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }

        byte[] network = this.parse(networkString);
        if (network.length != this.byteRepresentation().length) {
            String msg = "Network must have length " + this.byteRepresentation().length;
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }

        // bitwise compare
        int addrInt = 0, netInt = 0;
        for (int i = 0; i < address.length; i++) {
            addrInt = (addrInt << 8) | (address[i] & 0xFF);
            netInt  = (netInt  << 8) | (network[i] & 0xFF);
        }
        int maskBits = (mask == 0) ? 0 : (~0 << (8 * address.length - mask));
        boolean result = (addrInt & maskBits) == (netInt & maskBits);
        logger.debug("[" + CLS + "] isInSubnet(" + networkString + "/" + mask + ") -> " + result);
        return result;
    }

    @Override
    public void setAddress(String newAddress) {
        super.setAddress(this.parse(newAddress));
        logger.info("[" + CLS + "] address updated to " + stringRepresentation());
    }

    /**
     * Updates the address and its subnet prefix.
     *
     * @param newAddress the new address string
     * @param newPrefix the new subnet prefix
     */
    public void setAddress(String newAddress, int newPrefix) {
        super.setAddress(this.parse(newAddress));
        this.mask.setPrefix(newPrefix);
        logger.info("[" + CLS + "] address updated to " 
                    + stringRepresentation() + "/" + newPrefix);
    }

    /**
     * Updates the subnet prefix length.
     *
     * @param newMask the new prefix length
     */
    public void setMask(int newMask) {
        this.mask.setPrefix(newMask);
        logger.info("[" + CLS + "] mask updated to /" + newMask);
    }

    /**
     * Returns the current subnet prefix length.
     *
     * @return the prefix length
     */
    public int getMask() {
        return this.mask.getPrefix();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().isInstance(this)) {
            logger.debug("[" + CLS + "] equals() false: incompatible type or null");
            return false;
        }
        IP other = (IP) obj;
        boolean eq = Arrays.equals(this.byteRepresentation(), other.byteRepresentation())
                  && this.getMask() == other.getMask();
        logger.debug("[" + CLS + "] equals() -> " + eq);
        return eq;
    }

    @Override
    public int hashCode() {
        int h = Arrays.hashCode(this.address) * 31 + mask.hashCode();
        logger.debug("[" + CLS + "] hashCode() -> " + h);
        return h;
    }

    public abstract boolean isLoopback();
    public abstract boolean isMulticast();
    public abstract boolean isBroadcast();
    public abstract boolean isPrivate();
    public abstract boolean isLinkLocal();
    public abstract boolean isUnspecified();
    public abstract boolean isSubnet();
}