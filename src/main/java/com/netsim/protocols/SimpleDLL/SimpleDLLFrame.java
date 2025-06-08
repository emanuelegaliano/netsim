// src/main/java/com/netsim/standard/SimpleDLL/SimpleDLLFrame.java
package com.netsim.protocols.SimpleDLL;

import com.netsim.networkstack.PDU;
import com.netsim.addresses.Mac;

import java.nio.ByteBuffer;

/**
 * A simple Data Link Layer frame that prepends destination and source MAC addresses
 * to an encapsulated PDU payload.
 */
public class SimpleDLLFrame extends PDU {
      private final byte[] payload;

      /**
       * Constructs a new SimpleDLLFrame.
       *
       * @param srcMac   the source MAC address (non‐null)
       * @param dstMac   the destination MAC address (non‐null)
       * @param payload  the encapsulated PDU (non‐null)
       * @throws IllegalArgumentException if any argument is null
       */
      public SimpleDLLFrame(Mac srcMac, Mac dstMac, byte[] payload) throws IllegalArgumentException {
            super(srcMac, dstMac);
            if(srcMac == null || dstMac == null)
                  throw new IllegalArgumentException("SimpleDLLFrame: neither srcMac nor dstMac can be null");
            if(payload == null || payload.length == 0) 
                  throw new IllegalArgumentException("SimpleDLLFrame: payload cannot be null or have length 0");

            this.payload = payload;
      }

      /**
       * Builds the Data Link header consisting of:
       *   [6 bytes destination MAC][6 bytes source MAC]
       *
       * @return a byte array of length 12 containing dstMAC || srcMAC
       */
      @Override
      public byte[] getHeader() {

            byte[] srcBytes = this.source.byteRepresentation(); // 6 bytes
            byte[] dstBytes = this.destination.byteRepresentation(); // 6 bytes

            ByteBuffer buf = ByteBuffer.allocate(dstBytes.length + srcBytes.length);
            buf.put(dstBytes);
            buf.put(srcBytes);
            return buf.array();
      }

      /**
       * Serializes the entire frame:
       *   [header (12 bytes)] || [payload bytes]
       *
       * @return a byte array containing header || payload.toByte()
       */
      @Override
      public byte[] toByte() {
            byte[] header = getHeader();
            byte[] body = this.payload;
            ByteBuffer buf = ByteBuffer.allocate(header.length + body.length);
            buf.put(header).put(body);
            return buf.array();
      }
}