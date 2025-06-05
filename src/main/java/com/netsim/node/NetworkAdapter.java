package com.netsim.node;

import com.netsim.addresses.Mac;

public class NetworkAdapter {
      private final String name;
      private final int MTU;
      private final Mac macAddress;

      private boolean isUp;
      private boolean promiscuousMode;

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

      public void setPromiscuosModeOn() {
            this.promiscuousMode = true;
      }

      public void setPromiscuosModeOff() {
            this.promiscuousMode = false;
      }
}