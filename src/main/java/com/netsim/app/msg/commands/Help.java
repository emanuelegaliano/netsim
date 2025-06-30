package com.netsim.app.msg.commands;

import java.util.LinkedList;
import java.util.List;

import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.app.msg.MsgCommandFactory;
import com.netsim.utils.Logger;

/**
 * Displays a list of available MSG commands with brief descriptions.
 */
public class Help extends Command {
    private static final Logger logger = Logger.getInstance();

    /**
     * Constructs the Help command.
     */
    public Help() {
        super("help");
    }

    /**
     * Executes the help command, printing all available commands.
     *
     * @param app  the application context to print messages
     * @param args the command arguments (must be empty)
     * @throws IllegalArgumentException if any unexpected args are provided
     */
    @Override
    public void execute(App app, String args) throws IllegalArgumentException {
        String cls = this.getClass().getSimpleName();
        if (!args.equals("")) {
            String msg = "Unexpected parameters: \"" + args + "\"";
            logger.error("[" + cls + "] " + msg);
            throw new IllegalArgumentException(cls + ": expected no parameters");
        }

        StringBuilder helpMsg = new StringBuilder("List of commands\n");
        MsgCommandFactory factory = new MsgCommandFactory();

        try {
            for (String identifier : this.generateCommands()) {
                Command cmd = factory.get(identifier);
                helpMsg.append(cmd.name()).append(": ").append(cmd.help()).append("\n");
            }
            this.print(app, helpMsg.toString());
            logger.info("[" + cls + "] executed successfully");
        } catch (RuntimeException e) {
            logger.debug("[" + cls + "] exception generating help: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Generates the identifiers of all supported commands.
     *
     * @return a list of command names
     */
    private List<String> generateCommands() {
        List<String> commands = new LinkedList<>();
        commands.add("help");
        commands.add("send");
        return commands;
    }

    /**
     * Returns a short description of this command.
     *
     * @return help text
     */
    @Override
    public String help() {
        return "Displays this list of commands";
    }

    /**
     * Helper to send output to the application.
     *
     * @param app the application to print to
     * @param msg the message to print
     */
    private void print(App app, String msg) {
        app.printAppMessage(msg);
    }
}