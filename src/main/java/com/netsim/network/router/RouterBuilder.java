package com.netsim.network.router;

import com.netsim.network.NetworkNodeBuilder;
import com.netsim.utils.Logger;

/**
 * Builder for creating {@link Router} instances.
 * <p>
 * Validates that the routing table, ARP table, and interfaces contain entries
 * before constructing the Router.
 * </p>
 */
public class RouterBuilder extends NetworkNodeBuilder<Router> {
      private static final Logger logger = Logger.getInstance();
      private static final String CLS = RouterBuilder.class.getSimpleName();

      /**
       * Constructs a new RouterBuilder with empty routing and ARP tables,
       * and no interfaces.
       */
      public RouterBuilder() {
            super();
            logger.info("[" + CLS + "] initialized");
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
            if (this.routingTable.isEmpty()) {
                  logger.error("[" + CLS + "] routing table cannot be empty");
                  throw new RuntimeException("RouterBuilder: routing table cannot be empty");
            }
            if (this.arpTable.isEmpty()) {
                  logger.error("[" + CLS + "] ARP table cannot be empty");
                  throw new RuntimeException("RouterBuilder: ARP table cannot be empty");
            }
            if (this.interfaces.isEmpty()) {
                  logger.error("[" + CLS + "] interfaces must be at least one");
                  throw new RuntimeException("RouterBuilder: interfaces must be at least one");
            }

            Router router = new Router(
                  this.name,
                  this.routingTable,
                  this.arpTable,
                  this.interfaces
            );
            logger.info("[" + CLS + "] built Router \"" + this.name + "\" successfully");
            return router;
      }
}