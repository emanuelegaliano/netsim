package com.netsim.addresses;

public class Mac extends Address {
    /**
     * It set a Address sub-type of 6 bytes
     * @param address the address that will be set
     * @throws IllegalArgumentExceptiion if the address is not valid when parsing
     */
    public Mac(String address) throws IllegalArgumentException {
        super(address, 6);
    }
    
    /**
     *  @throws IllegalArgumentException if address is null,
     *         does not consist of exactly 4 parts, any part is empty,
     *         not a valid integer, or not in the range 0–255
     */
    protected byte[] parse(String address) throws IllegalArgumentException {
        if(address == null) 
            throw new IllegalArgumentException("MAC string cannot be null");
        // split solo sui due punti
        String[] parts = address.trim().split(":");
        if(parts.length != 6) 
            throw new IllegalArgumentException(
                "Invalid MAC format: expected 6 hex octets separated by ':', got "
                + parts.length + " in \"" + address + "\""
            );
        

        byte[] octets = new byte[6];
        for(int i = 0; i < 6; i++) {
            String part = parts[i];
            if(part.length() != 2) 
                throw new IllegalArgumentException(
                    "Invalid MAC octet length at index " + i 
                    + ": expected 2 hex digits, got \"" + part + "\""
                );

            int val;
            try {
                val = Integer.parseInt(part, 16);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException(
                    "MAC octet #" + (i + 1) + " is not valid hex: \"" 
                    + part + "\"", e
                );
            }
            octets[i] = (byte) val;
        }
        return octets;
    }

    /**
     * @throws IllegalArgumentException if the newAddress is not 6 bytes long
     */
    public void setAddress(String newAddress) throws IllegalArgumentException {
        byte[] newByteAddress = this.parse(newAddress);
        if(newByteAddress.length != 6)
            throw new IllegalArgumentException("Mac address must be 6 bytes");

        super.setAddress(newByteAddress);
    }

    @Override
    public String stringRepresentation() {
        if(this.address == null) 
            throw new NullPointerException("Address is not defined");
        byte[] octets = byteRepresentation();
        StringBuilder sb = new StringBuilder(octets.length * 3 - 1);

        for(int i = 0; i < octets.length; i++) {
            int unsigned = octets[i] & 0xFF;
            String hex = Integer.toHexString(unsigned).toUpperCase();

            if(hex.length() == 1)
                sb.append('0');
    
            sb.append(hex);

            if(i < octets.length - 1)
                sb.append(':');
        }

        return sb.toString();
    }
    

    public static Mac broadcast() {
        return new Mac("FF:FF:FF:FF:FF:FF");
    }

    /**
     * Converts a 6‐byte array into the usual “xx:xx:xx:xx:xx:xx” format
     * so that we can pass it into the Mac(String) constructor.
     * @param sixBytes the bytes of mac
     * @return a new mac address string
     * @throws IllegalArgumentException if either sixBytes is null or its length is 0
     */
    public static Mac bytesToMac(byte[] sixBytes) throws IllegalArgumentException {
        if(sixBytes == null || sixBytes.length != 6) 
                throw new IllegalArgumentException(
                "SimpleDLLProtocol.bytesToMac: must pass exactly 6 bytes"
                );

        StringBuilder sb = new StringBuilder(17);
        for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", sixBytes[i] & 0xFF));
                if (i < 5) {
                sb.append(':');
                }
        }
        return new Mac(sb.toString());
    }
}
