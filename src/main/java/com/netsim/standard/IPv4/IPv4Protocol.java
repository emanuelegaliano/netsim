package com.netsim.standard.IPv4;

import com.netsim.networkstack.Protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import com.netsim.addresses.IPv4;

public class IPv4Protocol implements Protocol {
      private final IPv4 source; 
      private final IPv4 destination;
      private final int version;
      private final int IHL; 
      private final int typeOfService;
      private int identification;
      @SuppressWarnings("unused") // for possible future updates
      private final int flags;
      private int ttl;
      private final int protocol;
      // max transport unit, minimum 20 byte but should be considered 28 byte minimum because of transport layer
      private final int MTU; 
      

      
      private Protocol nextProtocol;
      private Protocol previousProtocol;

      /**
     * Constructs a new IP layer with all header‐field parameters specified.
     *
     * @param source IPv4 source address (non‐null)
     * @param destination IPv4 destination address (non‐null)
     * @param IHL Internet Header Length in 32‐bit words (5…15)
     * @param typeOfService Type‐Of‐Service (0…255)
     * @param identification Identification field (0…65535)
     * @param flags 3‐bit flags value (0…7), where bit‐1=DF, bit‐2=MF
     * @param fragmentOffset Fragment‐Offset (0…8191) – typically 0 for first fragment
     * @param ttl (0…255)
     * @param protocol Upper‐layer protocol number (0…255, e.g. 17 for UDP)
     * @param MTU Maximum transmission unit in bytes (must be >= 20) 
     *            (if transport layer it's been used then 28 byte it's the minumum)
     * @throws IllegalArgumentException if any argument is out of its valid range,
     *                                  or if source/destination is null
     */
      public IPv4Protocol(
            IPv4 source, 
            IPv4 destination, 
            int IHL,
            int typeOfService,
            int identification,
            int flags,
            int ttl,
            int protocol,
            int MTU
            ) throws IllegalArgumentException {
            if(source == null || destination == null)
                  throw new IllegalArgumentException("IP: source/destination cannot be null");

            if(IHL < 5 || IHL > 15)
                  throw new IllegalArgumentException("IP: IHL must be between 5 and 15");

            if(typeOfService < 0 || typeOfService > 0xFF)
                  throw new IllegalArgumentException("IP: typeOfService must be between 0 and 255");
            
            if(identification < 0 || identification > 0xFFFF)
                  throw new IllegalArgumentException("IP: identification must be between 0 and 65535");

            if(flags < 0 || flags > 0x7)
                  throw new IllegalArgumentException("IP: flags must be between 0 and 7");

            if(ttl < 0 || ttl > 0xFF)
                  throw new IllegalArgumentException("IP: TTL must be between 0 and 255");
            
            if(MTU < IHL * 4)
                  throw new IllegalArgumentException(
                        "IP: MTU (" + MTU + ") must be at least header length (" + (IHL * 4) + ")"
                        );

            this.source = source;
            this.destination = destination;
            this.version = 4; // IPv4 version is always 4
            this.IHL = IHL;
            this.typeOfService = typeOfService;
            this.identification = identification;
            this.flags = flags;
            this.ttl = ttl;
            this.protocol = protocol;
            this.MTU = MTU;

            this.nextProtocol = null;
            this.previousProtocol = null;
      }

      public IPv4 getSourceIP() {
            return this.source;
      }

      public IPv4 getDestinationIP() {
            return this.destination;
      }

      public byte[] encapsulate(byte[] upperLayerPDU) throws IllegalArgumentException, RuntimeException {
            if(upperLayerPDU == null || upperLayerPDU.length == 0)
                  throw new IllegalArgumentException("IP: upperLayerPDU cannot be null or empty");

            if(this.nextProtocol == null)
                  throw new RuntimeException("IP: next protocol is null");

            int headerLen = this.IHL * 4;
            if(this.MTU < headerLen + 1)
                  throw new RuntimeException("IP: MTU (" + MTU + ") is too small to fit any payload");

            final int maxDataPerFragment = this.MTU - headerLen;
            final int fragmentUnit = 8;

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int totalPayload = upperLayerPDU.length;
            int offsetBytes = 0;
            int packetID = this.identification;

            while(offsetBytes < totalPayload) {
                  int remaining = totalPayload - offsetBytes;
                  int thisFragmentDataLen;

                  if(remaining > maxDataPerFragment) {
                        int usable = (maxDataPerFragment / fragmentUnit) * fragmentUnit;
                        thisFragmentDataLen = usable;
                  } else {
                        thisFragmentDataLen = remaining;
                  }

                  int thisTotalLen = headerLen + thisFragmentDataLen;
                  int mfFlag = ((offsetBytes + thisFragmentDataLen) < totalPayload) ? 1 : 0;
                  int fragOffset = offsetBytes / fragmentUnit;
                  int flagsValue = (mfFlag << 1); 

                  byte[] fragmentData = Arrays.copyOfRange(
                        upperLayerPDU,
                        offsetBytes,
                        offsetBytes + thisFragmentDataLen
                  );
                  
                  IPv4Packet fragmentPacket = new IPv4Packet(
                        this.source,
                        this.destination,
                        this.version,
                        this.IHL,
                        this.typeOfService,
                        thisTotalLen,
                        packetID,
                        flagsValue,
                        fragOffset,
                        ttl,
                        protocol,
                        fragmentData
                  );

                  byte[] fragmentBytes = fragmentPacket.toByte();
                  out.write(fragmentBytes, 0, fragmentBytes.length);

                  offsetBytes += thisFragmentDataLen;
            }

                  return this.nextProtocol.encapsulate(out.toByteArray());
            }

      public byte[] decapsulate(byte[] lowerLayerPDU) {
            // 1. Error checks
            if (lowerLayerPDU == null || lowerLayerPDU.length == 0) {
                  throw new IllegalArgumentException("IP: lowerLayerPDU cannot be null or empty");
            }
            if(this.previousProtocol == null) 
                  throw new RuntimeException("IP: previous protocol is null");
            

            final int MIN_IPV4_HEADER = 20;
            ByteArrayInputStream in = new ByteArrayInputStream(lowerLayerPDU);

            java.util.List<Fragment> fragments = new java.util.ArrayList<>();

            // 2. Parse each IPv4 packet fragment
            while (in.available() >= MIN_IPV4_HEADER) {
                  // Peek first 4 bytes to get IHL and Total Length
                  byte[] first4 = new byte[4];
                  in.mark(4);
                  int read = in.read(first4, 0, 4);
                  in.reset();
                  if (read < 4) {
                        throw new IllegalArgumentException("IP: incomplete header");
                  }
                  int versionIhl = first4[0] & 0xFF;
                  int ihl = versionIhl & 0x0F;
                  int headerLen = ihl * 4;
                  if (ihl < 5 || headerLen < MIN_IPV4_HEADER) {
                        throw new IllegalArgumentException("IP: invalid IHL: " + ihl);
                  }

                  // Read the full header to extract fields
                  byte[] header = new byte[headerLen];
                  int hread = in.read(header, 0, headerLen);
                  if (hread < headerLen) {
                        throw new IllegalArgumentException("IP: incomplete IPv4 header");
                  }
                  // Total Length is bytes 2–3
                  int totalLen = ((header[2] & 0xFF) << 8) | (header[3] & 0xFF);
                  if (totalLen < headerLen || totalLen > in.available() + headerLen) {
                        throw new IllegalArgumentException("IP: invalid Total Length: " + totalLen);
                  }

                  // Flags+FragmentOffset are bytes 6–7
                  int flagsAndOffset = ((header[6] & 0xFF) << 8) | (header[7] & 0xFF);
                  int fragOffsetUnits = flagsAndOffset & 0x1FFF; // lower 13 bits
                  int dataOffsetBytes = fragOffsetUnits * 8;

                  // Now read the payload chunk of length (totalLen - headerLen)
                  int dataLen = totalLen - headerLen;
                  byte[] chunk = new byte[dataLen];
                  int dread = in.read(chunk, 0, dataLen);
                  if (dread < dataLen) {
                        throw new IllegalArgumentException("IP: incomplete IPv4 payload");
                  }

                  fragments.add(new Fragment(dataOffsetBytes, chunk));
            }

            // 3. Reassemble payload by sorting fragments by offset and concatenating
            fragments.sort((a, b) -> Integer.compare(a.offset, b.offset));

            // Compute total reassembled length
            int reassembledLen = 0;
            for (Fragment f : fragments) {
                  int end = f.offset + f.data.length;
                  if (end > reassembledLen) {
                        reassembledLen = end;
                  }
            }

            byte[] reassembled = new byte[reassembledLen];
            for (Fragment f : fragments) {
                  System.arraycopy(f.data, 0, reassembled, f.offset, f.data.length);
            }

            // 4. Pass the reassembled byte[] to previousProtocol.decapsulate
            return this.previousProtocol.decapsulate(reassembled);
      }

      /**
       * @param nextProtocol is the next protocol in the chain
       * @throws IllegalArgumentException if nextProtocol is null
       */
      public void setNext(Protocol nextProtocol) throws IllegalArgumentException {
            if(nextProtocol == null)
                  throw new IllegalArgumentException("IP: next protocol cannot be null");

            this.nextProtocol = nextProtocol;
      }

      /**
       * @param previousProtocol is the previous protocol in the chain
       * @throws IllegalArgumentException if previous protocol is null
       */
      public void setPrevious(Protocol previousProtocol) throws IllegalArgumentException {
            if(previousProtocol == null)
                  throw new IllegalArgumentException("IP: previous protocol cannot be null");
            
            this.previousProtocol = previousProtocol;
      }
}
