package com.netsim.table;

import com.netsim.addresses.IPv4;
import com.netsim.network.NetworkAdapter;

public class RoutingInfo {
      private NetworkAdapter device;
      private IPv4 nextHop;

      /**
       * @param device the device to use to find the route
       * @param nextHop the next node IP in the route, null if directly connected
       * @throws IllegalArgumentException if device is null
       */
      public RoutingInfo(NetworkAdapter device, IPv4 nextHop) throws IllegalArgumentException {
            if(device == null)
                  throw new IllegalArgumentException("RoutingInfo: device cannot be null");

            this.device = device;
            this.nextHop = nextHop;
      }

      /** @return the next hop */
      public IPv4 getNextHop() {
            return this.nextHop;
      }

      /** @return the device */
      public NetworkAdapter getDevice() {
            return this.device;
      }

      /**
       * @param newNextHop the new IP for nextHop, null if directly connected
      */
      public void setNextHop(IPv4 newNextHop) {
            this.nextHop = newNextHop;
      }

      /**
       * @param newDevice the new device for the routing
       * @throws IllegalArgumentException if newDevice is null
       */
      public void setDevice(NetworkAdapter newDevice) throws IllegalArgumentException {
            if(newDevice == null)
                  throw new IllegalArgumentException("RoutingInfo: newDevice cannot be null");

            this.device = newDevice;
      }
}