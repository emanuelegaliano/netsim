package com.netsim.networkstack;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.netsim.addresses.Mac;
public class NetworkAdapter {
      private final String name;
      private final int MTU;
      private final Mac macAddress;

      private boolean isUp;
      private boolean promiscuousMode;

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
            
            // settings
            this.isUp = true;
            this.promiscuousMode = false;

            this.outGoingFrames = new LinkedList<>();
            this.inGoingFrames = new LinkedList<>();
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

      /** @return if the Adapter has promiscuos mode active */
      public boolean promiscuousMode() {
            return this.promiscuousMode;
      }

      /** set Adapter up for sending/receiving */
      public void setUp() {
            this.isUp = true;
      }

      /** set Adapter down for sending/receiving */
      public void setDown() {
            this.isUp = false;
      }

      /** set Adapter able to receive frames with destination MACs
       *  different from its address
       */
      public void setPromiscuosModeOn() {
            this.promiscuousMode = true;
      }

      /** set Adapter unable to receive frames with destination MACs
       *  different from its address
       */
      public void setPromiscuosModeOff() {
            this.promiscuousMode = false;
      }           

      /**
       * takes the list of frames from DLL protocol, splits them
       * and add each of them in outGoingFrames waiting for NetworkAdapter send
       * @param frames 
       * @throws IllegalArgumentException if either frames is null or is empty, 
       *                                  if anything goes wrong while adding frames to outGoingFrames
       * @throws RuntimException if adapter is down   
       */
      public void collectFrames(byte[] frames) throws IllegalArgumentException, RuntimeException {
            if(frames == null || frames.length == 0) 
                  throw new IllegalArgumentException("NetworkAdapter: frames must not be null or empty");
            if(!this.isUp)
                  throw new RuntimeException("NetworkAdapter: adapter is down");
            
            int offset = 0;
            while (offset < frames.length) {
                  int remaining = frames.length - offset;
                  // need at least MAC header (12) + minimal IPv4 header (20)
                  if (remaining < 12 + 20) 
                  throw new IllegalArgumentException("NetworkAdapter: truncated frame at offset " + offset);

                  // inside the DLL frame, at offset+12 begins the IPv4 header:
                  int ipHeaderStart = offset + 12;
                  int versionIhl = frames[ipHeaderStart] & 0xFF;
                  int ihl = versionIhl & 0x0F;
                  if(ihl < 5) 
                        throw new IllegalArgumentException("NetworkAdapter: invalid IHL=" + ihl + " at offset " + offset);
                  
                  int ipHeaderLen = ihl * 4;
                  if(remaining < 12 + ipHeaderLen) 
                        throw new IllegalArgumentException("NetworkAdapter: incomplete IP header at offset " + offset);

                  // total length is bytes 2–3 of the IP header:
                  int totalLen = ((frames[ipHeaderStart + 2] & 0xFF) << 8)
                              |  (frames[ipHeaderStart + 3] & 0xFF);
                  if(totalLen < ipHeaderLen || remaining < 12 + totalLen) 
                        throw new IllegalArgumentException("NetworkAdapter: invalid IP totalLen=" + totalLen + " at offset " + offset);

                  // Verify that the frame’s source MAC (bytes 6–11) equals this adapter’s MAC
                  byte[] srcBytes = Arrays.copyOfRange(frames, offset + 6, offset + 12);
                  Mac frameSrc = Mac.bytesToMac(srcBytes);
                  if(!Arrays.equals(frameSrc.byteRepresentation(), this.macAddress.byteRepresentation())) {
                        throw new IllegalArgumentException(
                        "NetworkAdapter: frame source mismatch—expected " 
                        + this.macAddress.stringRepresentation() 
                        + " but got " 
                        + frameSrc.stringRepresentation());
                  }

                  // Slice out the full DLL frame and store it
                  int frameLen = 12 + totalLen;
                  byte[] singleFrame = Arrays.copyOfRange(frames, offset, offset + frameLen);
                  this.outGoingFrames.add(singleFrame);

                  offset += frameLen;
            }
      }

      /**
       * Drains all received frames, concatenating them into one byte[].
       * Clears the inGoingFrames buffer.
       * @return a byte[] containing all frames back‐to‐back (empty if none)
       * @throws RuntimeException if the internal buffer (inGoingFrames) is empty
       */
      public byte[] releaseFrames() throws RuntimeException {
            if(this.inGoingFrames.isEmpty())
                  throw new RuntimeException("NetworkAdapter: adapter in buffer is empty");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for(byte[] frame : inGoingFrames)
                  out.write(frame, 0, frame.length);
            
            inGoingFrames.clear();
            return out.toByteArray();
      }


      /**
       * receive a frame and add it to internal buffer (inGoingFrames) if it's valid
       * @param frame the frame
       * @throws IllegalArgumentException if either frame is null or is empty
       * @throws RuntimeException if adapter is down
       */
      public void receiveFrame(byte[] frame) {
      if (frame == null || frame.length < 12) 
            throw new IllegalArgumentException("…frame too short…");

      // extract destination MAC = bytes 0–5
      Mac dst = Mac.bytesToMac(Arrays.copyOfRange(frame, 0, 6));
      if(!dst.equals(this.macAddress) && ! this.promiscuousMode) {
            throw new IllegalArgumentException(
            "NetworkAdapter: frame not addressed to me ("+ this.macAddress +")"
            );
      }
      inGoingFrames.add(frame);
      }

      /**
       * send a frame from internal buffer (outGoingFrames) to other adapter
       * @param frame the frame
       * @throws IllegalArgumentException if other adapter is null
       * @throws RuntimeException if adapter is down
       *                          if internal buffer(outGoingFrames) is null
       */
      public void sendFrames(NetworkAdapter other) throws IllegalArgumentException, RuntimeException {
            if(other == null)
                  throw new IllegalArgumentException("NetworkAdapter: other adapter cannot be null");
            if(!this.isUp)
                  throw new RuntimeException("NetworkAdapter: adapter is down");

            if(this.outGoingFrames.isEmpty())
                  throw new RuntimeException("NetworkAdapter: adapter out buffer is empty");

            for(byte[] frame : this.outGoingFrames)
                  other.receiveFrame(frame);
      }
}