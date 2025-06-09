package com.netsim.networkstack;

import java.util.List;

public class ProtocolPipeline {
      private final List<Protocol> protocols;

      /**
       * Assign internal final protocols list to protocols
       * @param protocols the protocols chain
       * @throws IllegalArgumentException if either protocols is null or is empty
       */
      public ProtocolPipeline(List<Protocol> protocols) throws IllegalArgumentException {
            if(protocols == null || protocols.size() == 0)
                  throw new IllegalArgumentException("ProtocolPipeline: protocols list cannot be null or empty");
            
            this.protocols = protocols;
      }

      /**
       * encapsulate data through the chain
       * @param data the data to encapsulate
       * @return the data encapsulated by all the protocols
       * @throws IllegalArgumentException 
       */
      public byte[] encapsulate(byte[] data) throws IllegalArgumentException {
            if(data == null || data.length == 0)
                  throw new IllegalArgumentException("ProtocolPipeline: data to encapsulate cannot be null or empty");
            
            // no need to check if protocols is empty, because constructor already checks it

            int size = this.protocols.size();
            if(size == 0)
                  throw new IllegalArgumentException("ProtocolPipeline: no protocols defined");

            // encapsulating through chain, using 1 because 0 is IdentityProtocol
            return this.protocols.get(1).encapsulate(data);
      }

      public byte[] decapsulate(byte[] data) throws IllegalArgumentException {
            if(data == null || data.length == 0)
                  throw new IllegalArgumentException("ProtocolPipeline: data to encapsulate cannot be null or empty");

            // no need to check if protocols is empty, because constructor already checks it

            int size = this.protocols.size();
            if(size == 0)
                  throw new IllegalArgumentException("ProtocolPipeline: no protocols defined");

            // decapsulating through chain, using size-1 because size protocol is IdentityProtocol
            return this.protocols.get(size-1).decapsulate(data);
      }

      public Protocol getProtocolAt(int index) throws IndexOutOfBoundsException {
            if(index == 0 || index > this.protocols.size())
                  throw new IndexOutOfBoundsException("ProtocolPipeline: index out of bound");

            return this.protocols.get(index);
      }
}

