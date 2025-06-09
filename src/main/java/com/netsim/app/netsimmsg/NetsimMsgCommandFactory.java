package com.netsim.app.netsimmsg;

import com.netsim.app.Command;
import com.netsim.app.netsimmsg.commands.Help;

public class NetsimMsgCommandFactory {
      public static Command get(String command) {
            switch(command.toLowerCase()) {
                  case "help":
                        return new Help();
                  default:
                        throw new IllegalArgumentException("NetsimCommandFactory: unable to find command");
            }
      }
}
