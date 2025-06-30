package com.netsim.app.msg.commands;

import java.util.LinkedList;
import java.util.List;

import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.app.msg.MsgCommandFactory;
import com.netsim.utils.Logger;

public class Help extends Command {
    private static final Logger logger = Logger.getInstance();

    public Help() {
        super("help");
    }

    @Override
    public void execute(App app, String args) {
        String cls = this.getClass().getSimpleName();
        if (!args.equals("")) {
            logger.error("[" + cls + "] Unexpected parameters: \"" + args + "\"");
            throw new IllegalArgumentException(cls + ": expected no parameters");
        }

        String helpMsg = "List of commands\n";
        MsgCommandFactory factory = new MsgCommandFactory();

        try {
            for (String cmdIdentifier : this.generateCommands()) {
                Command cmd = factory.get(cmdIdentifier);
                helpMsg += cmd.name() + ": " + cmd.help() + "\n";
            }
            app.printAppMessage(helpMsg);
            logger.info("[" + cls + "] Executed successfully");
        } catch (RuntimeException e) {
            logger.debug("[" + cls + "] Exception in generating help: " + e.getLocalizedMessage());
            throw e;
        }
    }

    private List<String> generateCommands() {
        final List<String> commands = new LinkedList<>();
        commands.add("help");
        commands.add("send");
        return commands;
    }

    @Override
    public String help() {
        return "returns a list of commands";
    }
}