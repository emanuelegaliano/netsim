package com.netsim.addresses;

import java.util.Arrays;
import com.netsim.utils.Logger;

/**
 * Represents a generic address parsed from a string into a byte array.
 * Provides common functionality for byte‐level and string representations.
 */
public abstract class Address {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = Address.class.getSimpleName();

    protected int bytesLen;
    protected byte[] address;

    /**
     * Constructs an Address by parsing the given string into a fixed‐length byte array.
     *
     * @param addressString the textual representation of the address (e.g. "192.168.1.1")
     * @param bytes         the expected length of the parsed byte array
     * @throws IllegalArgumentException if parsing fails or the resulting byte array length is incorrect
     */
    public Address(String addressString, int bytes) throws IllegalArgumentException {
        logger.info("[" + CLS + "] constructing from \"" + addressString + "\", expecting " + bytes + " bytes");
        this.bytesLen = bytes;
        byte[] byteRepr = this.parse(addressString);
        if (byteRepr.length != this.bytesLen) {
            String msg = "Invalid addressString length, must be " + this.bytesLen + " bytes";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        this.setAddress(byteRepr);
        logger.info("[" + CLS + "] constructed successfully: " + this.stringRepresentation());
    }

    /**
     * Constructs an Address by parsing the given string into a byte array of inferred length.
     *
     * @param addressString the textual representation of the address
     * @throws IllegalArgumentException if parsing fails
     */
    public Address(String addressString) throws IllegalArgumentException {
        logger.info("[" + CLS + "] constructing from \"" + addressString + "\"");
        byte[] byteRepr = this.parse(addressString);
        this.bytesLen = byteRepr.length;
        this.setAddress(byteRepr);
        logger.info("[" + CLS + "] constructed successfully: " + this.stringRepresentation());
    }

    /**
     * Parses a textual form into a raw byte array.
     *
     * @param address the textual address to parse
     * @return the raw byte array representation
     * @throws IllegalArgumentException if the input is invalid
     */
    protected abstract byte[] parse(String address) throws IllegalArgumentException;

    /**
     * Sets the address from a new textual form (to be implemented by subclasses).
     *
     * @param newAddress the new textual address
     * @throws IllegalArgumentException if the input is invalid
     */
    public abstract void setAddress(String newAddress) throws IllegalArgumentException;

    /**
     * Sets the internal byte array representation.
     *
     * @param newAddress the raw byte array to assign
     * @throws IllegalArgumentException if the array is null or of incorrect length
     */
    protected void setAddress(byte[] newAddress) throws IllegalArgumentException {
        if (newAddress == null || newAddress.length != this.bytesLen) {
            String msg = "New address must be " + this.bytesLen + " bytes long";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        this.address = newAddress.clone();
        logger.info("[" + CLS + "] byte address set to " + this.stringRepresentation());
    }

    /**
     * Returns a clone of the internal byte array representation.
     *
     * @return the raw byte array
     * @throws NullPointerException if the address is not defined
     */
    public byte[] byteRepresentation() throws NullPointerException {
        if (this.address == null) {
            String msg = "Address is not defined";
            logger.error("[" + CLS + "] " + msg);
            throw new NullPointerException(msg);
        }
        return this.address.clone();
    }

    /**
     * Returns the textual (dotted or colon‐separated) form of the address.
     *
     * @return the string form of the address
     * @throws NullPointerException if the address is not defined
     */
    public String stringRepresentation() throws NullPointerException {
        if (this.address == null) {
            String msg = "Address is not defined";
            logger.error("[" + CLS + "] " + msg);
            throw new NullPointerException(msg);
        }

        StringBuilder sb = new StringBuilder(this.bytesLen * 4);
        for (int i = 0; i < this.address.length; i++) {
            sb.append(this.address[i] & 0xFF);
            if (i < this.address.length - 1) {
                sb.append('.');
            }
        }
        return sb.toString();
    }

    /**
     * Compares this Address to another for byte‐wise equality.
     *
     * @param obj the object to compare
     * @return true if both are Address instances with identical byte arrays
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().isInstance(this)) {
            logger.debug("[" + CLS + "] equals() false: incompatible type or null");
            return false;
        }
        boolean eq = Arrays.equals(this.byteRepresentation(), ((Address) obj).byteRepresentation());
        logger.debug("[" + CLS + "] equals() result with "
                     + obj.getClass().getSimpleName() + ": " + eq);
        return eq;
    }

    /**
     * Computes a hash code based on the address byte array.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int h = Arrays.hashCode(this.address);
        logger.debug("[" + CLS + "] hashCode() = " + h);
        return h;
    }
}
