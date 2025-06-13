package com.netsim.network;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.netsim.addresses.Address;
import com.netsim.addresses.Mac;
import com.netsim.networkstack.Protocol;

public final class NetworkAdapter {
      private final String name;
      private final int MTU;
      private final Mac macAddress;
      private NetworkAdapter remote;
      private Node owner; // owner because
      private boolean isUp;

      private List<byte[]> outGoingFrames;
      private List<byte[]> inGoingFrames;


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

            this.outGoingFrames = new LinkedList<>();
            this.inGoingFrames = new LinkedList<>();
      }

      public void setOwner(Node newOwner) throws IllegalArgumentException{
            if(newOwner == null)
                  throw new IllegalArgumentException("NetworkAdapter: node owner cannot be null");
            
            this.owner = newOwner;
      }

      public Node getNode() throws NullPointerException {
            if(this.owner == null)
                  throw new NullPointerException("NetworkAdapter: node owner not setted");

            return this.owner;
      }

      public void setRemoteAdapter(NetworkAdapter newRemoteAdapter) throws IllegalArgumentException{
            if(newRemoteAdapter == null)
                  throw new IllegalArgumentException("NetworkAdapter: remote adapter cannot be null");

            this.remote = newRemoteAdapter;
      }

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

      
      public void collectFrames(Protocol dllProtocol, byte[] frames) 
      throws IllegalArgumentException, RuntimeException {
            if (dllProtocol == null || frames == null || frames.length == 0) {
                  throw new IllegalArgumentException("NetworkAdapter: invalid arguments");
            }
            if (!this.isUp) {
                  throw new RuntimeException("NetworkAdapter: adapter is down");
            }

            int offset = 0;
            while (offset < frames.length) {
                  // determine length of this single DLL frame
                  int remaining = frames.length - offset;
                  int frameLen = Math.min(this.MTU, remaining);

                  byte[] single = Arrays.copyOfRange(frames, offset, offset + frameLen);

                  // extract destination address via the Protocol interface
                  Address maybeDest = dllProtocol.extractDestination(single);
                  if (!(maybeDest instanceof Mac)) {
                        throw new RuntimeException("NetworkAdapter: extractDestination did not return a Mac");
                  }
                  Mac destMac = (Mac) maybeDest;

                  // if it’s for me, or it’s the broadcast address, collect it
                  if (destMac.equals(this.macAddress) || destMac.equals(Mac.broadcast())) {
                        this.outGoingFrames.add(single);
                  }

                  offset += frameLen;
            }
            }


      public byte[] releaseFrames(Protocol dllProtocol) throws RuntimeException {
            if(dllProtocol == null)
                  throw new IllegalArgumentException("NetworkAdapter: dllProtocol cannot be null");
            if(this.inGoingFrames.isEmpty())
                  throw new RuntimeException("NetworkAdapter: adapter in buffer is empty");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for(byte[] frame : inGoingFrames)
                  out.write(frame, 0, frame.length);
            
            this.inGoingFrames.clear();
            return dllProtocol.decapsulate(out.toByteArray());
      }

      /**
       * receive a frame and add it to internal buffer (inGoingFrames) if it's valid
       * @param frame the frame
       * @throws IllegalArgumentException if either frame is null or is empty
       * @throws RuntimeException if adapter is down
       */
      public void receiveFrame(byte[] frame) {
            if(frame == null || frame.length < 12) 
                  throw new IllegalArgumentException("…frame too short…");

            // extract destination MAC = bytes 0–5
            Mac dst = Mac.bytesToMac(Arrays.copyOfRange(frame, 0, 6));
            if(!dst.equals(this.macAddress)) {
                  throw new IllegalArgumentException(
                  "NetworkAdapter: frame not addressed to me ("+ this.macAddress +")"
                  );
            }

            this.inGoingFrames.add(frame);
      }

      /**
       * send a frame from internal buffer (outGoingFrames) to other adapter
       * @param frame the frame
       * @throws IllegalArgumentException if other adapter is null
       * @throws RuntimeException if adapter is down
       *                          if internal buffer(outGoingFrames) is null
       */
      public void sendFrames() throws IllegalArgumentException, RuntimeException {
            if(this.remote == null)
                  throw new IllegalArgumentException("NetworkAdapter: other adapter cannot be null");
            if(!this.isUp)
                  throw new RuntimeException("NetworkAdapter: adapter is down");
            if(this.outGoingFrames.isEmpty())
                  throw new RuntimeException("NetworkAdapter: adapter out buffer is empty");

            for(byte[] frame : this.outGoingFrames)
                  this.remote.receiveFrame(frame);
      }

      @Override
      public boolean equals(Object obj) {
            if(obj == null)
                  return false;
            if(!(obj instanceof NetworkAdapter))
                  return false;
            
            NetworkAdapter other = (NetworkAdapter)obj;
            return other.getMacAddress() == this.macAddress;
      }
}