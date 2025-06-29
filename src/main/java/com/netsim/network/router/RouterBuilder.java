package com.netsim.network.router;

import com.netsim.network.NetworkNodeBuilder;

/**
 * Builder for creating {@link Router} instances.
 * <p>
 * Validates that the routing table, ARP table, and interfaces contain entries
 * before constructing the Router.
 * </p>
 */
public class RouterBuilder extends NetworkNodeBuilder<Router> {
      /**
       * Constructs a new RouterBuilder with empty routing and ARP tables,
       * and no interfaces.
       */
      public RouterBuilder() {
            super();
      }

      /**
       * Builds and returns a {@link Router}.
       * <p>
       * Ensures that routing table, ARP table, and interfaces are not empty.
       * </p>
       *
       * @return configured Router
       * @throws RuntimeException if any required collection is empty
       */
      @Override
      public Router build() throws RuntimeException {
            // check that routing table has at least one entry
            if(this.routingTable.isEmpty())
                  throw new RuntimeException("RouterBuilder: routing table cannot be empty");

            // check that ARP table has at least one entry
            if(this.arpTable.isEmpty())
                  throw new RuntimeException("RouterBuilder: ARP table cannot be empty");

            // check that at least one interface exists
            if(this.interfaces.isEmpty())
                  throw new RuntimeException("RouterBuilder: interfaces must be at least one");

            // build Router using validated parameters
            return new Router(
                  this.name,
                  this.routingTable,
                  this.arpTable,
                  this.interfaces
            );
      }
}