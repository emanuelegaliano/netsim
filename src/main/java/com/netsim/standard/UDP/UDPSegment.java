package com.netsim.standard.UDP;

import com.netsim.networkstack.PDU;

import java.nio.ByteBuffer;

import com.netsim.addresses.Port;

/**
 * Simplified version of UDP without checksum
 */
public class UDPSegment extends PDU {
      private final short sequenceNumber;
      private final short length;
      private final byte[] payload;

      /**
     * Constructs a new UDPSegment.
     *
     * @param source the source port (non-null)
     * @param destination the destination port (non-null)
     * @param sequenceNumber an application-defined sequence number
     * @param payload the payload data (non-null, non-empty)
     * @throws IllegalArgumentException if payload is null or empty
     */
      public UDPSegment(Port source, Port destination, int sequenceNumber, byte[] payload) 
      throws IllegalArgumentException {
            super(source, destination);

            if(source == null || destination == null)
                  throw new IllegalArgumentException("UDPSegment: source or destination cannot be null");
            
            if(payload == null || payload.length == 0)
                  throw new IllegalArgumentException("UDPSegment: payload must be valid");

            if(sequenceNumber > Short.MAX_VALUE)
                  throw new IllegalArgumentException("UDPSegment: sequence number is too large");

            this.sequenceNumber = (short) sequenceNumber;
            this.payload = payload.clone();
            this.length = this.calculateLength();
      }

      /**
     * Calculates the total segment length (header + payload) in bits.
     *
     * @return the total length in bits
     */
      private short calculateLength() {
            // compute total bytes of header + payload
            int headerBytes = this.getHeader().length;
            int payloadBytes = payload.length;
            int totalBytes = headerBytes + payloadBytes;

            // convert to bits
            int totalBits = totalBytes * Byte.SIZE; // Byte.SIZE == 8

            // guard against overflow of a signed 16-bit value
            if(totalBits > Short.MAX_VALUE) 
                  throw new IllegalArgumentException(
                        "Segment too large to encode length in 16 bits: " + totalBits + " bits"
                  );

            return (short) totalBits;
      }

      /**
     * Retrieves the application-defined sequence number.
     *
     * @return the sequence number
     */
      public short getSequenceNumber() {
            return this.sequenceNumber;
      }

      /**
     * Retrieves the total segment length in bits.
     *
     * @return the segment length in bits
     */
      public short getLength() {
            return this.length;
      }

       /**
     * Constructs the raw header bytes for this segment.
     * Layout: 
     * - source port: 16 bits
     * - destination port: 16 bits
     * - sequence number: 16 bits
     * - length: 16 bits
     * @return a byte array containing the header fields in network byte order
     */
      protected byte[] getHeader() {
            byte[] srcBytes = this.source.byteRepresentation();
            byte[] dstBytes = this.destination.byteRepresentation();

            ByteBuffer buf = ByteBuffer.allocate(
                  srcBytes.length + dstBytes.length + Short.BYTES + Short.BYTES
            );

            buf.put(srcBytes)
               .put(dstBytes)
               .putShort(this.sequenceNumber)
               .putShort(this.length);
            
            return buf.array();
      }

      /**
       * Serializes this UDPSegment to a byte array.
       * This consists solely of the header bytes  + payload
       *
       * @return the segment as a byte array ready for transmission
       */
      @Override
      public byte[] toByte() {
            byte[] headerBytes = getHeader();

            ByteBuffer buf = ByteBuffer.allocate(headerBytes.length + payload.length);

            buf.put(headerBytes)
               .put(payload);
            
            return buf.array();
      }
}
