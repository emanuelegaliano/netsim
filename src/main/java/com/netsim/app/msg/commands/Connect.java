package com.netsim.app.msg.commands;

import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.MSG.MSGProtocol;

public class Connect extends Command {
      public Connect() {
            super("connect");
      }

      public void execute(App app, String args) {
            if(app == null)
                  throw new IllegalArgumentException("Connect: app cannot be null");
            if(args != "")
                  throw new IllegalArgumentException("Connect: expected no parameters");

            String connectMsg = "connect";
            ProtocolPipeline stack = new ProtocolPipeline();
            stack.push(new MSGProtocol(app.getUsername()));
            app.send(stack, connectMsg.getBytes());
      }

      public String help() {
            return "Connects to the nearest MsgApp server";
      }
}
