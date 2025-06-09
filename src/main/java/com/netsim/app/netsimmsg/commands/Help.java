package com.netsim.app.netsimmsg.commands;

import com.netsim.app.Command;
import com.netsim.app.netsimmsg.NetsimMsgCommandFactory;
import com.netsim.node.NetworkNode;

public class Help implements Command {
      String[] commands = {"help", "send"};
      
      /**
       * Print a list of all the commands in NetsimMsg App
       */
      public void execute(NetworkNode node, String[] args) {
            String commandList = "Command list: \n";
            
            for(String command : commands) {
                  try {
                        commandList += NetsimMsgCommandFactory.get(command).help() + "\n";
                  } catch(final IllegalArgumentException e) {
                        continue;
                  }
            }

            System.out.println(commandList);
      }

      public String help() {
            return "help: shows this list";
      }
}
