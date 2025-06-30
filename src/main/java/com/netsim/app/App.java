package com.netsim.app;

import com.netsim.network.NetworkNode;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.utils.Logger;

/**
 * Base class for applications running on NetworkNode.
 */
public abstract class App {
    private static final Logger logger = Logger.getInstance();
    private final String CLS = this.getClass().getSimpleName();

    protected final String name;
    protected final String usage;
    protected final CommandFactory commands;
    protected final NetworkNode owner;
    protected String username;

    /**
     * @param name    the name of the App (non-null)
     * @param usage   usage description (non-null)
     * @param factory factory for commands (non-null)
     * @param node    the node that runs the app (may be null for tests)
     * @throws IllegalArgumentException if name, usage, or factory is null
     */
    protected App(String name, String usage, CommandFactory factory, NetworkNode node) {
        if (name == null || usage == null || factory == null) {
            logger.error("[" + CLS + "] name, usage, and factory must be non-null");
            throw new IllegalArgumentException(CLS + ": name or usage cannot be null");
        }
        this.name = name;
        this.usage = usage;
        this.commands = factory;
        this.owner = node;
        logger.info("[" + CLS + "] initialized App \"" + name + "\" on node: "
            + (node != null ? node.getName() : "null"));
    }

    /**
     * Sets the user name for this App.
     *
     * @param newUsername non-null user name
     * @throws IllegalArgumentException if newUsername is null
     */
    public void setUsername(String newUsername) {
        if (newUsername == null) {
            logger.error("[" + CLS + "] invalid username (null)");
            throw new IllegalArgumentException(CLS + ": invalid username");
        }
        this.username = newUsername;
        logger.info("[" + CLS + "] username set to \"" + newUsername + "\"");
    }

    public abstract void start();
    public abstract void send(ProtocolPipeline stack, byte[] data);
    public abstract void receive(ProtocolPipeline stack, byte[] data);

    /**
     * Prints some message to System.out.
     *
     * @param message the message to print
     */
    public void printAppMessage(String message) {
        System.out.println(this.name + ": " + message);
        logger.info("[" + CLS + "] printed message: " + message.replace("\n", "\\n"));
    }

    /** @return the App’s name */
    public String getName() {
        return this.name;
    }

    /** @return current user’s username */
    public String getUsername() {
        return this.username;
    }

    /** @return the owner NetworkNode */
    public NetworkNode getOwner() {
        return this.owner;
    }

    /**
     * Retrieves a Command by its name.
     *
     * @param cmdName the command name to look up (non-null)
     * @return the Command if found, otherwise null
     * @throws IllegalArgumentException if cmdName is null
     */
    public Command getCommand(String cmdName) {
        if (cmdName == null) {
            logger.error("[" + CLS + "] command name cannot be null");
            throw new IllegalArgumentException(CLS + ": command name cannot be null");
        }
        Command cmd = this.commands.get(cmdName);
        logger.info("[" + CLS + "] getCommand(\"" + cmdName + "\") => "
            + (cmd != null ? cmd.name() : "null"));
        return cmd;
    }
}