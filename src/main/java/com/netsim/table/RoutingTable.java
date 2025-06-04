package com.netsim.table;

import com.netsim.node.NetworkAdapter;
import com.netsim.addresses.IPv4;

import java.util.HashMap;
import java.util.Map;

public class RoutingTable implements NetworkTable<IPv4, IPv4> {
      private HashMap<IPv4, HashMap<NetworkAdapter, IPv4>> table;

      public RoutingTable() {      
            this.table = new HashMap<>();
      }

      public IPv4 lookup(IPv4 key, NetworkAdapter device) throws IllegalArgumentException, NullPointerException {
            if(key == null)
                  throw new IllegalArgumentException("RoutingTable: key cannot be null");

            if(device == null)
                  throw new IllegalArgumentException("RoutingTable: device cannot be null");

            
            HashMap<NetworkAdapter, IPv4> inner = this.table.get(key);
            if(inner == null)
                  throw new NullPointerException("RoutingTable: route not found");

            IPv4 address = inner.get(device);
            if(address == null)
                  throw new NullPointerException(
                        "RoutingTable: device " 
                        + device.getName() 
                        + " has no route with ip: "
                        + key.stringRepresentation()
                  );

            return address;
      }

      public void add(IPv4 key, IPv4 value, NetworkAdapter device) throws IllegalArgumentException {
            if(key == null)
                  throw new IllegalArgumentException("RoutingTable: key cannot be null");

            if(value == null)
                  throw new IllegalArgumentException("RoutingTable: value cannot be null");

            if(device == null)
                  throw new IllegalArgumentException("RoutingTable: device cannot be null");

            this.table.computeIfAbsent(key, r -> new HashMap<>())
                      .put(device, value);
      }

      public void remove(IPv4 key, IPv4 value, NetworkAdapter device) throws IllegalArgumentException, NullPointerException {
            if(key == null)
                  throw new IllegalArgumentException("RoutingTable: key cannot be null");

            if(value == null)
                  throw new IllegalArgumentException("RoutingTable: value cannot be null");

            if(device == null)
                  throw new IllegalArgumentException("RoutingTable: device cannot be null");

            HashMap<NetworkAdapter, IPv4> inner = this.table.get(key);
            if(inner == null)
                  throw new NullPointerException(
                        "RoutingTable: unable to remove " 
                        + key.stringRepresentation()
                        + " because is not in the table");

            inner.remove(device);
            if(inner.isEmpty())
                  this.table.remove(key);
      }

      public int size() {
            int count = 0;
            for (Map<NetworkAdapter, IPv4> inner : table.values()) {
                  count += inner.size();
            }
            return count;
      }
      
      public void clear() {
            this.table.clear();
      }
}
