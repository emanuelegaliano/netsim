package com.netsim.app;

import com.netsim.network.NetworkNode;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.utils.Logger;

/**
 * Base class for applications running on a NetworkNode.
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
     * Constructs a new App.
     *
     * @param name     the name of the App (non-null)
     * @param usage    a usage description (non-null)
     * @param factory  the CommandFactory for this App (non-null)
     * @param node     the NetworkNode hosting this App (may be null)
     * @throws IllegalArgumentException if name, usage, or factory is null
     */
    protected App(String name, String usage, CommandFactory factory, NetworkNode node) throws IllegalArgumentException {
        if (name == null || usage == null || factory == null) {
            logger.error("[" + this.CLS + "] name, usage, and factory must be non-null");
            throw new IllegalArgumentException(this.CLS + ": name, usage, and factory cannot be null");
        }
        this.name = name;
        this.usage = usage;
        this.commands = factory;
        this.owner = node;
        logger.info("[" + this.CLS + "] initialized App \"" + this.name + "\" on node: "
            + (this.owner != null ? this.owner.getName() : "null"));
    }

    /**
     * Sets the username for this App.
     *
     * @param newUsername the new username (non-null)
     * @throws IllegalArgumentException if newUsername is null
     */
    public void setUsername(String newUsername) throws IllegalArgumentException {
        if (newUsername == null) {
            logger.error("[" + this.CLS + "] invalid username (null)");
            throw new IllegalArgumentException(this.CLS + ": invalid username");
        }
        this.username = newUsername;
        logger.info("[" + this.CLS + "] username set to \"" + this.username + "\"");
    }

    /**
     * Starts the App. Entry point for interactive or event-driven behavior.
     */
    public abstract void start();

    /**
     * Sends data through the given protocol pipeline.
     *
     * @param stack the ProtocolPipeline to use (non-null)
     * @param data  the payload bytes to send (non-null, non-empty)
     * @throws IllegalArgumentException if arguments are invalid
     */
    public abstract void send(ProtocolPipeline stack, byte[] data) throws IllegalArgumentException;

    /**
     * Receives data from the given protocol pipeline.
     *
     * @param stack the ProtocolPipeline used (non-null)
     * @param data  the received bytes (non-null, non-empty)
     * @throws IllegalArgumentException if arguments are invalid
     */
    public abstract void receive(ProtocolPipeline stack, byte[] data) throws IllegalArgumentException;

    /**
     * Prints a message to the console prefixed by the App’s name.
     *
     * @param message the message to print (may be empty)
     */
    public void printAppMessage(String message) {
        System.out.println(this.name + ": " + message);
        logger.info("[" + this.CLS + "] printed message: " + message.replace("\n", "\\n"));
    }

    /**
     * @return the App’s name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the current user’s username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * @return the owner NetworkNode, may be null
     */
    public NetworkNode getOwner() {
        return this.owner;
    }

    /**
     * Retrieves a Command by its identifier.
     *
     * @param cmdName the command name to retrieve (non-null)
     * @return the Command if found
     * @throws IllegalArgumentException if cmdName is null or command not found
     */
    public Command getCommand(String cmdName) throws IllegalArgumentException {
        if (cmdName == null) {
            logger.error("[" + this.CLS + "] command name cannot be null");
            throw new IllegalArgumentException(this.CLS + ": command name cannot be null");
        }
        Command cmd = this.commands.get(cmdName);
        if (cmd == null) {
            logger.error("[" + this.CLS + "] no command found for: " + cmdName);
            throw new IllegalArgumentException(this.CLS + ": no command found for \"" + cmdName + "\"");
        }
        logger.info("[" + this.CLS + "] getCommand(\"" + cmdName + "\") => " + cmd.name());
        return cmd;
    }
}