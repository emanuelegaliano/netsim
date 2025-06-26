package com.netsim.network.router;

import com.netsim.network.NetworkNodeBuilder;

/**
 * Builder for creating {@link Router} instances.
 * <p>
 * Validates that routing table, ARP table, and interfaces contain entries before building.
 * </p>
 */
public class RouterBuilder extends NetworkNodeBuilder<Router> {
      /**
       * Constructs a new RouterBuilder with default routing and ARP tables, and no interfaces.
       */
      public RouterBuilder() {
            super();
      }

      /**
       * Builds and returns a {@link Router}.
       *
       * @return configured Router
       * @throws RuntimeException if routing table, ARP table, or interfaces list is empty
       */
      @Override
      public Router build() throws RuntimeException {
            // check that routing table has entries
            if(this.routingTable.isEmpty())
                  throw new RuntimeException("RouterBuilder: routing table cannot be empty");
            // check that ARP table has entries
            if(this.arpTable.isEmpty())
                  throw new RuntimeException("RouterBuilder: ARP table cannot be empty");
            // check that at least one interface exists
            if(this.interfaces.isEmpty())
                  throw new RuntimeException("RouterBuilder: interfaces must be at least one");

            return new Router(
                  this.name,
                  this.routingTable,
                  this.arpTable,
                  this.interfaces
            );
      }
}