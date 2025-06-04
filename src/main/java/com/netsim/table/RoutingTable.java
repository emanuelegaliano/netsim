package com.netsim.table;

import com.netsim.node.NetworkAdapter;
import com.netsim.addresses.IPv4;

import java.util.HashMap;
import java.util.Map;

public class RoutingTable implements NetworkTable<IPv4, IPv4> {
      private HashMap<IPv4, HashMap<NetworkAdapter, IPv4>> table;

      /**
       * creates the internal HashMap
       */
      public RoutingTable() {      
            this.table = new HashMap<>();
      }

      /**
       * @param destination the destination of route
       * @param device the Network Adapter the containts the route
       * @throws IllegalArgumentException if any of the arguments is null
       * @throws NullPointerException if destination is not found or device does contain route
       */
      public IPv4 lookup(IPv4 destination, NetworkAdapter device) throws IllegalArgumentException, NullPointerException {
            if(destination == null)
                  throw new IllegalArgumentException("RoutingTable: destination cannot be null");

            if(device == null)
                  throw new IllegalArgumentException("RoutingTable: device cannot be null");

            
            HashMap<NetworkAdapter, IPv4> inner = this.table.get(destination);
            if(inner == null)
                  throw new NullPointerException("RoutingTable: route not found");

            IPv4 address = inner.get(device);
            if(address == null)
                  throw new NullPointerException(
                        "RoutingTable: device " 
                        + device.getName() 
                        + " has no route with ip: "
                        + destination.stringRepresentation()
                  );

            return address;
      }

      /**
       * @param destination the destination of new route
       * @param nextHop the value ip used in tuple (device, nextHop)
       * @param device the network adapter used in tuple (device, nextHop) 
       * @throws IllegalArgumentException if any of the arguments is null
       */
      public void add(IPv4 destination, IPv4 nextHop, NetworkAdapter device) throws IllegalArgumentException {
            if(destination == null)
                  throw new IllegalArgumentException("RoutingTable: destination cannot be null");

            if(nextHop == null)
                  throw new IllegalArgumentException("RoutingTable: nextHop cannot be null");

            if(device == null)
                  throw new IllegalArgumentException("RoutingTable: device cannot be null");

            this.table.computeIfAbsent(destination, r -> new HashMap<>())
                      .put(device, nextHop);
      }

      /**
       * @param destination the destination of the route that will be removed
       * @param nextHop the value ip used in tuple (device, nextHop) from route that will be removed
       * @param device the network adapter used in tuple (device, nextHop) from route that will be removed
       * @throws IllegalArgumentException if any of the arguments is null
       * @throws NullPointerException if destination is not found or device does not contain route
       */
      public void remove(IPv4 destination, IPv4 nextHop, NetworkAdapter device) throws IllegalArgumentException, NullPointerException {
            if(destination == null)
                  throw new IllegalArgumentException("RoutingTable: destination cannot be null");

            if(nextHop == null)
                  throw new IllegalArgumentException("RoutingTable: nextHop cannot be null");

            if(device == null)
                  throw new IllegalArgumentException("RoutingTable: device cannot be null");

            HashMap<NetworkAdapter, IPv4> inner = this.table.get(destination);
            if(inner == null)
                  throw new NullPointerException(
                        "RoutingTable: unable to remove " 
                        + destination.stringRepresentation()
                        + " because is not in the table");

            inner.remove(device);
            if(inner.isEmpty())
                  this.table.remove(destination);
      }

      /** @return the size of the internal hashmap */
      public int size() {
            int count = 0;
            for (Map<NetworkAdapter, IPv4> inner : table.values()) {
                  count += inner.size();
            }
            return count;
      }

      /** clears internal hashmap */
      public void clear() {
            this.table.clear();
      }
}
