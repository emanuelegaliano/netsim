package com.netsim.addresses;

import java.util.Arrays;

/**
 * Abstract base class representing an IP address (e.g., IPv4, IPv6).
 * It encapsulates the address bytes and a subnet mask, and provides
 * common methods for subnet operations and address classification.
 */
public abstract class IP extends Address {
    protected Mask mask;

    /**
     * Constructs an IP address with the given address string and prefix length.
     *
     * @param addressString the IP address in string format (e.g., "192.168.0.1")
     * @param bytes number of bytes for the address (4 for IPv4, 16 for IPv6)
     * @param prefix the subnet prefix length
     * @throws IllegalArgumentException by super constructor
     */
    protected IP(String addressString, int prefix, int bytes) throws IllegalArgumentException {
        super(addressString, bytes);
        this.mask = new Mask(prefix, bytes);
    }

    /**
     * Constructs an IP address with the given address and mask strings.
     *
     * @param addressString the IP address
     * @param maskString the subnet mask
     * @param bytes number of bytes in the address
     * @throws IllegalArgumentException by super constructor
     */
    protected IP(String addressString, String maskString, int bytes) throws IllegalArgumentException {
        super(addressString, bytes);
        this.mask = new Mask(maskString, bytes);
    }

    /**
     * Checks if this IP address belongs to a given subnet.
     *
     * @param networkString the network address as string (e.g., "192.168.1.0")
     * @param mask the subnet prefix length
     * @return true if this IP is within the subnet, false otherwise
     * @throws IllegalArgumentException if the network address or mask is invalid
     */
    public boolean isInSubnet(String networkString, int mask) throws IllegalArgumentException {
        if(networkString == null) {
            throw new IllegalArgumentException("Network string cannot be null");
        }

        byte[] network = this.parse(networkString);

        if(network == null || network.length != this.address.length) {
            throw new IllegalArgumentException(
                "Network must be non-null and have length " + this.address.length
            );
        }

        int addrInt = 0, netInt = 0;
        for(int i = 0; i < this.address.length; i++) {
            addrInt = (addrInt << 8) | (this.address[i] & 0xFF);
            netInt = (netInt << 8) | (network[i] & 0xFF);
        }

        int maskBits = (mask == 0)
                     ? 0
                     : (~0) << (8 * this.address.length - mask);

        return (addrInt & maskBits) == (netInt & maskBits);
    }

    /**
     * Updates the address with a new string value.
     *
     * @param newAddress the new address string
     */
    public void setAddress(String newAddress) throws IllegalArgumentException {
        this.address = this.parse(newAddress);
    }

    /**
     * Updates the address and its subnet prefix.
     *
     * @param newAddress the new address string
     * @param newPrefix the new subnet prefix
     */
    public void setAddress(String newAddress, int newPrefix) throws IllegalArgumentException {
        this.address = this.parse(newAddress);
        this.mask.setPrefix(newPrefix);
    }

    /**
     * Updates the subnet prefix length.
     *
     * @param newMask the new prefix length
     */
    public void setMask(int newMask) {
        this.mask.setPrefix(newMask);
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
        if(obj == null)
            return false;

        if(!obj.getClass().isInstance(this))
            return false;

        IP other = (IP)obj;
        return other == this && other.getMask() == this.getMask();
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(this.address);
        result = 31 * result + (this.mask != null ? mask.hashCode() : 0);
        return result;
    }

    public abstract boolean isLoopback();
    public abstract boolean isMulticast();
    public abstract boolean isBroadcast();
    public abstract boolean isPrivate();
    public abstract boolean isLinkLocal();
    public abstract boolean isUnspecified();
    /** @return true if all the host bits are zero, false otherwise */
    public abstract boolean isSubnet();
}