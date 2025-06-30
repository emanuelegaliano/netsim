package com.netsim.app.msg;

import com.netsim.app.Command;
import com.netsim.app.CommandFactory;
import com.netsim.app.msg.commands.Help;
import com.netsim.app.msg.commands.Send;
import com.netsim.utils.Logger;

public class MsgCommandFactory implements CommandFactory {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = MsgCommandFactory.class.getSimpleName();

    /**
     * @throws IllegalArgumentException if no command was found
     */
    @Override
    public Command get(String cmd) throws IllegalArgumentException {
        if (cmd == null) {
            logger.error("[" + CLS + "] command identifier is null");
            throw new IllegalArgumentException(CLS + ": command name cannot be null");
        }
        switch (cmd.toLowerCase()) {
            case "help":
                logger.info("[" + CLS + "] creating Help command");
                return new Help();
            case "send":
                logger.info("[" + CLS + "] creating Send command");
                return new Send();
            default:
                logger.error("[" + CLS + "] no command found for identifier: " + cmd);
                throw new IllegalArgumentException(CLS + ": no command found for \"" + cmd + "\"");
        }
    }
}