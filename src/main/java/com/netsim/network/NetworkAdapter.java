package com.netsim.network;

import java.util.LinkedList;
import java.util.List;

import com.netsim.addresses.Mac;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.SimpleDLL.SimpleDLLProtocol;

public final class NetworkAdapter {
      private final String name;
      private final int MTU;
      private final Mac macAddress;
      private NetworkAdapter remote;
      private Node owner; // owner because
      private boolean isUp;

      private List<byte[]> bufferFrames;


      /**
       * @param name the name of the adapter
       * @param MTU the maximum transport unit of the adapter
       * @param macAddress mac address of the adapter
       * @throws IllegalArgumentException if either name or macAddress is null
       */
      public NetworkAdapter(String name, int MTU, Mac macAddress) throws IllegalArgumentException {
            if(name == null)
                  throw new IllegalArgumentException("NetworkAdapter: name cannot be null");

            if(macAddress == null)
                  throw new IllegalArgumentException("NetworkAdapter: mac address cannot be null");
            
            this.name = name;
            this.MTU = MTU;
            this.macAddress = macAddress;
            this.remote = null;
            
            // settings
            this.isUp = true;

            this.bufferFrames = new LinkedList<>();
      }

      /**
       * Links this adapter to a new node
       * @param newOwner the new node
       * @throws IllegalArgumentException if newOwner is null
       */
      public void setOwner(Node newOwner) throws IllegalArgumentException{
            if(newOwner == null)
                  throw new IllegalArgumentException("NetworkAdapter: node owner cannot be null");
            
            this.owner = newOwner;
      }

      /**
       * 
       * @return returns the owner
       * @throws NullPointerException if owner is null
       */
      public Node getNode() throws NullPointerException {
            if(this.owner == null)
                  throw new NullPointerException("NetworkAdapter: node owner not setted");

            return this.owner;
      }

      /**
       * Links this adapter to a remote node (fake cable connection)
       * @param newRemoteAdapter 
       * @throws IllegalArgumentException if newRemoteAdapter is null
       */
      public void setRemoteAdapter(NetworkAdapter newRemoteAdapter) throws IllegalArgumentException{
            if(newRemoteAdapter == null)
                  throw new IllegalArgumentException("NetworkAdapter: remote adapter cannot be null");

            this.remote = newRemoteAdapter;
      }

      /**
       * @return the (fake) cable connected "other" adapter
       * @throws NullPointerException if remote adapter is null
       */
      public NetworkAdapter getLinkedAdapter() throws NullPointerException {
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

      /** @return the mac address of the Adapter */
      public Mac getMacAddress() {
            return this.macAddress;
      }

      /** @return if the Adapter is up for sending/receiving */
      public boolean isUp() {
            return this.isUp;
            
      }

      /** 
       * Sets Adapter up for sending/receiving 
       * @throws RuntimException from Network.notifyAdapterState
      */
      public void setUp() throws RuntimeException {
            this.isUp = true;
      }

      /** 
       * Sets Adapter down for sending/receiving 
       * @throws RuntimException from Network.notifyAdapterState */
      public void setDown() throws RuntimeException{
            this.isUp = false;
      }

      /**
       * Called by a Node’s send(...) sequence to hand this adapter
       * raw payloads (e.g. IP datagrams) ready for framing.
       */
      public void collectFrames(byte[] payload) {
            if(payload == null || payload.length == 0)
                  throw new IllegalArgumentException("NetworkAdapter: payload cannot be null or empty");
            this.bufferFrames.add(payload);
      }

      /**
       * Encapsulate each payload in a DLL frame (using the given
       * SimpleDLLProtocol), then fire it across the “wire” to the
       * linked adapter’s receive(...).
       *
       * @param dllProto a SimpleDLLProtocol configured with src/dst MAC
       * @param pipeline the full protocol pipeline (for higher‐layer chaining)
       */
      public void sendFrames(SimpleDLLProtocol dllProto, ProtocolPipeline pipeline) {
            if(dllProto == null || pipeline == null)
                  throw new IllegalArgumentException("NetworkAdapter: invalid arguments to sendFrames");
            if(!this.isUp())
                  throw new RuntimeException("NetworkAdapter: adapter is down");

            // for each buffered payload, frame + deliver
            for(byte[] payload : this.bufferFrames) {
                  // build raw frame
                  byte[] frame = dllProto.encapsulate(payload);
                  // deliver to remote side
                  this.getLinkedAdapter().receive(pipeline, frame);
            }
            this.bufferFrames.clear();
      }

      /**
       * Called by the “wire” when a raw DLL frame arrives.
       * Pops the DLL protocol off the pipeline, checks destination MAC,
       * decapsulates, buffers payload, and notifies the owning Node.
       *
       * @param pipeline full protocol pipeline, with DLL at top
       * @param frame raw frame bytes
       */
      public void receive(ProtocolPipeline pipeline, byte[] frame) {
            if(pipeline == null || frame == null || frame.length == 0)
                  throw new IllegalArgumentException("NetworkAdapter: invalid arguments to receive");
            if(!this.isUp())
                  return;  // drop silently if down

            // 1) pop off the DLL protocol
            Protocol top = pipeline.pop();
            if(!(top instanceof SimpleDLLProtocol))
                  throw new RuntimeException("NetworkAdapter: expected DLL protocol");
            SimpleDLLProtocol dll = (SimpleDLLProtocol) top;

            // 2) extract destination MAC
            Mac dest = dll.extractDestination(frame);
            if(!this.macAddress.equals(dest)) {
                  // not for me, ignore
                  return;
            }

            // 3) decapsulate one level
            byte[] payload = dll.decapsulate(frame);

            // 4) buffer stripped payload for higher layers
            this.bufferFrames.add(payload);

            // 5) notify owning node
            this.getNode().receive(pipeline, payload);
      }

      @Override
      public boolean equals(Object obj) {
            if(obj == null)
                  return false;
            if(!(obj instanceof NetworkAdapter))
                  return false;
            
            NetworkAdapter other = (NetworkAdapter)obj;
            return other.getMacAddress().equals(this.macAddress);
      }

      @Override
      public int hashCode() {
            return this.macAddress.hashCode();
      }
}