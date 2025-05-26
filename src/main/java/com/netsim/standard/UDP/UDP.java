package com.netsim.standard.UDP;

import java.util.List;

import com.netsim.addresses.Port;

import com.netsim.networkstack.Protocol;
import com.netsim.standard.HTTP.HTTPRequest;

public class UDP implements Protocol<HTTPRequest, List<UDPSegment>> {
      private final Port sourcePort;
      private final Port destinationPort;
      private final int segmentSize;

      public UDP(int segmentSize, Port source, Port destination) throws IllegalArgumentException {
            if(segmentSize <= 0)
                  throw new IllegalArgumentException("UDP: segment size must be positive");

            if(source == null || destination == null)
                  throw new IllegalArgumentException("UDP: source and destination port cannot be null");

            this.segmentSize = segmentSize;
            this.sourcePort = source;
            this.destinationPort = destination;
      }

      public Port getSourcePort() {
            return this.sourcePort;
      }

      public Port getDestinationPort() {
            return this.destinationPort;
      }

      public int getSegmentSize() {
            return this.segmentSize;
      }

      public List<UDPSegment> encapsulate(HTTPRequest pdu) {
            
      }

      public HTTPRequest decapsulate(List<UDPSegment> pdu) {

      }
}
