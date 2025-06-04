package com.netsim.table;

import com.netsim.addresses.IP;
import com.netsim.node.NetworkAdapter;

public class RoutingInfo {
      private NetworkAdapter device;
      private IP nextHop;

      /**
       * @param device the device to use to find the route
       * @param nextHop the next node IP in the route, null if directly connected
       * @throws IllegalArgumentException if device is null
       */
      public RoutingInfo(NetworkAdapter device, IP nextHop) throws IllegalArgumentException {
            if(device == null || nextHop == null)
                  throw new IllegalArgumentException("RoutingInfo: device and nextHop cannot be null");

            this.device = device;
            this.nextHop = nextHop;
      }

      /** @return the next hop */
      public IP getNextHop() {
            return this.nextHop;
      }

      /** @return the device */
      public NetworkAdapter getDevice() {
            return this.device;
      }

      /**
       * @param newNextHop the new IP for nextHop, null if directly connected
      */
      public void setNextHop(IP newNextHop) {
            this.nextHop = newNextHop;
      }

      /**
       * @param newDevice the new device for the routing
       * @throws IllegalArgumentException if newDevice is null
       */
      public void setDevice(NetworkAdapter newDevice) throws IllegalArgumentException {
            if(newDevice == null)
                  throw new IllegalArgumentException("RoutingInfo: newNextHop cannot be null");

            this.device = newDevice;
      }
}