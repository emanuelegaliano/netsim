package com.netsim.app;

import com.netsim.utils.Logger;

/**
 * Base class for a user‐invokable command within an App.
 */
public abstract class Command {
    private static final Logger logger = Logger.getInstance();
    private final String CLS = this.getClass().getSimpleName();

    protected final String name;

    /**
     * Constructs a new Command.
     *
     * @param name the command identifier (non-null)
     * @throws IllegalArgumentException if name is null
     */
    protected Command(String name) throws IllegalArgumentException {
        if (name == null) {
            logger.error("[" + this.CLS + "] invalid argument: name cannot be null");
            throw new IllegalArgumentException(this.CLS + ": name cannot be null");
        }
        this.name = name;
        logger.info("[" + this.CLS + "] created command \"" + this.name + "\"");
    }

    /**
     * Executes this command in the context of the given App.
     *
     * @param app  the App invoking this command (non-null)
     * @param args the argument string for the command (non-null, may be empty)
     * @throws RuntimeException           on execution error
     * @throws IllegalArgumentException   on invalid arguments
     */
    public abstract void execute(App app, String args) throws IllegalArgumentException, RuntimeException;

    /**
     * Provides a one‐line description of this command.
     *
     * @return brief help text
     */
    public abstract String help();

    /**
     * Returns the identifier of this command.
     *
     * @return the command name
     */
    public String name() {
        logger.info("[" + this.CLS + "] name() => " + this.name);
        return this.name;
    }
}