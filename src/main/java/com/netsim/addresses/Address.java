package com.netsim.addresses;

import java.util.Arrays;
import com.netsim.utils.Logger;

public abstract class Address {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = Address.class.getSimpleName();

    protected int bytesLen;
    protected byte[] address;

    /**
     * @param addressString textual address (e.g. "192.168.1.1")
     * @param bytes         expected length of parsed byte array
     */
    public Address(String addressString, int bytes) {
        logger.info("[" + CLS + "] constructing from \"" + addressString + "\", expecting " + bytes + " bytes");
        this.bytesLen = bytes;
        byte[] byteRepr = this.parse(addressString);
        if (byteRepr.length != bytes) {
            String msg = "Invalid addressString length, must be " + bytes + " bytes";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        this.setAddress(byteRepr);
        logger.info("[" + CLS + "] constructed successfully: " + this.stringRepresentation());
    }

    /**
     * @param addressString textual form only; length inferred by parse()
     */
    public Address(String addressString) {
        logger.info("[" + CLS + "] constructing from \"" + addressString + "\"");
        byte[] byteRepr = this.parse(addressString);
        this.bytesLen = byteRepr.length;
        this.setAddress(byteRepr);
        logger.info("[" + CLS + "] constructed successfully: " + this.stringRepresentation());
    }

    /** parse textual form into raw byte[] */
    protected abstract byte[] parse(String address);

    /** set from new textual form (implement in subclass) */
    public abstract void setAddress(String newAddress);

    /**
     * @param newAddress raw bytes to assign
     */
    protected void setAddress(byte[] newAddress) {
        if (newAddress == null || newAddress.length != this.bytesLen) {
            String msg = "New address must be " + this.bytesLen + " bytes long";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        this.address = newAddress.clone();
        logger.info("[" + CLS + "] byte address set to " + stringRepresentation());
    }

    /** @return raw bytes */
    public byte[] byteRepresentation() {
        if (this.address == null) {
            String msg = "Address is not defined";
            logger.error("[" + CLS + "] " + msg);
            throw new NullPointerException(msg);
        }
        return this.address.clone();
    }

    /** @return dotted/string form */
    public String stringRepresentation() {
        if (this.address == null) {
            String msg = "Address is not defined";
            logger.error("[" + CLS + "] " + msg);
            throw new NullPointerException(msg);
        }

        StringBuilder sb = new StringBuilder(this.bytesLen * 4);
        for (int i = 0; i < address.length; i++) {
            sb.append(address[i] & 0xFF);
            if (i < address.length - 1) sb.append('.');
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj.getClass().isInstance(this))) {
            logger.debug("[" + CLS + "] equals() false: incompatible type or null");
            return false;
        }
        boolean eq = Arrays.equals(this.byteRepresentation(), ((Address) obj).byteRepresentation());
        logger.debug("[" + CLS + "] equals() result with " 
                     + obj.getClass().getSimpleName() + ": " + eq);
        return eq;
    }

    @Override
    public int hashCode() {
        int h = Arrays.hashCode(this.address);
        logger.debug("[" + CLS + "] hashCode() = " + h);
        return h;
    }
}