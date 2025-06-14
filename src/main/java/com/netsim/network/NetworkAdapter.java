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
       * Collects a fragmented byte array of frames, checking if
       * destination is remote adapter one.
       * @param dllProtocol the protocol used to encapsulate the frames
       * @param frames 
       * @throws IllegalArgumentException if any of the arguments is invalid
       * @throws RuntimeException if adapter is down,
       *                          if dll protocol did not extract Mac address, 
       *                          if adapter is empty after function call
       */
      public void collectFrames(Protocol dllProtocol, byte[] frames) 
      throws IllegalArgumentException, RuntimeException {
            if(dllProtocol == null || frames == null || frames.length == 0) 
                  throw new IllegalArgumentException("NetworkAdapter: invalid arguments");
            if(!this.isUp) 
                  throw new RuntimeException("NetworkAdapter: adapter is down");

            int offset = 0;
            while(offset < frames.length) {
                  int remain = frames.length - offset;
                  int frameLen = Math.min(this.MTU, remain);
                  byte[] single = Arrays.copyOfRange(frames, offset, offset+frameLen);
                  offset += frameLen;

                  Address maybeDest = dllProtocol.extractDestination(single);
                  if(!(maybeDest instanceof Mac))
                        throw new RuntimeException("NetworkAdapter: dllProtocol did not extract a Mac");

                  Mac destMac = (Mac) maybeDest;
                  Mac peerMac = this.remote.getMacAddress();
                  if(destMac.equals(peerMac) 
                  || destMac.equals(Mac.broadcast()))
                        this.outGoingFrames.add(single);
            }

            if(this.outGoingFrames.isEmpty())
                  throw new RuntimeException("NetworkAdapter: no frames collected");
      }

      /**
       * Returns what's inside the inGoingFrames buffer of
       * adapter.
       * @param dllProtocol the protocol to use in order to remove header from frames
       * @return byte array of inGoingFrames buffer without dllProtocol header
       * @throws IllegalArgumentException if dllProtocol is null
       * @throws RuntimeException if inGoingFrames buffer is empty
       */
      public byte[] releaseFrames(Protocol dllProtocol) throws IllegalArgumentException, RuntimeException {
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
      public void receiveFrame(Protocol framingProtocol, byte[] frame) {
            if(framingProtocol == null || frame == null) 
                  throw new IllegalArgumentException("NetworkAdapter: invalid arguments");
            if(!this.isUp)
                  throw new RuntimeException("NetworkAdapter: adapter is down");

            Address dstAddr = framingProtocol.extractDestination(frame);
            if(!(dstAddr instanceof Mac))
                  throw new RuntimeException("networkAdapter: framing protocol does not extract Mac");

            Mac dst = (Mac) dstAddr;
            if(dst.equals(this.macAddress) || dst.equals(Mac.broadcast())) 
                  this.inGoingFrames.add(frame);
      }

      /**
       * send a frame from internal buffer (outGoingFrames) to other adapter
       * @param frame the frame
       * @throws IllegalArgumentException if other adapter is null
       * @throws RuntimeException if adapter is down
       *                          if internal buffer(outGoingFrames) is null
       */
      public void sendFrames(Protocol framingProtocol) throws IllegalArgumentException, RuntimeException {
            if(this.remote == null)
                  throw new IllegalArgumentException("NetworkAdapter: other adapter cannot be null");
            if(!this.isUp)
                  throw new RuntimeException("NetworkAdapter: adapter is down");
            if(this.outGoingFrames.isEmpty())
                  throw new RuntimeException("NetworkAdapter: adapter out buffer is empty");

            for(byte[] frame : this.outGoingFrames)
                  this.remote.receiveFrame(framingProtocol, frame);

            this.outGoingFrames.clear();
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