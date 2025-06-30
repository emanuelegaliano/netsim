package com.netsim.app.msg;

import com.netsim.app.Command;
import com.netsim.app.CommandFactory;
import com.netsim.app.msg.commands.Help;
import com.netsim.app.msg.commands.Send;
import com.netsim.utils.Logger;

/**
 * Factory for MSG application commands.
 */
public class MsgCommandFactory implements CommandFactory {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = MsgCommandFactory.class.getSimpleName();

    /**
     * Returns a Command instance matching the given identifier.
     *
     * @param cmd the command identifier (non-null)
     * @return the matching Command
     * @throws IllegalArgumentException if cmd is null or no matching command exists
     */
    @Override
    public Command get(String cmd) throws IllegalArgumentException {
        if (cmd == null) {
            String msg = "command name cannot be null";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(CLS + ": " + msg);
        }

        switch (cmd.toLowerCase()) {
            case "help":
                logger.info("[" + CLS + "] creating Help command");
                return new Help();
            case "send":
                logger.info("[" + CLS + "] creating Send command");
                return new Send();
            default:
                String msg = "no command found for \"" + cmd + "\"";
                logger.error("[" + CLS + "] " + msg);
                throw new IllegalArgumentException(CLS + ": " + msg);
        }
    }
}