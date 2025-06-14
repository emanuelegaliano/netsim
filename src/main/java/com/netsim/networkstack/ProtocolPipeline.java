package com.netsim.networkstack;

import java.util.ArrayList;
import java.util.List;

import com.netsim.addresses.Address;

public class ProtocolPipeline {
      private final List<Protocol> protocols;

      /**
       * Assign internal final protocols list to protocols
       * @param protocols the protocols chain
       * @throws IllegalArgumentException if either protocols is null or is empty
       */
      public ProtocolPipeline(List<Protocol> protocols) throws IllegalArgumentException {
            if(protocols == null || protocols.isEmpty())
                  throw new IllegalArgumentException("ProtocolPipeline: protocols list cannot be null or empty");

            // Build an internal list with two extra IdentityProtocol endpoints
            this.protocols = new ArrayList<>(protocols.size() + 2);

            // 1) leading identity
            IdentityProtocol head = new IdentityProtocol();
            this.protocols.add(head);

            // 2) real protocols, wiring next/previous pointers
            Protocol prev = head;
            for (Protocol p : protocols) {
                  if (p == null) 
                        throw new IllegalArgumentException("ProtocolPipeline: protocols list must not contain null");
                  prev.setNext(p);
                  p.setPrevious(prev);
                  this.protocols.add(p);
                  prev = p;
            }

            // 3) trailing identity
            IdentityProtocol tail = new IdentityProtocol();
            prev.setNext(tail);
            tail.setPrevious(prev);
            this.protocols.add(tail);
      }

      /**
       * This method returns the size minus IdentityProtocol 
       * upper and lower bounds.
       * @return the dimension of the purely used protocols
       * @throws IllegalArgumentException
       */
      public int size() throws IllegalArgumentException {
            if(protocols.size() <= 2)
                  throw new IllegalArgumentException("ProtocolPipeline: no protocols found");

            return this.protocols.size()-2;
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

      public Protocol getProtocolAt(int index) {
            if(index < 0 || index >= this.size())
                  throw new IllegalArgumentException(
                        "getProtocolsTo: count must be between 1 and " + protocols.size()
                  );
            
            return this.protocols.get(index+1).copy();
      }

      /**
       * Return a new ProtocolPipeline containing
       * just the first {@code count} protocols from this one.
       *
       * @param count how many protocols to include (must be ≥1 and ≤ size)
       * @return a new ProtocolPipeline over protocols[0..count-1]
       * @throws IllegalArgumentException if count is out of range
       */
      public ProtocolPipeline getProtocolsRange(int count) {
            if(count <= 0 || count >= protocols.size()-2) {
                  throw new IllegalArgumentException(
                  "getProtocolsTo: count must be between 1 and " + protocols.size()
                  );
            }

            List<Protocol> sub = new ArrayList<>(
                  this.protocols.subList(1, 1 + count)
            );

            return new ProtocolPipeline(sub);
      }
      
      /**
       * @return list of protocol
       * @throws IllegalArgumentException if the list of protocols only contains IdentityProtocol
       */
      public List<Protocol> getProtocols() throws IllegalArgumentException{
            if(this.protocols.size() <= 2)
                  throw new IllegalArgumentException("ProtocolPipeline: no protocols found");

            return this.protocols;
      }

      /**
       * Using streams protocol class is searched in the list
       * @param clazz the class type of the protocol
       * @return a copy of the protocol used in the pipeline
       * @throws RuntimeException if protocol is not found
       */
      public <T extends Protocol> T getProtocolByClass(Class<T> clazz) throws RuntimeException {
            T originalP = protocols.stream()
                            .filter(clazz::isInstance)
                            .map(clazz::cast)
                            .findFirst()
                            .orElseThrow(() ->
                                    new RuntimeException(
                                          "ProtocolPipeline: Protocol not found " 
                                          + clazz.getSimpleName()
                                    )
                            );

            return clazz.cast(originalP.copy());
      }

      public <T extends Protocol> Address extractDestinationFrom(Class<T> clazz, byte[] data) 
      throws IllegalArgumentException {
            // stessa logica di sopra, ma chiami extractDestination(...)
            if(clazz == null || data == null) 
                  throw new IllegalArgumentException("extractDestinationFrom: arguments cannot be null");

            int idx = -1;
            for (int i = 0; i < this.protocols.size(); i++) {
                  if (clazz.isInstance(this.protocols.get(i))) {
                        idx = i;
                        break;
                  }
            }
            if(idx < 0) 
                  throw new IllegalArgumentException("ProtocolPipeline: no protocol of type " + clazz.getSimpleName());

            byte[] pdu = data;
            for(int i = this.protocols.size() - 1; i > idx; i--)
                  pdu = this.protocols.get(i).decapsulate(pdu);
            

            @SuppressWarnings("unchecked")
            T target = (T) this.protocols.get(idx);
            return target.extractDestination(pdu);
      }

      public <T extends Protocol> Address extractSourceFrom(Class<T> clazz, byte[] data) {
            // stessa logica di sopra, ma chiami extractDestination(...)
            if(clazz == null || data == null) 
                  throw new IllegalArgumentException("extractDestinationFrom: arguments cannot be null");

            int idx = -1;
            for (int i = 0; i < this.protocols.size(); i++) {
                  if (clazz.isInstance(this.protocols.get(i))) {
                        idx = i;
                        break;
                  }
            }
            if(idx < 0) 
                  throw new IllegalArgumentException("ProtocolPipeline: no protocol of type " + clazz.getSimpleName());

            byte[] pdu = data;
            for(int i = this.protocols.size() - 1; i > idx; i--)
                  pdu = this.protocols.get(i).decapsulate(pdu);
            

            @SuppressWarnings("unchecked")
            T target = (T) this.protocols.get(idx);
            return target.extractSource(pdu);
      }
}

