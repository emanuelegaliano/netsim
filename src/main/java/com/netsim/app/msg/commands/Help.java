package com.netsim.app.msg.commands;

import java.util.LinkedList;
import java.util.List;

import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.app.msg.MsgCommandFactory;

public class Help extends Command {
      public Help() {
            super("help");
      }

      public void execute(App app, String args) {
            if(args != "")
                  throw new IllegalArgumentException("HelpCommand: expected no parameters");

            String helpMsg = "List of commands\n";
            MsgCommandFactory factory = new MsgCommandFactory();

            for(String cmdIdentifiyer : this.generateCommands()) {
                  Command cmd = factory.get(cmdIdentifiyer);
                  helpMsg += cmd.name() + ": " + cmd.help() + "\n";
            }

            app.printAppMessage(helpMsg);
      }

      private List<String> generateCommands() {
            final List<String> commands = new LinkedList<>();
            commands.add("help");

            return commands;
      }

      public String help() {
            return "returns a list of commands";
      }
}
