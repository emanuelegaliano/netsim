package com.netsim.protocols.MSG;

import com.netsim.networkstack.PDU;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A simple application‐level PDU whose serialization
 * is just “name: message”.
 */
public class MSGHeader extends PDU {
      private final String name;
      private final String message;

      /**
       * @param name    the header name (non-null)
       * @param message the payload message (non-null)
       * @throws IllegalArgumentException if either argument is null
       */
      public MSGHeader(String name, String message) {
            // we don’t need real Address here, so pass null
            super(null, null);
            if(name == null || message == null) 
                  throw new IllegalArgumentException("MSGHeader: name and message must be non-null");
            
                  this.name = name;
            this.message = message;
      }

      /**
       * Returns just the name (without the “: message”).
       */
      public byte[] getHeader() {
            return name.getBytes(StandardCharsets.UTF_8);
      }

      /**
       * Returns the full serialization “name: message”.
       */
      public byte[] toByte() {
            String line = name + ": " + message;
            return line.getBytes(StandardCharsets.UTF_8);
      }

      /** @return the header name */
      public String getNameString() {
            return name;
      }

      /** @return the payload message */
      public String getMessageString() {
            return message;
      }

      public boolean equals(Object o) {
            if (!(o instanceof MSGHeader)) return false;
            MSGHeader that = (MSGHeader) o;
            return name.equals(that.name) &&
                  message.equals(that.message);
      }

      public int hashCode() {
            return Objects.hash(name, message);
      }
}