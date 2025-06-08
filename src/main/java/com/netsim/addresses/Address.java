package com.netsim.addresses;

import java.util.Arrays;

public abstract class Address {
    protected int bytesLen;
    protected byte[] address;

    /**
     * @param addressString the address of the IPv4
     * @param maskString the mask of the subnet
     * @throws IllegalArgumentException if address it's not valid when parsing
     */
    public Address(String addressString, int bytes) throws IllegalArgumentException {
        this.bytesLen = bytes;
        byte[] byteRepr = this.parse(addressString);
        if(byteRepr.length != bytes)
            throw new IllegalArgumentException("Invalid addressString, must be bytes long");
        
        this.setAddress(byteRepr);
    }

    /**
     * @param addressString the 4 byte address of the object
     * @throws IllegalArgumentException if address it's not valid when parsing
     */
    public Address(String addressString) throws IllegalArgumentException {
        byte[] byteRepr = this.parse(addressString);
        this.bytesLen = byteRepr.length;
        this.setAddress(byteRepr);
    }

    /**
     * 
     * @param address the address that will be parsed
     * @return the byte representation of the address
     */
    protected abstract byte[] parse(String address);
    /**
     * 
     * @param newAddress the new address that wil be set
     */
    public abstract void setAddress(String newAddress);

    /**
     * 
     * @param newAddress the byte representation of the address that will be assigned if it's valid
     * @throws IllegalArgumentException if the address it's valid
     */
    protected void setAddress(byte[] newAddress) {
        if(newAddress == null || newAddress.length != this.bytesLen) 
            throw new IllegalArgumentException(
                "New address must be " + this.bytesLen + " bytes long"
            );

        this.address = newAddress.clone();
    }

    /**
     * @return byte array of the address
     * @throws NullPointerException if address is null
     */
    public byte[] byteRepresentation() {
        if(this.address == null)
            throw new NullPointerException("Address is not defined");
        
        return this.address;
    }

    /**
     * @return string representation of the address bytes arrray
     * @throws NullPointerException if address is null
     */
    public String stringRepresentation() {
        if(this.address == null)
            throw new NullPointerException("Address is not defined");

        byte[] octets = this.byteRepresentation(); // o toByte()
        StringBuilder sb = new StringBuilder(3 * octets.length + octets.length - 1);
        for (int i = 0; i < octets.length; i++) {
            sb.append(octets[i] & 0xFF);
            if (i < octets.length - 1) {
                sb.append('.');
            }
        }
        return sb.toString();
    }
    
    @Override   
    public boolean equals(Object obj) {
        if(obj == null)
            return false;

        if(!obj.getClass().isInstance(this))
            return false;

        Address other = (Address)obj;

        return Arrays.equals(this.byteRepresentation(), other.byteRepresentation());
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(this.address);
        result = 31 * result;
        return result;
    }
}