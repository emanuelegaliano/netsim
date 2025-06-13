package com.netsim.protocols.MSG;

import com.netsim.networkstack.Protocol;
import com.netsim.addresses.Address;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A simple application-level protocol that serializes a payload as
 *   [name][": "][payload]
 * and decapsulates by stripping off the leading name+": " header.
 * 
 * Defined in port 9696
 */
public class MSGProtocol implements Protocol {
    public final static int port = 9696;
    private final String name;
    private Protocol nextProtocol;
    private Protocol previousProtocol;

    /**
     * @param name the ASCII name to use as header (non-null)
     */
    public MSGProtocol(String name) {
        if (name == null) {
            throw new IllegalArgumentException("MSGProtocol: name cannot be null");
        }
        this.name = name;
    }

    /** encapsulates everything in a MSGHEader and pass it to the next protocol */
    public byte[] encapsulate(byte[] upperLayerPDU) {
        if(upperLayerPDU == null || upperLayerPDU.length == 0) 
            throw new IllegalArgumentException("MSGProtocol: payload cannot be null or empty");

        // Interpret the incoming bytes as UTF-8 text
        String message = new String(upperLayerPDU, StandardCharsets.UTF_8);

        // Build a PDU and serialize it
        MSGHeader header = new MSGHeader(name, message);
        byte[] frame = header.toByte();

        // Pass along the chain (or return if we're at the bottom)
        if(nextProtocol != null) {
            return nextProtocol.encapsulate(frame);
        } else {
            return frame;
        }
    }

    /** decapsulates previous protocol data removing MSG header */
    public byte[] decapsulate(byte[] lowerLayerPDU) {
        if(lowerLayerPDU == null || lowerLayerPDU.length == 0) 
            throw new IllegalArgumentException("MSGProtocol: input cannot be null or empty");
        if(previousProtocol == null) 
            throw new NullPointerException("MSGProtocol: previous protocol is null");

        // Convert to string to strip off the name+": " prefix
        String full = new String(lowerLayerPDU, StandardCharsets.UTF_8);
        String prefix = name + ": ";
        if(!full.startsWith(prefix)) 
            throw new IllegalArgumentException(
                "MSGProtocol: input does not start with \"" + prefix + "\""
            );
        
        String message = full.substring(prefix.length());

        // Back to bytes
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);

        // And pass it up the stack
        return previousProtocol.decapsulate(payload);
    }

    public void setNext(Protocol nextProtocol) {
        if(nextProtocol == null) {
            throw new IllegalArgumentException("MSGProtocol: nextProtocol cannot be null");
        }
        this.nextProtocol = nextProtocol;
    }

    public void setPrevious(Protocol previousProtocol) {
        if (previousProtocol == null) {
            throw new IllegalArgumentException("MSGProtocol: previousProtocol cannot be null");
        }
        this.previousProtocol = previousProtocol;
    }

    public Address getSource() {
        return null;  // not meaningful at this layer
    }

    public Address getDestination() {
        return null;  // not meaningful at this layer
    }

    public Address extractSource(byte[] pdu) {
        return null;  // not applicable
    }

    public Address extractDestination(byte[] pdu) {
        return null;  // not applicable
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof MSGProtocol)) return false;
        return Objects.equals(name, ((MSGProtocol)o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public MSGProtocol copy() {
        return new MSGProtocol(this.name);
    }
}