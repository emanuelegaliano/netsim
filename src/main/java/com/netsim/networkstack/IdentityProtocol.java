package com.netsim.networkstack;

import com.netsim.addresses.Address;

/**
 * A protocol used for returning message when chain is finished
 */
public class IdentityProtocol implements Protocol {
      Protocol nextProtocol;
      Protocol previousProtocol;

      public IdentityProtocol() {
            this.nextProtocol = null;
            this.previousProtocol = null;
      }

      public byte[] encapsulate(byte[] upperLayerPDU) {
            return upperLayerPDU;
      }

      public byte[] decapsulate(byte[] lowerLayerPDU) {
            return lowerLayerPDU;
      }

      public void setNext(Protocol nextProtocol) throws IllegalArgumentException {
            if(nextProtocol == null)
                  throw new IllegalArgumentException("IdentityProtocol: nextProtocol cannot be null");

            this.nextProtocol = nextProtocol;
      }
      public void setPrevious(Protocol previousProtocol) throws IllegalArgumentException {
            if(previousProtocol == null)
                  throw new IllegalArgumentException("IdentityProtocol: previousProtocol cannot be null");

            this.previousProtocol = previousProtocol;
      }

      /** @return null */
      public Address getSource() {
            return null;
      }

      /** @return null */
      public Address getDestination() {
            return null;
      }

      /** return null */
      public Address extractSource(byte[] pdu) {
            return null;
      }

      /** return null */
      public Address extractDestination(byte[] pdu) {
            return null;
      }

      public IdentityProtocol copy() {
            return new IdentityProtocol();
      }
}
