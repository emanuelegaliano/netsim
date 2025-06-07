package com.netsim.networkstack;

import com.netsim.addresses.Address;

/**
 * A protocol used for returning message when chain is finished
 */
public class IdentityProtocol implements Protocol {
      public byte[] encapsulate(byte[] upperLayerPDU) {
            return upperLayerPDU;
      }

      public byte[] decapsulate(byte[] lowerLayerPDU) {
            return lowerLayerPDU;
      }

      public void setNext(Protocol nextProtocol) {}
      public void setPrevious(Protocol previousProtocol) {}

      /** return null */
      public Address extractSource(byte[] pdu) {
            return null;
      }

      /** return null */
      public Address extractDestination(byte[] pdu) {
            return null;
      }
}
