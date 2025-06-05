package com.netsim.standard.SimpleDLL;

import com.netsim.addresses.Mac;
import com.netsim.networkstack.Protocol;


public class SimpleDLLProtocol implements Protocol {
      private final Mac source;
      private final Mac destination;

      private Protocol nextProtocol;
      private Protocol previousProtocol;

      public SimpleDLLProtocol(Mac source, Mac destination) throws IllegalArgumentException {
            if(source == null || destination == null)
                  throw new IllegalArgumentException("SimpleDLLProtocol: source or destination cannot be null");

            this.source = source;
            this.destination = destination;

            this.nextProtocol = null;
            this.previousProtocol = null;
      }

      /**
       * encapsulate message in a raw byte array adding SimpleDLLFrame header
       * @throws IllegalArgumentException if upperLayerPDU is null or its length is 0
       * @throws NullPointerException if nextProtocol is null
       */
      public byte[] encapsulate(byte[] upperLayerPDU) throws IllegalArgumentException, NullPointerException {
            if(upperLayerPDU == null || upperLayerPDU.length == 0) 
                  throw new IllegalArgumentException("SimpleDLLProtocol: payload cannot be null or empty");
             if(nextProtocol == null)
                  throw new NullPointerException("SimpleDLLProtocol: nextProtocol is null");

            SimpleDLLFrame frame = new SimpleDLLFrame(source, destination, upperLayerPDU);
            return this.nextProtocol.encapsulate(frame.toByte());
      }

      /**
       * decapsulate message in a raw byte array removing SimpleDLLFrame header
       * @throws IllegalArgumentException if lowerLayerPDU is null or has length < 12
       * @throws NullPointerException if previousProtocol is null
       */
      public byte[] decapsulate(byte[] lowerLayerPDU) throws IllegalArgumentException, NullPointerException {
            if(lowerLayerPDU == null || lowerLayerPDU.length < 12)
                  throw new IllegalArgumentException("SimpleDLLProtocol: payload cannot be null and must have SimpleDLLFrame header");
            if(previousProtocol == null)
                  throw new NullPointerException("SimpleDLLProtocol: previous protocol is null");

            int payloadLen = lowerLayerPDU.length - 12;
            byte[] payload = new byte[payloadLen];
            System.arraycopy(lowerLayerPDU, 12, payload, 0, payloadLen);

            return this.previousProtocol.decapsulate(payload);
      }

      public void setNext(Protocol nextProtocol) throws IllegalArgumentException {
            if(nextProtocol == null)
                  throw new IllegalArgumentException("SimpleDLLProtocol: nextProtocol cannot be null");

            this.nextProtocol = nextProtocol;     
      }

      public void setPrevious(Protocol previousProtocol) throws IllegalArgumentException {
            if(previousProtocol == null)
                  throw new IllegalArgumentException("SimpleDLlProtocol: previousProtocol cannot be null");

            this.previousProtocol = previousProtocol;
      }
}
