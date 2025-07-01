package com.netsim.protocols.SimpleDLL;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import com.netsim.addresses.Mac;
import com.netsim.networkstack.Protocol;
import com.netsim.utils.Logger;

/**
 * A Data Link Layer protocol that fragments or reassembles raw IP packets
 * into Ethernet‐like frames using MAC addresses.
 */
public class SimpleDLLProtocol implements Protocol {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = SimpleDLLProtocol.class.getSimpleName();

    private final Mac source;
    private final Mac destination;

    /**
     * Constructs a new SimpleDLLProtocol.
     *
     * @param source      the source MAC address (non-null)
     * @param destination the destination MAC address (non-null)
     * @throws IllegalArgumentException if either MAC is null
     */
    public SimpleDLLProtocol(Mac source, Mac destination) {
        if (source == null) {
            logger.error("[" + CLS + "] source MAC cannot be null");
            throw new IllegalArgumentException("SimpleDLLProtocol: source MAC cannot be null");
        }
        if (destination == null) {
            logger.error("[" + CLS + "] destination MAC cannot be null");
            throw new IllegalArgumentException("SimpleDLLProtocol: destination MAC cannot be null");
        }
        this.source      = source;
        this.destination = destination;
        logger.info("[" + CLS + "] instantiated with src=" + this.source.stringRepresentation()
                    + " dst=" + this.destination.stringRepresentation());
    }

    /**
     * Encapsulates one or more concatenated IP packets into DLL frames.
     *
     * @param ipPackets the raw IP packet bytes (non-null, non-empty)
     * @return DLL‐framed bytes
     * @throws IllegalArgumentException if ipPackets is null/empty or malformed
     */
    @Override
    public byte[] encapsulate(byte[] ipPackets) {
        if (ipPackets == null || ipPackets.length == 0) {
            logger.error("[" + CLS + "] encapsulate: ipPackets cannot be null or empty");
            throw new IllegalArgumentException("SimpleDLLProtocol: ipPackets cannot be null or empty");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        while (offset < ipPackets.length) {
            if (offset + 4 > ipPackets.length) {
                logger.error("[" + CLS + "] encapsulate: truncated IP packet at offset " + offset);
                throw new IllegalArgumentException("SimpleDLLProtocol: truncated IP packet");
            }
            int ihl         = ipPackets[offset] & 0x0F;
            int headerBytes = ihl * 4;
            if (ihl < 5 || offset + headerBytes > ipPackets.length) {
                logger.error("[" + CLS + "] encapsulate: invalid IHL or incomplete header");
                throw new IllegalArgumentException("SimpleDLLProtocol: invalid IHL or incomplete header");
            }
            int totalLen = ((ipPackets[offset + 2] & 0xFF) << 8)
                         |  (ipPackets[offset + 3] & 0xFF);
            if (totalLen < headerBytes || offset + totalLen > ipPackets.length) {
                logger.error("[" + CLS + "] encapsulate: invalid total length=" + totalLen);
                throw new IllegalArgumentException("SimpleDLLProtocol: invalid total length");
            }
            byte[] ipPkt = Arrays.copyOfRange(ipPackets, offset, offset + totalLen);
            SimpleDLLFrame frame = new SimpleDLLFrame(this.source, this.destination, ipPkt);
            out.write(frame.toByte(), 0, frame.toByte().length);
            logger.debug("[" + CLS + "] encapsulate: framed IP packet length=" + totalLen);
            offset += totalLen;
        }
        byte[] result = out.toByteArray();
        logger.info("[" + CLS + "] encapsulate: produced " + result.length + " bytes");
        return result;
    }

    /**
     * Decapsulates DLL frames to extract concatenated IP packets.
     *
     * @param frames the raw DLL‐framed bytes (non-null, length ≥12)
     * @return concatenated IP packet bytes
     * @throws IllegalArgumentException if frames is null, too short, or malformed
     */
    @Override
    public byte[] decapsulate(byte[] frames) {
        if (frames == null || frames.length < 12) {
            logger.error("[" + CLS + "] decapsulate: frames too short");
            throw new IllegalArgumentException("SimpleDLLProtocol: frames too short");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        while (offset + 12 <= frames.length) {
            int ipOffset    = offset + 12;
            if (ipOffset + 4 > frames.length) {
                logger.error("[" + CLS + "] decapsulate: truncated IP header at offset " + ipOffset);
                throw new IllegalArgumentException("SimpleDLLProtocol: truncated IP header");
            }
            int ihl         = frames[ipOffset] & 0x0F;
            int headerBytes = ihl * 4;
            if (ihl < 5 || ipOffset + headerBytes > frames.length) {
                logger.error("[" + CLS + "] decapsulate: invalid IP header");
                throw new IllegalArgumentException("SimpleDLLProtocol: invalid IP header");
            }
            int totalLen = ((frames[ipOffset + 2] & 0xFF) << 8)
                         |  (frames[ipOffset + 3] & 0xFF);
            if (totalLen < headerBytes || ipOffset + totalLen > frames.length) {
                logger.error("[" + CLS + "] decapsulate: invalid total length=" + totalLen);
                throw new IllegalArgumentException("SimpleDLLProtocol: invalid total length");
            }
            out.write(frames, ipOffset, totalLen);
            logger.debug("[" + CLS + "] decapsulate: extracted IP packet length=" + totalLen);
            offset += 12 + totalLen;
        }
        byte[] result = out.toByteArray();
        logger.info("[" + CLS + "] decapsulate: reassembled " + result.length + " bytes");
        return result;
    }

    @Override
    public Mac getSource() {
        return this.source;
    }

    @Override
    public Mac getDestination() {
        return this.destination;
    }

    /**
     * Extracts the source MAC from a single DLL frame.
     *
     * @param frame the raw frame bytes (non-null, length ≥12)
     * @return the source MAC address
     * @throws IllegalArgumentException if frame is null or too short
     */
    @Override
    public Mac extractSource(byte[] frame) {
        if (frame == null || frame.length < 12) {
            logger.error("[" + CLS + "] extractSource: frame too short");
            throw new IllegalArgumentException("SimpleDLLProtocol: frame too short");
        }
        byte[] srcBytes = Arrays.copyOfRange(frame, 6, 12);
        Mac mac = Mac.bytesToMac(srcBytes);
        logger.debug("[" + CLS + "] extractSource: " + mac.stringRepresentation());
        return mac;
    }

    /**
     * Extracts the destination MAC from a single DLL frame.
     *
     * @param frame the raw frame bytes (non-null, length ≥6)
     * @return the destination MAC address
     * @throws IllegalArgumentException if frame is null or too short
     */
    @Override
    public Mac extractDestination(byte[] frame) {
        if (frame == null || frame.length < 6) {
            logger.error("[" + CLS + "] extractDestination: frame too short");
            throw new IllegalArgumentException("SimpleDLLProtocol: frame too short");
        }
        byte[] dstBytes = Arrays.copyOfRange(frame, 0, 6);
        Mac mac = Mac.bytesToMac(dstBytes);
        logger.debug("[" + CLS + "] extractDestination: " + mac.stringRepresentation());
        return mac;
    }

    /**
     * Creates a copy of this protocol instance.
     *
     * @return a new SimpleDLLProtocol with the same MAC addresses
     */
    @Override
    public Protocol copy() {
        logger.debug("[" + CLS + "] copy()");
        return new SimpleDLLProtocol(this.source, this.destination);
    }
}