package com.netsim.addresses;

public class Mask extends Address {
    private int prefix;

    /**
     * @param prefix the prefix of the subnet: x.x.x.x/prefix
     * @param bytes the length in bytes of the mask (e.g. 4 bytes)
     * @throws IllegalArgumentException if the string mask build from 
     *         prefix it's not valid when parsing
     */
    public Mask(int prefix, int bytes) throws IllegalArgumentException {
        super(buildMaskString(prefix, bytes), bytes);
        this.prefix = prefix;
    }

    /**
     * Constructor of Mask using String notation "x.x.x.x"
     * @param address used to build the mask and count bytes
     * @param bytes the length in bytes of the mask (e.g. 4 bytes)
     * @throws IllegalArgumentException if address it's valid when parsing
     */
    public Mask(String address, int bytes) throws IllegalArgumentException {
        super(address, bytes);

        boolean zeroFound = false;
        int computedPrefix = 0;

        for(byte b : this.address) {
            int unsignedOctet = b & 0xFF;
            for(int bitIndex = 7; bitIndex >= 0; bitIndex--) {
                boolean isOne = ((unsignedOctet >> bitIndex) & 1) == 1;
                if(isOne) {
                    if(zeroFound)
                        throw new IllegalArgumentException("Non-Contigue mask: " + address);
                    
                    computedPrefix++;
                } else {
                    zeroFound = true;
                }
            }
        }

        this.prefix = computedPrefix;
    }

    /**
     * 
     * @param prefix the prefix used to build the mask (e.g. 24 = 255.255.255.0)
     * @param bytes the length in bytes of the mask
     * @return
     */
    public static String buildMaskString(int prefix, int bytes) {
        int fullBytes = prefix / 8;
        int remBits = prefix % 8;
        StringBuilder sb = new StringBuilder(bytes * 4);

        for (int i = 0; i < bytes; i++) {
            int octet;
            if (i < fullBytes) {
                octet = 0xFF;
            } else if (i == fullBytes) {
                // e.g. prefix=20 → remBits=4 → 0xFF << 4 = 11110000b = 240
                octet = ((0xFF << (8 - remBits)) & 0xFF);
            } else {
                octet = 0;
            }

            sb.append(octet);
            if (i < bytes - 1) sb.append('.');
        }

        return sb.toString();
    }

    /**
     * @throws IllegalArgumentException if:
     * newAddress is null, 
     * length is > byteLen of Mask, 
     * either octet is null or not valid (not in range 0-255).
     */
    public void setAddress(String newAddress) throws IllegalArgumentException {
        this.parse(newAddress);
    }

    /**
     * @param newPrefix the new prefix that will replace old prefix
     */
    public void setPrefix(int newPrefix) {
        this.prefix = newPrefix;
    }

    /**
     * @throws IllegalArgumentException if:
     * - newAddress is null
     * - length is > byteLen of Mask
     * - either octet is null or not valid (not in range 0-255)
     */
    protected byte[] parse(String address) {
        if(address == null)
            throw new IllegalArgumentException("Address string cannot be null");
        
        String[] parts = address.trim().split("\\.");
        if(parts.length != this.bytesLen) 
            throw new IllegalArgumentException(
                "Invalid IPv4 format: must contain exactly " 
                + this.bytesLen + " octets, got " 
                + parts.length + " in \"" + address + "\""    
            );

        
        byte[] octets = new byte[this.bytesLen];
        for(int i = 0; i < this.bytesLen; i++) {

            String part = parts[i];
            if(part.isEmpty())
                throw new IllegalArgumentException(
                    "Octet #" + (i+1) + " is empty in \"" + address + "\""
                );
            
            int val;
            try {
                val = Integer.parseInt(part);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Octet #" + (i+1) + " is not a valid integer: \"" + part + "\"", 
                    e
                );
            }

            if(val < 0 || val > 255) {
                throw new IllegalArgumentException(
                    "Octet #" + (i+1) + " out of range (0-255): " + val
                );
            }

            octets[i] = (byte) val;
        }

        return octets;
    }

    public int getPrefix() {
        return this.prefix;
    }
}
