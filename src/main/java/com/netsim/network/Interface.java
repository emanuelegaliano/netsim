package com.netsim.network;

import com.netsim.addresses.IPv4;

public final class Interface {
      private final NetworkAdapter adapter;
      private final IPv4 ip;

      /**
       * @param adapter
       * @param ip
       * @throws IllegalArgumentException if either adapter or ip is null
       */
      public Interface(NetworkAdapter adapter, IPv4 ip) {
            if(adapter == null || ip == null)
                  throw new IllegalArgumentException("Interface: arguments cannot be null");

            this.adapter = adapter;
            this.ip = ip;
      }

      public NetworkAdapter getAdapter() {
            return this.adapter;
      }

      public IPv4 getIP() {
            return this.ip;
      }

      @Override
      public boolean equals(Object obj) {
            if(obj == null)
                  return false;
            if(!(obj instanceof Interface))
                  return false;

            Interface other = (Interface) obj;
            return other.getAdapter().equals(this.adapter) && other.getIP().equals(this.ip);
      }
}

