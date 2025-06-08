package com.netsim.protocols.IPv4;

/**
 * Class for having 4 bit Version and 4 bit IP header length 
 * (not possible natively in java)
 */
public final class VersionIHL {
      private byte b;

      /**
       * @param version 4-bit version (e.g. 4 for IPv4)
       * @param ihl the 4-bit header length (in 32-bit words)
       */
      public VersionIHL(int version, int ihl) throws IllegalArgumentException {
            if(version  < 0 || version > 0xF) 
                  throw new IllegalArgumentException("VersionIHL: version bits < 0 or > 0xF (15)");

            if(ihl < 5 || ihl > 0xF) 
                  throw new IllegalArgumentException("VersionIHL: IP Header length < 5 or > OxF (15)");
            
            this.b = (byte)((version << 4) | (ihl & 0xF));
      }

      /** @return the high-order 4 bits (0…15) */
      public int getVersion() {
            return (b >>> 4) & 0xF;
      }

      /** @return the low-order 4 bits (0…15) */
      public int getIhl() {
            return b & 0xF;
      }

      /** @return the raw combined byte, for serialization */
      public byte toByte() {
            return b;
      }

      /** build from a raw byte (e.g. when parsing) */
      public static VersionIHL fromByte(byte raw) {
            int version = (raw >>> 4) & 0xF;
            int ihl = raw & 0xF;
            return new VersionIHL(version, ihl);
      }
}
