package com.netsim.protocols.MSG;

import com.netsim.networkstack.PDU;
import com.netsim.utils.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A simple application‐level PDU whose serialization
 * is just “name: message”.
 */
public class MSGHeader extends PDU {
      private static final Logger logger = Logger.getInstance();
      private static final String CLS = MSGHeader.class.getSimpleName();

      private final String name;
      private final String message;

      /**
       * @param name    the header name (non-null)
       * @param message the payload message (non-null)
       * @throws IllegalArgumentException if either argument is null
       */
      public MSGHeader(String name, String message) {
            super(null, null);
            logger.info("[" + CLS + "] constructing header for name=\"" + name + "\" message=\"" + message + "\"");
            if (name == null || message == null) {
                  logger.error("[" + CLS + "] name or message is null");
                  throw new IllegalArgumentException("MSGHeader: name and message must be non-null");
            }
            this.name = name;
            this.message = message;
      }

      /**
       * Returns just the name (without the “: message”).
       */
      @Override
      public byte[] getHeader() {
            logger.debug("[" + CLS + "] getHeader()");
            byte[] hdr = name.getBytes(StandardCharsets.UTF_8);
            logger.info("[" + CLS + "] header length=" + hdr.length);
            return hdr;
      }

      /**
       * Returns the full serialization “name: message”.
       */
      @Override
      public byte[] toByte() {
            logger.debug("[" + CLS + "] toByte()");
            String line = name + ": " + message;
            byte[] full = line.getBytes(StandardCharsets.UTF_8);
            logger.info("[" + CLS + "] full PDU length=" + full.length);
            return full;
      }

      /** @return the header name */
      public String getNameString() {
            return name;
      }

      /** @return the payload message */
      public String getMessageString() {
            return message;
      }

      @Override
      public boolean equals(Object o) {
            if (!(o instanceof MSGHeader)) return false;
            MSGHeader that = (MSGHeader) o;
            return name.equals(that.name) &&
                  message.equals(that.message);
      }

      @Override
      public int hashCode() {
            return Objects.hash(name, message);
      }
}