package com.netsim.network.server;

import com.netsim.app.App;
import com.netsim.network.NetworkNodeBuilder;

/**
 * Builder for creating {@link Server} instances.
 * <p>
 * Validates that name, application, routing and ARP tables, and interfaces
 * have been configured before building.
 * </p>
 *
 * @param <AppType> the application type for the Server
 */
public class ServerBuilder<AppType extends App>
      extends NetworkNodeBuilder<Server<AppType>> {

      /**
       * Constructs a new ServerBuilder.
       */
      public ServerBuilder() {
            super();
      }

      /**
       * Builds and returns a configured {@link Server}.
       * <p>
       * Ensures that name, application, routing table, ARP table, and
       * interfaces have all been set and are non‐empty.
       * </p>
       *
       * @return configured Server
       * @throws RuntimeException if any required field is missing or empty
       */
      @Override
      public Server<AppType> build() throws RuntimeException {
            if(this.name == null)
                  throw new RuntimeException(
                  "ServerBuilder: name cannot be null");
            if(this.routingTable.isEmpty())
                  throw new RuntimeException(
                  "ServerBuilder: routing table cannot be empty");
            if(this.arpTable.isEmpty())
                  throw new RuntimeException(
                  "ServerBuilder: ARP table cannot be empty");
            if(this.interfaces.isEmpty())
                  throw new RuntimeException(
                  "ServerBuilder: interfaces must be at least one");

            return new Server<>(
                  this.name,
                  this.routingTable,
                  this.arpTable,
                  this.interfaces
            );
      }
}