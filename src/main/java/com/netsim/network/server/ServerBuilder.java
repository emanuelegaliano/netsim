package com.netsim.network.server;

import com.netsim.app.App;
import com.netsim.network.NetworkNodeBuilder;

/**
 * Builder for creating {@link Server} instances.
 * <p>
 * Validates that routing and ARP tables and interfaces contain entries before building.
 * </p>
 *
 * @param <AppType> the application type for the Server
 */
public class ServerBuilder<AppType extends App> extends NetworkNodeBuilder<Server<AppType>> {
      private AppType app;

      /**
       * Constructs a new ServerBuilder with the specified application.
       *
       * @param app non-null application instance
       */
      public ServerBuilder() {
            super();
      }

      /**
       * Sets internal app
       * @param app
       * @throws IllegalArgumentException if app is null
       */
      public ServerBuilder<AppType> setApp(AppType app) throws IllegalArgumentException {
            if(app == null)
                  throw new IllegalArgumentException("ServerBuilder: app cannot be null");

            this.app = app;
            return this;
      }

      /**
       * Builds and returns a {@link Server}.
       * <p>
       * Ensures that routing table, ARP table, and interfaces are not empty.
       * </p>
       *
       * @return configured Server
       * @throws RuntimeException if any required collection is empty
       */
      @Override
      public Server<AppType> build() throws RuntimeException {
            // check that routing table has entries
            if (this.routingTable.isEmpty())
                  throw new RuntimeException("ServerBuilder: routing table cannot be empty");
            // check that ARP table has entries
            if (this.arpTable.isEmpty())
                  throw new RuntimeException("ServerBuilder: ARP table cannot be empty");
            // check that at least one interface exists
            if (this.interfaces.isEmpty())
                  throw new RuntimeException("ServerBuilder: interfaces must be at least one");

            return new Server<>(this.name,
                              this.routingTable,
                              this.arpTable,
                              this.interfaces,
                              this.app);
      }
}