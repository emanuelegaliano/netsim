package com.netsim.table;

import java.util.HashMap;

import com.netsim.addresses.IPv4;

public class RoutingTable implements NetworkTable<IPv4, RoutingInfo> {
      private HashMap<IPv4, RoutingInfo> table;

      public RoutingTable() {
            this.table = new HashMap<>();
      }

      /**
       * @param destination the destination of route
       * @throws IllegalArgumentException if destination is null
       * @throws NullPointerException if destination is not found 
       */
      public RoutingInfo lookup(IPv4 destination) throws IllegalArgumentException, NullPointerException {
            if(destination == null)
                  throw new IllegalArgumentException("RoutingTable: destination cannot be null");

            RoutingInfo ri = this.table.get(destination);
            if(ri == null)
                  throw new NullPointerException("RoutingTable: route not found");
                  
            return ri;
      }

      /**
       * @param destination the destination of new route
       * @param route a class for info routing on pair (device, nextHop)
       * @throws IllegalArgumentException if either desination or route is null
       * @throws RuntimeException if the route is already contained
       */
      public void add(IPv4 destination, RoutingInfo route) throws IllegalArgumentException, RuntimeException {
            if(destination == null)
                  throw new IllegalArgumentException("RoutingTable: destination cannot be null");
            if(route == null)
                  throw new IllegalArgumentException("RoutingTable: route cannot be null");
            if(this.table.containsKey(destination))
                  throw new RuntimeException("RoutingTable: route already contained");

            this.table.put(destination, route);
      }

      public void setDefault(RoutingInfo route) throws IllegalArgumentException {
            if(route == null)
                  throw new IllegalArgumentException("RoutingTable: route cannot be null");

            IPv4 defaultIP = new IPv4("0.0.0.0", 0);
                  

            if(this.table.containsKey(defaultIP))
                  this.table.remove(defaultIP);
            
            this.add(defaultIP, route);
            
      }

            /**
       * @param destination the destination of the route that will be removed
       * @throws IllegalArgumentException if destination null
       */
      public void remove(IPv4 destination) throws IllegalArgumentException {
            if(destination == null)
                  throw new IllegalArgumentException("RoutingTable: destination cannot be null");

            RoutingInfo routeCheck = this.table.remove(destination);
            if(routeCheck == null)
                  throw new NullPointerException(
                        "RoutingTable: unable to remove " 
                        + destination.stringRepresentation()
                        + " because is not in the table"
                  );
      }

      /**
       * @return the size of the internal hashmap */
      public int size() {
            return this.table.size();
      }

      /** clears internal hashmap */
      public void clear() {
            this.table.clear();
      }
}
