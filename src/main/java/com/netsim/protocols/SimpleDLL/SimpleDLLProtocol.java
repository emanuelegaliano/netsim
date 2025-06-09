package com.netsim.protocols.SimpleDLL;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import com.netsim.addresses.Mac;
import com.netsim.networkstack.Protocol;


public class SimpleDLLProtocol implements Protocol {
      private final Mac source;
      private final Mac destination;

      private Protocol nextProtocol;
      private Protocol previousProtocol;

      /**
       * @param source mac address of source
       * @param destination mac address of destination
       * @throws IllegalArgumentException if soruce or destination is null
       */
      public SimpleDLLProtocol(Mac source, Mac destination) throws IllegalArgumentException {
            if(source == null || destination == null)
                  throw new IllegalArgumentException("SimpleDLLProtocol: source or destination cannot be null");
                  
            this.source = source;
            this.destination = destination;
            
            this.nextProtocol = null;
            this.previousProtocol = null;
      }

      /**
       * encapsulate packets in frames
       * @param packets a list (fragmentation possible from IP) of packets
       * @return a list of frames
       * @throws IllegalArgumentException if packets is null or is empty,
       *                                  if something goes wrong while adding frames
       * @throws NullPointerException if next protocol is not defined
       */
      public byte[] encapsulate(byte[] packets) throws IllegalArgumentException, NullPointerException {
            if(packets == null || packets.length == 0)
                  throw new IllegalArgumentException("SimpleDLLProtocol: packets required");
            if(this.nextProtocol == null)
                  throw new NullPointerException("SimpleDLLProtocol: next protocol is null");

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Walk through the stream, extracting each IP packet
            int offset = 0;
            while(offset < packets.length) {
                  if(offset + 4 > packets.length) 
                        throw new IllegalArgumentException("SimpleDLLProtocol: truncated IP packet at offset " + offset);
                  // IHL is low nibble of byte 0
                  int ihl = packets[offset] & 0x0F;
                  if(ihl < 5) 
                        throw new IllegalArgumentException("SimpleDLLProtocol: invalid IHL=" + ihl + " at offset " + offset);
                  
                  int headerBytes = ihl * 4;
                  if(offset + headerBytes > packets.length) 
                        throw new IllegalArgumentException("SimpleDLLProtocol: incomplete IP header at offset " + offset);
                  // total length is bytes 2–3
                  int totalLen = ((packets[offset+2]&0xFF)<<8)
                              |  (packets[offset+3]&0xFF);

                  if(totalLen < headerBytes || offset + totalLen > packets.length)
                        throw new IllegalArgumentException("SimpleDLLProtocol: invalid IP totalLen=" + totalLen + " at offset " + offset);

                  // Extract one IP packet
                  byte[] ipPkt = new byte[totalLen];
                  System.arraycopy(packets, offset, ipPkt, 0, totalLen);

                  // Build frame: dst||src||ipPkt
                  SimpleDLLFrame frame = new SimpleDLLFrame(this.source, this.destination, ipPkt);

                  // Append to out
                  out.write(frame.toByte(), 0, 12 + totalLen);

                  offset += totalLen;
            }

            return this.nextProtocol.encapsulate(out.toByteArray());
      }

      /**
       * decapsulate frames into packets
       * @param frames a list of frames with SimpleDLLFrame header
       * @return a list of packets
       * @throws IllegalArgumentException if either frames is null or empty
       * 
       */
      public byte[] decapsulate(byte[] frames) throws IllegalArgumentException, NullPointerException {
            if (frames == null || frames.length < 12)
                  throw new IllegalArgumentException("SimpleDLLProtocol: frames too short");
            if(this.previousProtocol == null)
                  throw new NullPointerException("SimpleDLLProtocol: previous protocol is null");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offset = 0;
            while(offset < frames.length) {
                  int remain = frames.length - offset;
                  if (remain < 12 + 4) 
                        throw new IllegalArgumentException("SimpleDLLProtocol: truncated frame at offset " + offset);
                  // skip 12-byte DLL header, then parse the IP packet
                  int ipOffset = offset + 12;
                  int ihl = frames[ipOffset] & 0x0F;
                  if(ihl < 5) 
                        throw new IllegalArgumentException("SimpleDLLProtocol: invalid IHL=" + ihl + " at frame offset " + offset);
                  
                  int headerBytes = ihl * 4;
                  if(ipOffset + headerBytes > frames.length) 
                        throw new IllegalArgumentException("SimpleDLLProtocol: incomplete IP header in frame at offset " + offset);
                  
                  int totalLen = ((frames[ipOffset+2]&0xFF)<<8)
                              |  (frames[ipOffset+3]&0xFF);
                  if(totalLen < headerBytes || ipOffset + totalLen > frames.length) 
                        throw new IllegalArgumentException("invalid IP totalLen=" + totalLen + " at frame offset " + offset);

                  // copy the IP packet out
                  out.write(frames, ipOffset, totalLen);
                  // advance to next frame
                  offset += 12 + totalLen;
            }

            return this.previousProtocol.decapsulate(out.toByteArray());
      }

      public void setNext(Protocol nextProtocol) throws IllegalArgumentException {
            if(nextProtocol == null)
                  throw new IllegalArgumentException("SimpleDLLProtocol: nextProtocol cannot be null");

            this.nextProtocol = nextProtocol;     
      }

      public void setPrevious(Protocol previousProtocol) throws IllegalArgumentException {
            if(previousProtocol == null)
                  throw new IllegalArgumentException("SimpleDLlProtocol: previousProtocol cannot be null");

            this.previousProtocol = previousProtocol;
      }

      public Mac getSource() {
            return this.source;
      }

      public Mac getDestination() {
            return this.destination;
      }

      /**
       * extracts the bytes from 6 to 12 of destination mac
       * @param frame a byte[] raw array of SimpleDLLFrame
       * @return the mac address of destination
       * @throws IllegalArgumentException if either frame is null or have length < 12
       */
      public Mac extractSource(byte[] frame) {
            if(frame == null || frame.length < 12)
                  throw new IllegalArgumentException("SimpleDLLProtocol: frame too short for source MAC");
            
            return Mac.bytesToMac(Arrays.copyOfRange(frame, 6, 12));
      }

      /**
       * extracts the first 6 bytes of source mac from frame
       * @param frame a byte[] raw array of SimpleDLLFrame
       * @return the mac address of source
       * @throws IllegalArgumentException if either frame is null or have length < 6
       */
      public Mac extractDestination(byte[] frame) {
            if(frame == null || frame.length < 6)
                  throw new IllegalArgumentException("SimpleDLLProtocol: frame too short for destination MAC");
            
            return Mac.bytesToMac(Arrays.copyOfRange(frame, 0, 6));
      }
}