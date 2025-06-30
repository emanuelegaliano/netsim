package com.netsim.networkstack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.netsim.utils.Logger;

public class ProtocolPipeline {
    private static final Logger logger = Logger.getInstance();
      private final List<Protocol> stack;

      /**
       * Creates an empty pipeline.
       */
      public ProtocolPipeline() {
            this.stack = new ArrayList<>();
            logger.info("[" + getClass().getSimpleName() + "] initialized empty pipeline");
      }

      /**
       * Pushes a protocol onto the top of the stack.
       * @param protocol non-null Protocol to add
       * @throws IllegalArgumentException if protocol is null
       */
      public void push(Protocol protocol) {
            if (protocol == null) {
                  logger.error("[" + getClass().getSimpleName() + "] push failed: protocol is null");
                  throw new IllegalArgumentException("ProtocolPipeline: protocol cannot be null");
            }
            this.stack.add(0, protocol);
            logger.info("[" + getClass().getSimpleName() + "] pushed protocol: " + protocol.getClass().getSimpleName());
      }

      /**
       * Pops the most recently pushed protocol.
       * @return the popped Protocol
       * @throws RuntimeException if the stack is empty
       */
      public Protocol pop() {
            if (this.stack.isEmpty()) {
                  logger.error("[" + getClass().getSimpleName() + "] pop failed: stack is empty");
                  throw new RuntimeException("ProtocolPipeline: nothing to pop");
            }
            Protocol p = this.stack.remove(0);
            logger.info("[" + getClass().getSimpleName() + "] popped protocol: " + p.getClass().getSimpleName());
            return p;
      }

      /**
       * Encapsulates data from top to bottom of the stack.
       * @param data non-null, non-empty payload
       * @return fully encapsulated data
       */
      public byte[] encapsulate(byte[] data) {
            if (data == null || data.length == 0) {
                  logger.error("[" + getClass().getSimpleName() + "] encapsulate failed: data is null or empty");
                  throw new IllegalArgumentException("ProtocolPipeline: data cannot be null or empty");
            }
            logger.info("[" + getClass().getSimpleName() + "] starting encapsulation, initial length=" + data.length);
            byte[] result = data;
            for (Protocol proto : this.stack) {
                  result = proto.encapsulate(result);
                  logger.debug("[" + getClass().getSimpleName() + "] applied " +
                              proto.getClass().getSimpleName() + ", new length=" + result.length);
            }
            logger.info("[" + getClass().getSimpleName() + "] encapsulation complete, final length=" + result.length);
            return result;
      }

      /**
       * Decapsulates data from bottom to top of the stack.
       * @param data non-null, non-empty payload
       * @return fully decapsulated data
       */
      public byte[] decapsulate(byte[] data) {
            if (data == null || data.length == 0) {
                  logger.error("[" + getClass().getSimpleName() + "] decapsulate failed: data is null or empty");
                  throw new IllegalArgumentException("ProtocolPipeline: data cannot be null or empty");
            }
            logger.info("[" + getClass().getSimpleName() + "] starting decapsulation, initial length=" + data.length);
            byte[] result = data;
            List<Protocol> reversed = new ArrayList<>(this.stack);
            Collections.reverse(reversed);
            for (Protocol proto : reversed) {
                  result = proto.decapsulate(result);
                  logger.debug("[" + getClass().getSimpleName() + "] stripped " +
                              proto.getClass().getSimpleName() + ", new length=" + result.length);
            }
            logger.info("[" + getClass().getSimpleName() + "] decapsulation complete, final length=" + result.length);
            return result;
      }

      /**
       * Returns how many protocols are currently in the stack.
       */
      public int size() {
            int sz = this.stack.size();
            logger.debug("[" + getClass().getSimpleName() + "] size() = " + sz);
            return sz;
      }

      /**
       * Checks whether the stack is empty.
       * @return true if no protocols in the stack
       */
      public boolean isEmpty() {
            boolean empty = this.stack.isEmpty();
            logger.debug("[" + getClass().getSimpleName() + "] isEmpty() = " + empty);
            return empty;
      }

      /**
       * Peeks at the top protocol without removing it.
       * @return the top Protocol
       * @throws RuntimeException if the stack is empty
       */
      public Protocol peek() {
            if (this.stack.isEmpty()) {
                  logger.error("[" + getClass().getSimpleName() + "] peek failed: stack is empty");
                  throw new RuntimeException("ProtocolPipeline: stack is empty");
            }
            Protocol p = this.stack.get(0);
            logger.debug("[" + getClass().getSimpleName() + "] peek() = " + p.getClass().getSimpleName());
            return p;
      }
}
