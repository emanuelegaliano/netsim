package com.netsim.table;

import com.netsim.addresses.IPv4;
import com.netsim.network.NetworkAdapter;
import com.netsim.utils.Logger;

/**
 * Encapsulates the next-hop IP and outgoing device for a route.
 */
public class RoutingInfo {
      private static final Logger logger = Logger.getInstance();
      private static final String CLS = RoutingInfo.class.getSimpleName();

      private NetworkAdapter device;
      private IPv4 nextHop;

      /**
       * @param device  the device to use to send packets (non-null)
       * @param nextHop the next-hop IP, or null if directly attached
       * @throws IllegalArgumentException if device is null
       */
      public RoutingInfo(NetworkAdapter device, IPv4 nextHop) {
            if (device == null) {
                  logger.error("[" + CLS + "] constructor: device cannot be null");
                  throw new IllegalArgumentException("RoutingInfo: device cannot be null");
            }
            this.device = device;
            this.nextHop = nextHop;
            logger.info("[" + CLS + "] created with device=" + device.getName()
                        + " nextHop=" + (nextHop == null ? "direct" : nextHop.stringRepresentation()));
      }

      /** @return the next-hop IP, or null for directly connected */
      public IPv4 getNextHop() {
            logger.debug("[" + CLS + "] getNextHop -> " 
                        + (nextHop == null ? "direct" : nextHop.stringRepresentation()));
            return this.nextHop;
      }

      /** @return the outgoing device */
      public NetworkAdapter getDevice() {
            logger.debug("[" + CLS + "] getDevice -> " + device.getName());
            return this.device;
      }

      /**
       * @param newNextHop the new next-hop IP (or null for direct)
       */
      public void setNextHop(IPv4 newNextHop) {
            this.nextHop = newNextHop;
            logger.info("[" + CLS + "] nextHop set to " 
                        + (newNextHop == null ? "direct" : newNextHop.stringRepresentation()));
      }

      /**
       * @param newDevice the new outgoing device (non-null)
       * @throws IllegalArgumentException if newDevice is null
       */
      public void setDevice(NetworkAdapter newDevice) {
            if (newDevice == null) {
                  logger.error("[" + CLS + "] setDevice: newDevice cannot be null");
                  throw new IllegalArgumentException("RoutingInfo: newDevice cannot be null");
            }
            this.device = newDevice;
            logger.info("[" + CLS + "] device set to " + newDevice.getName());
      }
}