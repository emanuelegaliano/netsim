package com.netsim.app;

/**
 * Factory interface for creating Command instances by identifier.
 */
public interface CommandFactory {
    /**
     * Returns a Command instance corresponding to the given identifier.
     *
     * @param cmd the command identifier (non-null)
     * @return the Command instance
     * @throws IllegalArgumentException if cmd is null or no matching command exists
     */
    Command get(String cmd) throws IllegalArgumentException;
}
