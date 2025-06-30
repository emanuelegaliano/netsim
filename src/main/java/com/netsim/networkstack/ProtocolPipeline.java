package com.netsim.networkstack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProtocolPipeline {
      private final List<Protocol> stack;

      /**
       * Creates an empty pipeline.
       */
      public ProtocolPipeline() {
            this.stack = new ArrayList<>();
      }

      /**
       * Pushes a protocol onto the top of the stack.
       * @param protocol non-null Protocol to add
       * @throws IllegalArgumentException if protocol is null
       */
      public void push(Protocol protocol) {
            if(protocol == null)
                  throw new IllegalArgumentException("ProtocolPipeline: protocol cannot be null");
            this.stack.add(0, protocol); // LIFO
      }

      /**
       * Pops the most recently pushed protocol.
       * @return the popped Protocol
       * @throws RuntimeException if the stack is empty
       */
      public Protocol pop() {
            if(this.stack.isEmpty())
                  throw new RuntimeException("ProtocolPipeline: nothing to pop");
            return this.stack.remove(0);
      }

      /**
       * Encapsulates data from top to bottom of the stack.
       * @param data non-null, non-empty payload
       * @return fully encapsulated data
       */
      public byte[] encapsulate(byte[] data) {
            if(data == null || data.length == 0)
                  throw new IllegalArgumentException("ProtocolPipeline: data cannot be null or empty");

            byte[] result = data;
            for(Protocol proto : this.stack) {
                  result = proto.encapsulate(result);
            }
            return result;
      }

      /**
       * Decapsulates data from bottom to top of the stack.
       * @param data non-null, non-empty payload
       * @return fully decapsulated data
       */
      public byte[] decapsulate(byte[] data) {
            if(data == null || data.length == 0)
                  throw new IllegalArgumentException("ProtocolPipeline: data cannot be null or empty");

            byte[] result = data;
            List<Protocol> reversed = new ArrayList<>(this.stack);
            Collections.reverse(reversed);
            for(Protocol proto : reversed) {
                  result = proto.decapsulate(result);
            }
            return result;
      }

      /**
       * Returns how many protocols are currently in the stack.
       */
      public int size() {
            return this.stack.size();
      }
}