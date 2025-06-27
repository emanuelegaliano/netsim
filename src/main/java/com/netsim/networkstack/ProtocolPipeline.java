package com.netsim.networkstack;

import java.util.ArrayList;
import java.util.List;

public class ProtocolPipeline {
      private final IdentityProtocol head;
      private final IdentityProtocol tail;
      private final List<Protocol> stack;

      /**
       * Creates an empty pipeline (no real protocols).
       */
      public ProtocolPipeline() {
            this.head = new IdentityProtocol();
            this.tail = new IdentityProtocol();
            this.stack = new ArrayList<>();
            // wire head <-> tail
            this.head.setNext(this.tail);
            this.tail.setPrevious(this.head);
      }

      /**
       * Pushes a protocol onto the top of the stack.
       * @param protocol non-null Protocol to add
       * @throws IllegalArgumentException if proto is null
       */
      public void push(Protocol protocol) {
            if(protocol == null)
                  throw new IllegalArgumentException("ProtocolPipeline: protocol cannot be null");
            this.stack.add(0, protocol);
            this.rewireChain();
      }

      /**
       * Pops the most recently pushed protocol.
       * @return the popped Protocol
       * @throws RuntimeException if the stack is empty
       */
      public Protocol pop() {
            if (this.stack.isEmpty())
                  throw new RuntimeException("ProtocolPipeline: nothing to pop");
            Protocol removed = this.stack.remove(0);
            this.rewireChain();
            return removed;
      }

      /**
       * Encapsulates data by invoking the top protocol in the chain.
       * @param data non-null, non-empty payload
       * @return fully encapsulated byte[] through the chain
       * @throws IllegalArgumentException if data is null or empty
       */
      public byte[] encapsulate(byte[] data) {
            if(data == null || data.length == 0)
                  throw new IllegalArgumentException("ProtocolPipeline: data cannot be null or empty");
            if(this.stack.isEmpty()) 
                  // no real protocols => return data unchanged
                  return data;
            
            // start chain at top‐of‐stack
            return this.stack.get(0).encapsulate(data);
      }

      /**
       * Decapsulates data by invoking the bottom protocol in the chain.
       * @param data non-null, non-empty payload
       * @return fully decapsulated byte[] through the chain
       * @throws IllegalArgumentException if data is null or empty
       */
      public byte[] decapsulate(byte[] data) {
            if(data == null || data.length == 0)
                  throw new IllegalArgumentException("ProtocolPipeline: data cannot be null or empty");
            if(this.stack.isEmpty()) 
                  // no real protocols => return data unchanged
                  return data;
            // start chain at bottom‐of‐stack
            int last = this.stack.size() - 1;
            return this.stack.get(last).decapsulate(data);
      }

      /**
       * Returns how many real protocols are on the stack.
       */
      public int size() {
            return this.stack.size();
      }

      // Rebuilds the next/previous links between head, all stacked protocols, and tail.
      private void rewireChain() {
            Protocol prev = this.head;
            // wire each real protocol in LIFO order
            for(Protocol p : this.stack) {
                  prev.setNext(p);
                  p.setPrevious(prev);
                  prev = p;
            }
            // finally wire to tail
            prev.setNext(this.tail);
            this.tail.setPrevious(prev);
      }
}