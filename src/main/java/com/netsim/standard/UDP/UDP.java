package com.netsim.standard.UDP;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.netsim.addresses.Port;

import com.netsim.networkstack.Protocol;
import com.netsim.standard.HTTP.HTTPMethods;
import com.netsim.standard.HTTP.HTTPRequest;

public class UDP implements Protocol<HTTPRequest, List<UDPSegment>> {
      private final Port sourcePort;
      private final Port destinationPort;
      private final int MSS; // maximum segment size

      /**
       * 
       * @param MSS the maximum segment size (calculated from the MTU of the device)
       * @param source the source port of the message
       * @param destination the destination port of the message
       * @throws IllegalArgumentException when MSS <= 0 and when source or destination is null
       */
      public UDP(int MSS, Port source, Port destination) throws IllegalArgumentException {
            if(MSS <= 0)
                  throw new IllegalArgumentException("UDP: segment size must be positive");

            if(source == null || destination == null)
                  throw new IllegalArgumentException("UDP: source and destination port cannot be null");

            this.MSS = MSS;
            this.sourcePort = source;
            this.destinationPort = destination;
      }

      /**
       * @return the source port attribute
       */
      public Port getSourcePort() {
            return this.sourcePort;
      }

      /**
       * @return the destination port attribute
       */
      public Port getDestinationPort() {
            return this.destinationPort;
      }

      /**
       * @return the maximum segment size (MSS)
       */
      public int getMSS() {
            return this.MSS;
      }

      /**
       * @throws IllegalArgumentException when pdu is null
       */
      @Override
      public List<UDPSegment> encapsulate(HTTPRequest pdu) throws IllegalArgumentException {
            if (pdu == null) {
                  throw new IllegalArgumentException("UDP: encapsulation received null HTTPRequest");
            }
            byte[] requestBits = pdu.toByte();
            List<UDPSegment> segments = new ArrayList<>();
            int sequenceNumber = 0;

            // frammenta in base alla MSS
            for(int offset = 0; offset < requestBits.length; offset += this.MSS) {
                  int len = Math.min(this.MSS, requestBits.length - offset);
                  byte[] chunk = new byte[len];
                  System.arraycopy(requestBits, offset, chunk, 0, len);

                  UDPSegment seg = new UDPSegment(
                        this.sourcePort,
                        this.destinationPort,
                        sequenceNumber,
                        chunk
                  );

                  sequenceNumber++;
                  segments.add(seg);
            }

            return segments;
      }

      /**
       * @throws IllegalArgumentException when pdu is null or segments list is empty
       */
      @Override
      public HTTPRequest decapsulate(List<UDPSegment> segments) throws IllegalArgumentException {
            if(segments == null || segments.isEmpty()) {
                  throw new IllegalArgumentException("UDP: decapsulation received null or empty segment list");
            }

            // 1) Sort segments by sequence number
            List<UDPSegment> sorted = new ArrayList<>(segments);
            sorted.sort(Comparator.comparingInt(UDPSegment::getSequenceNumber));

            // 2) Stitch together payload bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for(UDPSegment seg : sorted) {
                  byte[] raw = seg.toByte();
                  byte[] header = seg.getHeader();  // protected access within same package
                  int hdrLen = header.length;
                  baos.write(raw, hdrLen, raw.length - hdrLen);
            }

            byte[] httpBytes = baos.toByteArray();
            String httpText = new String(httpBytes, StandardCharsets.US_ASCII);

            // 3) Split header and body
            String[] parts      = httpText.split("\r\n\r\n", 2);
            String headerText = parts[0];
            String bodyText   = (parts.length > 1 ? parts[1] : "");

            // 4) Parse the request‐line and Host header
            String[] lines = headerText.split("\r\n");
            String[] requestLine = lines[0].split(" ", 3);
            if(requestLine.length < 3 || !"HTTP/1.0".equals(requestLine[2])) {
                  throw new IllegalArgumentException("UDP: malformed HTTP request‐line: " + lines[0]);
            }
            HTTPMethods method = HTTPMethods.valueOf(requestLine[0]);
            String path = requestLine[1];

            String host = "";
            boolean parsing = true;
            for(int i = 1; i < lines.length && parsing; i++) {
                  String line = lines[i];
                  if(line.startsWith("Host: ")) {
                        host = line.substring(6);
                        parsing = false;
                  }
            }

            // 5) Reconstruct and return the HTTPRequest
            return new HTTPRequest(method, path, host, bodyText);
      }
}
