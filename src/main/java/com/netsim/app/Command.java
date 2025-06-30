package com.netsim.app;

import com.netsim.utils.Logger;

public abstract class Command {
    private static final Logger logger = Logger.getInstance();
    private final String CLS = this.getClass().getSimpleName();

    protected final String name;

    /**
     * @param name the command name (non-null)
     * @throws IllegalArgumentException if name is null
     */
    protected Command(String name) {
        if (name == null) {
            logger.error("[" + CLS + "] invalid argument: name cannot be null");
            throw new IllegalArgumentException(CLS + ": invalid argument");
        }
        this.name = name;
        logger.info("[" + CLS + "] created command \"" + name + "\"");
    }

    /**
     * Execute this command.
     * 
     * @param app  the App in which context this command runs
     * @param args the arguments string (may be empty but non-null)
     */
    public abstract void execute(App app, String args);

    /**
     * Short help message for this command.
     * 
     * @return a one‐line description of the command
     */
    public abstract String help();

    /**
     * Returns the name of this command.
     * 
     * @return the command identifier
     */
    public String name() {
        logger.info("[" + CLS + "] name() => " + this.name);
        return this.name;
    }
}