package com.netsim.network;

import com.netsim.addresses.Mac;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.SimpleDLL.SimpleDLLProtocol;

/**
 * Represents a point-to-point network adapter.
 * <p>
 * Simplified: only send and receive stubs, no internal buffering.
 * </p>
 */
public final class NetworkAdapter {
      private final String name;
      private final int MTU;
      private final Mac macAddress;
      private NetworkAdapter remote;
      private Node owner;
      private boolean isUp;

      /**
       * @param name the name of the adapter
       * @param MTU the maximum transport unit of the adapter
       * @param macAddress mac address of the adapter
       * @throws IllegalArgumentException if either name or macAddress is null
       */
      public NetworkAdapter(String name, int MTU, Mac macAddress) {
            if(name == null)
                  throw new IllegalArgumentException("NetworkAdapter: name cannot be null");
            if(macAddress == null)
                  throw new IllegalArgumentException("NetworkAdapter: mac address cannot be null");

            this.name       = name;
            this.MTU        = MTU;
            this.macAddress = macAddress;
            this.remote     = null;
            this.isUp       = true;
      }

      /**
       * Links this adapter to a new node.
       *
       * @param newOwner the new node
       * @throws IllegalArgumentException if newOwner is null
       */
      public void setOwner(Node newOwner) {
            if(newOwner == null)
                  throw new IllegalArgumentException("NetworkAdapter: node owner cannot be null");
            this.owner = newOwner;
      }

      /**
       * @return the owner node
       * @throws NullPointerException if owner is not set
       */
      public Node getNode() {
            if(this.owner == null)
                  throw new NullPointerException("NetworkAdapter: node owner not set");
            return this.owner;
      }

      /**
       * Links this adapter to a remote adapter (fake cable).
       *
       * @param newRemoteAdapter the remote adapter
       * @throws IllegalArgumentException if newRemoteAdapter is null
       */
      public void setRemoteAdapter(NetworkAdapter newRemoteAdapter) {
            if(newRemoteAdapter == null)
                  throw new IllegalArgumentException("NetworkAdapter: remote adapter cannot be null");
            this.remote = newRemoteAdapter;
      }

      /**
       * @return the linked remote adapter
       * @throws NullPointerException if not connected
       */
      public NetworkAdapter getLinkedAdapter() {
            if(this.remote == null)
                  throw new NullPointerException("NetworkAdapter: remote adapter not connected");
            return this.remote;
      }

      /** @return the name of the Adapter */
      public String getName() {
            return this.name;
      }

      /** @return the Maximum Transport Unit of the Adapter */
      public int getMTU() {
            return this.MTU;
      }

      /** @return the MAC address of the Adapter */
      public Mac getMacAddress() {
            return this.macAddress;
      }

      /** @return whether the Adapter is up */
      public boolean isUp() {
            return this.isUp;
      }

      /** Sets Adapter up for sending/receiving */
      public void setUp() {
            this.isUp = true;
      }

      /** Sets Adapter down for sending/receiving */
      public void setDown() {
            this.isUp = false;
      }

      /**
       * Sends a raw frame (e.g. DLL-encapsulated) to the linked adapter.
       *
       * @param frame raw bytes to send
       * @throws IllegalArgumentException if frame is null or empty
       * @throws RuntimeException if adapter is down or not linked
       */
      public void send(ProtocolPipeline stack, byte[] frame) {
            if(frame == null || frame.length == 0)
                  throw new IllegalArgumentException("NetworkAdapter: frame cannot be null or empty");
            if(!this.isUp)
                  throw new RuntimeException("NetworkAdapter: adapter is down");
            
            stack.push(new SimpleDLLProtocol(
                  this.macAddress,
                  this.getLinkedAdapter().getMacAddress()
                  )
            );

            byte[] encapsulatedData = stack.encapsulate(frame);
            this.getLinkedAdapter().receive(stack, encapsulatedData);
      }

      /**
       * Receives a raw frame from the linked adapter.
       * <p>
       * Actual processing (decapsulation, handing to node) to be implemented.
       * </p>
       *
       * @param frame raw bytes received
       * @throws IllegalArgumentException if frame is null or empty
       */
      public void receive(ProtocolPipeline stack, byte[] frame) {
            if(frame == null || frame.length == 0)
                  throw new IllegalArgumentException("NetworkAdapter: frame cannot be null or empty");
            if(!this.isUp)
                  return; // drop silently
      }

      @Override
      public boolean equals(Object obj) {
            if(obj == null || !(obj instanceof NetworkAdapter))
                  return false;
            NetworkAdapter other = (NetworkAdapter)obj;
            return this.macAddress.equals(other.macAddress);
      }

      @Override
      public int hashCode() {
            return this.macAddress.hashCode();
      }
}