package com.netsim.app.netsimmsg;

import com.netsim.app.Command;
import com.netsim.app.netsimmsg.commands.*;

public class NetsimMsgCommandFactory {
      /**
       * This method is the getter factory method
       * of the App NetsimMsg
       * @param command the command string used by the factory method
       *                to identify the Command to return
       * @return a subclass of Command based on param command
       * @throws IllegalArgumentException if no command is found
       */
      public static Command get(String command) throws IllegalArgumentException {
            switch(command.toLowerCase()) {
                  case "help":
                        return new Help();
                  case "connect":
                        return new Connect();
                  default:
                        throw new IllegalArgumentException("NetsimCommandFactory: unable to find command");
            }
      }
}
