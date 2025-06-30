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
      private static final Logger logger = Logger.getInstance();
      private static final String CLS = NetworkAdapter.class.getSimpleName();

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
            if (name == null) {
                  logger.error("[" + CLS + "] name cannot be null");
                  throw new IllegalArgumentException("NetworkAdapter: name cannot be null");
            }
            if (macAddress == null) {
                  logger.error("[" + CLS + "] macAddress cannot be null");
                  throw new IllegalArgumentException("NetworkAdapter: mac address cannot be null");
            }
            this.name       = name;
            this.MTU        = MTU;
            this.macAddress = macAddress;
            this.remote     = null;
            this.isUp       = true;
            logger.info("[" + CLS + "] created adapter \"" + name + "\" with MTU=" + MTU 
                        + " and MAC=" + macAddress.stringRepresentation());
      }

      /**
       * Links this adapter to a new node.
       *
       * @param newOwner the new node
       * @throws IllegalArgumentException if newOwner is null
       */
      public void setOwner(Node newOwner) {
            if (newOwner == null) {
                  logger.error("[" + CLS + "] cannot set null owner");
                  throw new IllegalArgumentException("NetworkAdapter: node owner cannot be null");
            }
            this.owner = newOwner;
            logger.info("[" + CLS + "] adapter \"" + name + "\" owner set to node \"" 
                        + newOwner.getName() + "\"");
      }

      /**
       * @return the owner node
       * @throws NullPointerException if owner is not set
       */
      public Node getNode() {
            if (this.owner == null) {
                  logger.error("[" + CLS + "] attempted getNode but owner not set");
                  throw new NullPointerException("NetworkAdapter: node owner not set");
            }
            return this.owner;
      }

      /**
       * Links this adapter to a remote adapter (fake cable).
       *
       * @param newRemoteAdapter the remote adapter
       * @throws IllegalArgumentException if newRemoteAdapter is null
       */
      public void setRemoteAdapter(NetworkAdapter newRemoteAdapter) {
            if (newRemoteAdapter == null) {
                  logger.error("[" + CLS + "] cannot set null remote adapter");
                  throw new IllegalArgumentException("NetworkAdapter: remote adapter cannot be null");
            }
            this.remote = newRemoteAdapter;
            logger.info("[" + CLS + "] adapter \"" + name + "\" linked to remote adapter \"" 
                        + newRemoteAdapter.getName() + "\"");
      }

      /**
       * @return the linked remote adapter
       * @throws NullPointerException if not connected
       */
      public NetworkAdapter getLinkedAdapter() {
            if (this.remote == null) {
                  logger.error("[" + CLS + "] no remote adapter connected");
                  throw new NullPointerException("NetworkAdapter: remote adapter not connected");
            }
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
            logger.info("[" + CLS + "] adapter \"" + name + "\" is UP");
      }

      /** Sets Adapter down for sending/receiving */
      public void setDown() {
            this.isUp = false;
            logger.info("[" + CLS + "] adapter \"" + name + "\" is DOWN");
      }

      /**
       * Sends a raw frame (e.g. DLL-encapsulated) to the linked adapter.
       *
       * @param stack protocol pipeline to use
       * @param frame raw bytes to send
       * @throws IllegalArgumentException if stack or frame is null or empty
       * @throws RuntimeException if adapter is down or not linked
       */
      public void send(ProtocolPipeline stack, byte[] frame) {
            if (stack == null || frame == null || frame.length == 0) {
                  logger.error("[" + CLS + "] invalid arguments to send");
                  throw new IllegalArgumentException("NetworkAdapter: invalid arguments");
            }
            if (!this.isUp) {
                  logger.error("[" + CLS + "] adapter \"" + name + "\" is down, cannot send");
                  throw new RuntimeException("NetworkAdapter: adapter is down");
            }

            SimpleDLLProtocol framingProtocol = new SimpleDLLProtocol(
                  this.macAddress,
                  this.getLinkedAdapter().getMacAddress()
            );
            byte[] encapsulated = framingProtocol.encapsulate(frame);
            stack.push(framingProtocol);
            logger.info("[" + CLS + "] adapter \"" + name + "\" sent frame (" 
                        + encapsulated.length + " bytes) to adapter \""
                        + getLinkedAdapter().getName() + "\"");
            this.getLinkedAdapter().receive(stack, encapsulated);
      }

      /**
       * Receives a raw frame from the linked adapter.
       *
       * @param stack protocol pipeline to use
       * @param frame raw bytes received
       * @throws IllegalArgumentException if stack or frame is null or empty
       */
      public void receive(ProtocolPipeline stack, byte[] frame) {
            if (stack == null || frame == null || frame.length == 0) {
                  logger.error("[" + CLS + "] invalid arguments to receive");
                  throw new IllegalArgumentException("NetworkAdapter: invalid arguments");
            }
            if (!this.isUp) {
                  logger.debug("[" + CLS + "] adapter \"" + name + "\" is down, dropping frame");
                  return;
            }
            if (this.owner == null) {
                  logger.error("[" + CLS + "] owner node is null, cannot deliver frame");
                  throw new RuntimeException("NetworkAdapter: owner node is null");
            }

            Protocol framingProtocol = stack.pop();
            Address destination = framingProtocol.extractDestination(frame);
            if (!(destination instanceof Mac)) {
                  logger.error("[" + CLS + "] expected SimpleDLLProtocol, got " 
                              + framingProtocol.getClass().getSimpleName());
                  throw new RuntimeException("NetworkAdapter: expected dll protocol");
            }

            Mac destinationMac = (Mac) destination;
            if (!(destination.equals(this.macAddress) || destinationMac.equals(Mac.broadcast()))) {
                  logger.debug("[" + CLS + "] frame not for this adapter (" 
                              + destinationMac.stringRepresentation() + "), dropping");
                  return;
            }

            byte[] packets = framingProtocol.decapsulate(frame);
            logger.info("[" + CLS + "] adapter \"" + name + "\" received frame, passing up to node");
            this.owner.receive(stack, packets);
      }

      @Override
      public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof NetworkAdapter)) {
                  logger.debug("[" + CLS + "] equals: object is not a NetworkAdapter");
                  return false;
            }
            NetworkAdapter other = (NetworkAdapter) obj;
            boolean eq = this.macAddress.equals(other.macAddress);
            logger.debug("[" + CLS + "] equals: comparing MACs, result=" + eq);
            return eq;
      }

      @Override
      public int hashCode() {
            return this.macAddress.hashCode();
      }
}