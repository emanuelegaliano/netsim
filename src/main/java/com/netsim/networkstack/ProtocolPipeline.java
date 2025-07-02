package com.netsim.networkstack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.netsim.utils.Logger;

/**
 * Manages a stack of Protocols for encapsulation and decapsulation.
 */
public class ProtocolPipeline {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = ProtocolPipeline.class.getSimpleName();

    private final List<Protocol> stack;

    /**
     * Creates an empty pipeline.
     */
    public ProtocolPipeline() {
        this.stack = new ArrayList<>();
        logger.info("[" + CLS + "] initialized empty pipeline");
    }

    /**
     * Pushes a Protocol onto the top of the stack.
     *
     * @param protocol the Protocol to add (non-null)
     * @throws IllegalArgumentException if protocol is null
     */
    public void push(Protocol protocol) throws IllegalArgumentException {
        if (protocol == null) {
            logger.error("[" + CLS + "] push failed: protocol is null");
            throw new IllegalArgumentException("ProtocolPipeline: protocol cannot be null");
        }
        this.stack.add(0, protocol);
        logger.info("[" + CLS + "] pushed protocol: " + protocol.getClass().getSimpleName());
    }

    /**
     * Pops the most recently pushed Protocol.
     *
     * @return the popped Protocol
     * @throws RuntimeException if the stack is empty
     */
    public Protocol pop() throws RuntimeException {
        if (this.stack.isEmpty()) {
            logger.error("[" + CLS + "] pop failed: stack is empty");
            throw new RuntimeException("ProtocolPipeline: nothing to pop");
        }
        Protocol p = this.stack.remove(0);
        logger.info("[" + CLS + "] popped protocol: " + p.getClass().getSimpleName());
        return p;
    }

    /**
     * Encapsulates data through all Protocols in stack order.
     *
     * @param data the payload bytes to encapsulate (non-null, non-empty)
     * @return fully encapsulated bytes
     * @throws IllegalArgumentException if data is null or empty
     */
    public byte[] encapsulate(byte[] data) throws IllegalArgumentException {
        if (data == null || data.length == 0) {
            logger.error("[" + CLS + "] encapsulate failed: data is null or empty");
            throw new IllegalArgumentException("ProtocolPipeline: data cannot be null or empty");
        }
        logger.info("[" + CLS + "] starting encapsulation, initial length=" + data.length);
        byte[] result = data;
        for (Protocol proto : this.stack) {
            result = proto.encapsulate(result);
            logger.debug("[" + CLS + "] applied " +
                         proto.getClass().getSimpleName() + ", new length=" + result.length);
        }
        logger.info("[" + CLS + "] encapsulation complete, final length=" + result.length);
        return result;
    }

    /**
     * Decapsulates data through all Protocols in reverse stack order.
     *
     * @param data the payload bytes to decapsulate (non-null, non-empty)
     * @return fully decapsulated bytes
     * @throws IllegalArgumentException if data is null or empty
     */
    public byte[] decapsulate(byte[] data) throws IllegalArgumentException {
        if (data == null || data.length == 0) {
            logger.error("[" + CLS + "] decapsulate failed: data is null or empty");
            throw new IllegalArgumentException("ProtocolPipeline: data cannot be null or empty");
        }
        logger.info("[" + CLS + "] starting decapsulation, initial length=" + data.length);
        byte[] result = data;
        List<Protocol> reversed = new ArrayList<>(this.stack);
        Collections.reverse(reversed);
        for (Protocol proto : reversed) {
            result = proto.decapsulate(result);
            logger.debug("[" + CLS + "] stripped " +
                         proto.getClass().getSimpleName() + ", new length=" + result.length);
        }
        logger.info("[" + CLS + "] decapsulation complete, final length=" + result.length);
        return result;
    }

    /**
     * Returns the number of Protocols in the stack.
     *
     * @return the stack size
     */
    public int size() {
        int sz = this.stack.size();
        logger.debug("[" + CLS + "] size() = " + sz);
        return sz;
    }

    /**
     * Checks whether the stack is empty.
     *
     * @return true if no Protocols are in the stack
     */
    public boolean isEmpty() {
        boolean empty = this.stack.isEmpty();
        logger.debug("[" + CLS + "] isEmpty() = " + empty);
        return empty;
    }

    /**
     * Peeks at the top Protocol without removing it.
     *
     * @return the top Protocol
     * @throws RuntimeException if the stack is empty
     */
    public Protocol peek() throws RuntimeException {
        if (this.stack.isEmpty()) {
            logger.error("[" + CLS + "] peek failed: stack is empty");
            throw new RuntimeException("ProtocolPipeline: stack is empty");
        }
        Protocol p = this.stack.get(0).copy();
        logger.debug("[" + CLS + "] peek() = " + p.getClass().getSimpleName());
        return p;
    }
}