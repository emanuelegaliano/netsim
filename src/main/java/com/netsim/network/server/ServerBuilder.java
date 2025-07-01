package com.netsim.network.server;

import com.netsim.app.App;
import com.netsim.network.NetworkNodeBuilder;
import com.netsim.utils.Logger;

/**
 * Builder for creating {@link Server} instances.
 * <p>
 * Validates that all required fields (name, routing table, ARP table,
 * and interfaces) are configured before building.
 * </p>
 *
 * @param <AppType> the application type for the Server
 */
public class ServerBuilder<AppType extends App>
        extends NetworkNodeBuilder<Server<AppType>> {

    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = ServerBuilder.class.getSimpleName();

    /**
     * Constructs a new ServerBuilder.
     */
    public ServerBuilder() {
        super();
        logger.info("[" + CLS + "] initialized");
    }

    /**
     * Builds and returns a configured {@link Server}.
     *
     * @return the configured Server
     * @throws RuntimeException if name is null, or if routing table,
     *         ARP table, or interfaces are empty
     */
    @Override
    public Server<AppType> build() throws RuntimeException {
        if (this.name == null) {
            String msg = "name cannot be null";
            logger.error("[" + CLS + "] " + msg);
            throw new RuntimeException("ServerBuilder: " + msg);
        }
        if (this.routingTable.isEmpty()) {
            String msg = "routing table cannot be empty";
            logger.error("[" + CLS + "] " + msg);
            throw new RuntimeException("ServerBuilder: " + msg);
        }
        if (this.arpTable.isEmpty()) {
            String msg = "ARP table cannot be empty";
            logger.error("[" + CLS + "] " + msg);
            throw new RuntimeException("ServerBuilder: " + msg);
        }
        if (this.interfaces.isEmpty()) {
            String msg = "interfaces must be at least one";
            logger.error("[" + CLS + "] " + msg);
            throw new RuntimeException("ServerBuilder: " + msg);
        }

        Server<AppType> server = new Server<>(
            this.name,
            this.routingTable,
            this.arpTable,
            this.interfaces
        );
        logger.info("[" + CLS + "] built Server \"" + this.name + "\" successfully");
        return server;
    }
}