package com.netsim.network.host;

import com.netsim.network.NetworkNodeBuilder;
import com.netsim.utils.Logger;

/**
 * Builder for creating {@link Host} instances.
 * <p>
 * Validates that routing and ARP tables contain entries before building.
 * </p>
 */
public class HostBuilder extends NetworkNodeBuilder<Host> {
      private static final Logger logger = Logger.getInstance();
      private static final String CLS = HostBuilder.class.getSimpleName();

      /**
       * Constructs a new HostBuilder with default routing and ARP tables, and no interfaces.
       */
      public HostBuilder() {
            super();
            logger.info("[" + CLS + "] initialized");
      }

      /**
       * Builds and returns a {@link Host}.
       * <p>
       * Ensures that both routing and ARP tables are not empty.
       * </p>
       *
       * @return configured Host
       * @throws RuntimeException if routing or ARP table is empty
       */
      public Host build() throws RuntimeException {
            if (this.routingTable.isEmpty()) {
                  logger.error("[" + CLS + "] routing table cannot be empty");
                  throw new RuntimeException("HostBuilder: routing table cannot be empty");
            }
            if (this.arpTable.isEmpty()) {
                  logger.error("[" + CLS + "] ARP table cannot be empty");
                  throw new RuntimeException("HostBuilder: ARP table cannot be empty");
            }
            if (this.interfaces.isEmpty()) {
                  logger.error("[" + CLS + "] interfaces must be at least one");
                  throw new RuntimeException("HostBuilder: interfaces must be at least one");
            }

            Host host = new Host(
                  this.name,
                  this.routingTable,
                  this.arpTable,
                  this.interfaces
            );
            logger.info("[" + CLS + "] built Host \"" + this.name + "\" successfully");
            return host;
      }
}