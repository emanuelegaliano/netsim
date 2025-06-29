package com.netsim.app.msg;

import com.netsim.app.Command;
import com.netsim.app.CommandFactory;
import com.netsim.app.msg.commands.*;

public class MsgCommandFactory implements CommandFactory {
      /**
       * @throws IllegalArgumentException if no command was found
       */
      public Command get(String cmd) throws IllegalArgumentException {
            switch(cmd.toLowerCase()) {
                  case "help":
                        return new Help();
                  case "send":
                        return new Send();
                  default:
                        throw new IllegalArgumentException("CommandFactory: no command found");
            }
      }
}
