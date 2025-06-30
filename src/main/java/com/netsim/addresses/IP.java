package com.netsim.addresses;

import java.util.Arrays;
import com.netsim.utils.Logger;

/**
 * Represents an IP address with an associated subnet mask.
 * Provides common operations for subnet checks and address updates.
 */
public abstract class IP extends Address {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = IP.class.getSimpleName();

    protected Mask mask;

    /**
     * Constructs an IP address from a textual address and prefix length.
     *
     * @param addressString the IP address in dotted‐decimal form
     * @param prefix        the subnet prefix length (0–8*bytes)
     * @param bytes         number of address bytes (e.g. 4 for IPv4)
     * @throws IllegalArgumentException if parsing fails or prefix invalid
     */
    protected IP(String addressString, int prefix, int bytes) throws IllegalArgumentException {
        super(addressString, bytes);
        this.mask = new Mask(prefix, bytes);
        logger.info("[" + CLS + "] constructed " + this.stringRepresentation() + "/" + prefix);
    }

    /**
     * Constructs an IP address from a textual address and mask string.
     *
     * @param addressString the IP address in dotted‐decimal form
     * @param maskString    the subnet mask in dotted‐decimal form
     * @param bytes         number of address bytes (e.g. 4 for IPv4)
     * @throws IllegalArgumentException if parsing fails or mask invalid
     */
    protected IP(String addressString, String maskString, int bytes) throws IllegalArgumentException {
        super(addressString, bytes);
        this.mask = new Mask(maskString, bytes);
        logger.info("[" + CLS + "] constructed " + this.stringRepresentation() + " mask=" + maskString);
    }

    /**
     * Checks whether this IP lies within a given subnet.
     *
     * @param networkString the network address in dotted‐decimal form
     * @param mask          the subnet prefix length
     * @return true if this address is in the subnet, false otherwise
     * @throws IllegalArgumentException if networkString is null or invalid
     */
    public boolean isInSubnet(String networkString, int mask) throws IllegalArgumentException {
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
        int addrInt = 0;
        int netInt  = 0;
        for (int i = 0; i < this.address.length; i++) {
            addrInt = (addrInt << 8) | (this.address[i] & 0xFF);
            netInt  = (netInt  << 8) | (network[i] & 0xFF);
        }
        int maskBits = (mask == 0) ? 0 : (~0 << (8 * this.address.length - mask));
        boolean result = (addrInt & maskBits) == (netInt & maskBits);
        logger.debug("[" + CLS + "] isInSubnet(" + networkString + "/" + mask + ") -> " + result);
        return result;
    }

    /**
     * Sets the IP address from a new textual form.
     *
     * @param newAddress the new IP address in dotted‐decimal form
     * @throws IllegalArgumentException if parsing fails
     */
    @Override
    public void setAddress(String newAddress) throws IllegalArgumentException {
        super.setAddress(this.parse(newAddress));
        logger.info("[" + CLS + "] address updated to " + this.stringRepresentation());
    }

    /**
     * Sets the IP address and updates the subnet prefix.
     *
     * @param newAddress the new IP address in dotted‐decimal form
     * @param newPrefix  the new subnet prefix length
     * @throws IllegalArgumentException if parsing fails or prefix invalid
     */
    public void setAddress(String newAddress, int newPrefix) throws IllegalArgumentException {
        super.setAddress(this.parse(newAddress));
        this.mask.setPrefix(newPrefix);
        logger.info("[" + CLS + "] address updated to " + this.stringRepresentation() + "/" + newPrefix);
    }

    /**
     * Updates the subnet prefix length.
     *
     * @param newMask the new subnet prefix length
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
        if (obj == null || !(obj.getClass().isInstance(this))) {
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
        int h = Arrays.hashCode(this.address) * 31 + this.mask.hashCode();
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
