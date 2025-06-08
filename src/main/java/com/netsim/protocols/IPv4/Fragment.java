package com.netsim.protocols.IPv4;

/**
 * helper class for IPv4 decapsulation
 */
class Fragment {
      int offset;       // in bytes, actual data offset within reassembled payload
      byte[] data;

      Fragment(int offset, byte[] data) {
            this.offset = offset;
            this.data = data;
      }
}