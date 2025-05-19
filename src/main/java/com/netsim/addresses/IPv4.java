package com.netsim.addresses;

public class IPv4 extends Address {
    private Mask mask;

    /**
     * @param addressString the string that will be set as address
     * @param maskString the mask of the address
     * @throws IllegalArgumentException in case the addressString is not valid when parsing
     */
    public IPv4(String addressString, String maskString) throws IllegalArgumentException {
        super(addressString, 4);
        this.mask = new Mask(maskString, 4);
    }

    /**
     * @param addressString the string that will be set as address
     * @param maskPrefix the prefix of the mask
     * @throws IllegalArgumentException in case the addressString is not valid when parsing
     */
    public IPv4(String addressString, int maskPrefix) throws IllegalArgumentException {
        super(addressString, 4);
        this.mask = new Mask(maskPrefix, 4);
    }

    /**
     * Parses a dotted‐decimal IPv4 address into a 4-byte array.
     *
     * @param address the IPv4 address in dotted-decimal notation
     * @return a byte[4] where each element is one octet (0–255)
     * @throws IllegalArgumentException if address is null,
     *         does not consist of exactly 4 parts, any part is empty,
     *         not a valid integer, or not in the range 0–255
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

    /**
     * @param newAddress the param that will be setted instead of Address.address
     */
    protected void setAddress(byte[] newAddress) {
        if(newAddress.length != 4)
            throw new IllegalArgumentException("IPv4 bytes must be 4");

        super.setAddress(newAddress);
    }
    
    /**
     * @param networkString the subnet network string
     * @param mask the mask of the network
     * @return true if it's in subnet, false otherwise
     */
    public boolean isInSubnet(String networkString, int mask) throws IllegalArgumentException {
        if(networkString == null)
            throw new IllegalArgumentException("Network string cannot be null");

        byte[] network = this.parse(networkString);

        if(network == null || network.length != this.address.length) {
            throw new IllegalArgumentException(
                "Network must be non-null and have length " + this.address.length
            );
        }
        int maxPrefix = 8 * this.address.length;
        if(mask < 0 || mask > maxPrefix) {
            throw new IllegalArgumentException(
                "Invalid prefix length: " + mask + ", expected 0-" + maxPrefix
            );
        }

        int addrInt = 0, netInt = 0;
        for(int i = 0; i < this.address.length; i++) {
            addrInt = (addrInt << 8) | (this.address[i] & 0xFF);
            netInt  = (netInt  << 8) | (network[i]     & 0xFF);
        }

        int maskBits = (mask == 0)
                    ? 0
                    : (~0) << (32 - mask);

        return (addrInt & maskBits) == (netInt & maskBits);
    }
    
    /**
     * @param newAddress the new string address
     * @throws IllegalArgumentException if newAddress is null,
     *         does not consist of exactly 4 parts, any part is empty,
     *         not a valid integer, or not in the range 0–255
     */
    public void setAddress(String newAddress) throws IllegalArgumentException {
        byte[] newByteAddress = this.parse(newAddress);
        this.setAddress(newByteAddress);
    }

    /**
     * 
     * @param newAddress the new string address
     * @param newPrefix the new prefix of the subnet mask
     * @throws IllegalArgumentException if newAddress is null,
     *         does not consist of exactly 4 parts, any part is empty,
     *         not a valid integer, or not in the range 0–255
     */
    public void setAddress(String newAddress, int newPrefix) throws IllegalArgumentException {
        this.address = this.parse(newAddress);
        this.mask.setPrefix(newPrefix);
    }

    public void setMask(int newMask) {
        this.mask.setPrefix(newMask);
    }

    public int getMask() {
        return this.mask.getPrefix();
    }

    /**
     * @return true if instance is in subnet 127.0.0.0/8
     */
    public boolean isLoopback() {
        // 127.0.0.0/8
        return isInSubnet("127.0.0.0", 8);
    }

    /**
     * @return true if instance is in subnet 224.0.0.0/4
     */
    public boolean isMulticast() {
        // 224.0.0.0/4
        return isInSubnet("224.0.0.0", 4);
    }

    /**
     * @return true if instance has ip address 255.255.255.255
     */
    public boolean isBroadcast() {
        // 255.255.255.255
        for (byte b : this.address) {
            if ((b & 0xFF) != 0xFF) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * @return true if instance is in one of this subnets:
     * - 10.0.0.0/8
     * - 172.16.0.0/12
     * - 192.168.0.0/16
    */
    public boolean isPrivate() {
        // 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16
        return isInSubnet("10.0.0.0", 8)
            || isInSubnet("172.16.0.0", 12)
            || isInSubnet("192.168.0.0", 16);
    }

    /**
     * @return true if instance is in subnet 169.254.0.0/16
     */
    public boolean isLinkLocal() {
        // 169.254.0.0/16
        return isInSubnet("169.254.0.0", 16);
    }

    /**
     * @return true if instance has ip 0.0.0.0
     */
    public boolean isUnspecified() {
        // 0.0.0.0
        for (byte b : this.address) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }
}