package com.netsim.network.host;

import com.netsim.network.NetworkNodeBuilder;

/**
 * Builder for creating {@link Host} instances.
 * <p>
 * Validates that routing and ARP tables contain entries before building.
 * </p>
 */
public class HostBuilder extends NetworkNodeBuilder<Host> {
      /**
       * Constructs a new HostBuilder with default routing and ARP tables, and no interfaces.
       */
      public HostBuilder() {
            super();
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
            // check that routing table has at least one entry
            if(this.routingTable.isEmpty())
                  throw new RuntimeException("HostBuilder: routing table cannot be empty");
            // check that ARP table has at least one entry
            if(this.arpTable.isEmpty())
                  throw new RuntimeException("HostBuilder: ARP table cannot be empty");
            // check that interfaces list has at least one
            if(this.interfaces.isEmpty())
                  throw new RuntimeException("HostBuilder: interfaces must be at least one");
            // build Host using validated parameters
            return new Host(
                  this.name, 
                  this.routingTable, 
                  this.arpTable, 
                  this.interfaces
                  );
      }
}
