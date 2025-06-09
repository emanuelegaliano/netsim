package com.netsim.protocols.MSG;

import com.netsim.networkstack.Protocol;
import com.netsim.addresses.Address;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A simple application-level protocol that serializes a payload as
 *   [name][": "][payload]
 * and decapsulates by stripping off the leading name+": " header.
 */
public class MSGProtocol implements Protocol {
      private final String name;
      private Protocol nextProtocol;
      private Protocol previousProtocol;

      /**
       * @param name the ASCII name to use as header (non-null)
       * @throws IllegalArgumentException if name is null
       */
      public MSGProtocol(String name) throws IllegalArgumentException {
            if(name == null) 
                  throw new IllegalArgumentException("MSGProtocol: name cannot be null");
            this.name = name;
      }

      public byte[] encapsulate(byte[] upperLayerPDU) {
            if(upperLayerPDU == null || upperLayerPDU.length == 0) 
                  throw new IllegalArgumentException("MSGProtocol: payload cannot be null or empty");
            
            // build name+": "+payload
            byte[] header = (name + ": ").getBytes(StandardCharsets.UTF_8);
            ByteArrayOutputStream out = new ByteArrayOutputStream(header.length + upperLayerPDU.length);
            try {
                  out.write(header);
                  out.write(upperLayerPDU);
            } catch (IOException e) {
                  // should never happen with ByteArrayOutputStream
                  throw new RuntimeException(e);
            }
            byte[] framed = out.toByteArray();

            if (nextProtocol != null) {
                  return nextProtocol.encapsulate(framed);
            }
            return framed;
      }

      public byte[] decapsulate(byte[] lowerLayerPDU) {
            if(lowerLayerPDU == null || lowerLayerPDU.length == 0) 
                  throw new IllegalArgumentException("MSGProtocol: input cannot be null or empty");
            if(previousProtocol == null) 
                  throw new NullPointerException("MSGProtocol: previous protocol is null");
            
            // find the ": " after the name
            String full = new String(lowerLayerPDU, StandardCharsets.UTF_8);
            String prefix = name + ": ";
            if (!full.startsWith(prefix)) {
                  // header mismatch
                  throw new IllegalArgumentException(
                  "MSGProtocol: input does not start with \"" + prefix + "\""
                  );
            }
            byte[] payload = full.substring(prefix.length())
                              .getBytes(StandardCharsets.UTF_8);
            return previousProtocol.decapsulate(payload);
      }

      public void setNext(Protocol nextProtocol) {
            if(nextProtocol == null) 
                  throw new IllegalArgumentException("MSGProtocol: nextProtocol cannot be null");
            this.nextProtocol = nextProtocol;
      }

      public void setPrevious(Protocol previousProtocol) {
            if(previousProtocol == null)
                  throw new IllegalArgumentException("MSGProtocol: previousProtocol cannot be null");
            this.previousProtocol = previousProtocol;
      }

      public Address getSource() {
            // application layer has no network-level source
            return null;
      }

      public Address getDestination() {
            // application layer has no network-level destination
            return null;
      }

      public Address extractSource(byte[] pdu) {
            // not applicable at this layer
            return null;
      }

      public Address extractDestination(byte[] pdu) {
            // not applicable at this layer
            return null;
      }

      @Override
      public boolean equals(Object o) {
            if (!(o instanceof MSGProtocol)) return false;
            return Objects.equals(name, ((MSGProtocol)o).name);
      }

      @Override
      public int hashCode() {
            return Objects.hash(name);
      }
}