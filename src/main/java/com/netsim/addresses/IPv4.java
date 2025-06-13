package com.netsim.addresses;

import java.util.Arrays;

public class IPv4 extends IP {

    /**
     * Constructs an IPv4 address using a dotted-decimal address and subnet mask string.
     *
     * @param addressString the IPv4 address (e.g., "192.168.1.1")
     * @param maskString the subnet mask (e.g., "255.255.255.0")
     * @throws IllegalArgumentException by super constructor
     */
    public IPv4(String addressString, String maskString) throws IllegalArgumentException {
        super(addressString, maskString, 4);
    }

    /**
     * Constructs an IPv4 address using a dotted-decimal address and prefix length.
     *
     * @param addressString the IPv4 address
     * @param maskPrefix the subnet prefix length (e.g., 24 for 255.255.255.0)
     * @throws IllegalArgumentException by super constructor
     */
    public IPv4(String addressString, int maskPrefix) throws IllegalArgumentException {
        super(addressString, maskPrefix, 4);
    }

    /**
     * Parses a dotted‐decimal IPv4 address into a 4-byte array.
     *
     * @param address the IPv4 address string
     * @return a byte array of length 4
     * @throws IllegalArgumentException if the address is not a valid IPv4 format
     */
    protected byte[] parse(String address) {
        if(address == null)
            throw new IllegalArgumentException("Address string cannot be null");

        // split with limit to catch empty parts (e.g. "1.2..4")
        String[] parts = address.trim().split("\\.", -1);
        if(parts.length != 4) {
            throw new IllegalArgumentException(
                "Invalid IPv4 format: must contain exactly 4 octets, got " 
                + parts.length + " in \"" + address + "\""
            );
        }

        byte[] octets = new byte[4];
        for(int i = 0; i < 4; i++) {
            String part = parts[i];
            if(part.isEmpty()) {
                throw new IllegalArgumentException(
                    "Octet #" + (i+1) + " is empty in \"" + address + "\""
                );
            }

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
                    "Octet #" + (i+1) + " out of range (0–255): " + val
                );
            }

            octets[i] = (byte) val;
        }

        return octets;
    }

    public boolean isLoopback() {
        return isInSubnet("127.0.0.0", 8);
    }

    public boolean isMulticast() {
        return isInSubnet("224.0.0.0", 4);
    }

    public boolean isBroadcast() {
        for(byte b : this.address) {
            if ((b & 0xFF) != 0xFF) {
                return false;
            }
        }
        return true;
    }

    public boolean isPrivate() {
        return isInSubnet("10.0.0.0", 8)
            || isInSubnet("172.16.0.0", 12)
            || isInSubnet("192.168.0.0", 16);
    }

    public boolean isLinkLocal() {
        return isInSubnet("169.254.0.0", 16);
    }

    public boolean isUnspecified() {
        for(byte b : address) {
            if(b != 0) return false;
        }
        return true;
    }

    public boolean isSubnet() throws IllegalArgumentException {
        int prefix = this.mask.getPrefix(); // es. 24
        if (prefix < 0 || prefix > 32)
            throw new IllegalStateException("Invalid mask prefix: " + prefix);

        // costruisce la maschera in byte
        byte[] maskBytes = new Mask(prefix, 4).byteRepresentation();
        byte[] addrBytes = this.byteRepresentation();

        // verifica che addrBytes & ~maskBytes == 0 in tutti gli ottetti
        for(int i = 0; i < 4; i++) {
            int hostBits = addrBytes[i] & (~maskBytes[i]);
            if (hostBits != 0)
                return false;
        }

        return true;
    }

    /** @return global broadcast */
    public static IPv4 broadcast() {
        return new IPv4("255.255.255.255", 0);
    }

    /** @return broadcast based on the current subnet */
    public IPv4 subnetBroadcast() {
        String[] ipParts = this.stringRepresentation().split("\\.");
        int ip = 0;

        for (int i = 0; i < 4; i++)
            ip |= (Integer.parseInt(ipParts[i]) << (24 - (8 * i)));

        int subnetMask = 0xFFFFFFFF << (32 - this.mask.getPrefix());

        int broadcast = ip | (~subnetMask);

        String broadcastStr = 
            ((broadcast >> 24) & 0xFF) + "." +
            ((broadcast >> 16) & 0xFF) + "." +
            ((broadcast >> 8) & 0xFF) + "." +
            (broadcast & 0xFF);

        return new IPv4(broadcastStr, this.mask.getPrefix());
    }
}
