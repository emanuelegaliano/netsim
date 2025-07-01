package com.netsim.protocols.SimpleDLL;

import com.netsim.networkstack.PDU;
import com.netsim.addresses.Mac;
import com.netsim.utils.Logger;

import java.nio.ByteBuffer;

/**
 * A simple Data Link Layer frame that prepends destination and source MAC addresses
 * to an encapsulated PDU payload.
 */
public final class SimpleDLLFrame extends PDU {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = SimpleDLLFrame.class.getSimpleName();

    private final byte[] payload;

    /**
     * Constructs a new SimpleDLLFrame.
     *
     * @param srcMac  the source MAC address (non-null)
     * @param dstMac  the destination MAC address (non-null)
     * @param payload the encapsulated PDU bytes (non-null, non-empty)
     * @throws IllegalArgumentException if any argument is null or empty
     */
    public SimpleDLLFrame(Mac srcMac, Mac dstMac, byte[] payload) throws IllegalArgumentException {
        super(srcMac, dstMac);
        if (srcMac == null) {
            logger.error("[" + CLS + "] srcMac cannot be null");
            throw new IllegalArgumentException("SimpleDLLFrame: srcMac cannot be null");
        }
        if (dstMac == null) {
            logger.error("[" + CLS + "] dstMac cannot be null");
            throw new IllegalArgumentException("SimpleDLLFrame: dstMac cannot be null");
        }
        if (payload == null || payload.length == 0) {
            logger.error("[" + CLS + "] payload cannot be null or empty");
            throw new IllegalArgumentException("SimpleDLLFrame: payload cannot be null or empty");
        }
        this.payload = payload.clone();
        logger.info("[" + CLS + "] constructed with payload length=" + this.payload.length);
    }

    /**
     * Builds the Data Link header consisting of:
     * [6 bytes destination MAC][6 bytes source MAC]
     *
     * @return a 12-byte header array
     */
    @Override
    public byte[] getHeader() {
        logger.debug("[" + CLS + "] getHeader()");
        byte[] dstBytes = this.destination.byteRepresentation();
        byte[] srcBytes = this.source.byteRepresentation();
        ByteBuffer buf = ByteBuffer.allocate(dstBytes.length + srcBytes.length);
        buf.put(dstBytes).put(srcBytes);
        byte[] header = buf.array();
        logger.debug("[" + CLS + "] header built, length=" + header.length);
        return header;
    }

    /**
     * Serializes the entire frame: header || payload.
     *
     * @return the full frame bytes
     */
    @Override
    public byte[] toByte() {
        logger.debug("[" + CLS + "] toByte()");
        byte[] header = this.getHeader();
        byte[] body   = this.payload;
        ByteBuffer buf = ByteBuffer.allocate(header.length + body.length);
        buf.put(header).put(body);
        byte[] frame = buf.array();
        logger.info("[" + CLS + "] serialized frame, total length=" + frame.length);
        return frame;
    }
}