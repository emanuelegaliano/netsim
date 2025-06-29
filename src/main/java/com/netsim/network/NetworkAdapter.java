package com.netsim.network;

import com.netsim.addresses.Address;
import com.netsim.addresses.Mac;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.SimpleDLL.SimpleDLLProtocol;
import com.netsim.utils.Logger;

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
      public void send(ProtocolPipeline stack, byte[] frame) throws IllegalArgumentException, RuntimeException {
            if(stack == null || frame == null || frame.length == 0)
                  throw new IllegalArgumentException("NetworkAdapter: invalid arguments");
            if(!this.isUp)
                  throw new RuntimeException("NetworkAdapter: adapter is down");

            SimpleDLLProtocol framingProtocol = new SimpleDLLProtocol(
                  this.macAddress, 
                  this.getLinkedAdapter().getMacAddress()
            );
            byte[] encapsulated = framingProtocol.encapsulate(frame);
            stack.push(framingProtocol);
            
            this.getLinkedAdapter().receive(stack, encapsulated);
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
      public void receive(ProtocolPipeline stack, byte[] frame) throws IllegalArgumentException, RuntimeException {
            if(stack == null || frame == null || frame.length == 0)
                  throw new IllegalArgumentException("NetworkAdapter: invalid arguments");
            if(!this.isUp) {
                  Logger.getInstance().debug("Adapter down");
                  return; // drop them
            }

            if(this.owner == null) 
                  throw new RuntimeException("NetworkAdapter: owner node is null");

            Protocol framingProtocol = stack.pop();
            Address destination = framingProtocol.extractDestination(frame);
            if(!(destination instanceof Mac))
                  throw new RuntimeException("NetworkAdapter: expected dll protocol");

            Mac destinationMac = (Mac) destination;
            // if frames are not for me or are not broadcast drop them
            if(!(destination.equals(this.macAddress) || destinationMac.equals(Mac.broadcast()))) {
                  Logger.getInstance().debug("Wrong destination Mac");
                  return; // drop them
            }

            byte[] packets = framingProtocol.decapsulate(frame);
            this.owner.receive(stack, packets);
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