package com.netsim.app.netsimmsg.commands;

import com.netsim.app.Command;
import com.netsim.app.App;
import com.netsim.app.netsimmsg.NetsimMsg;
import com.netsim.network.NetworkNode;

import com.netsim.addresses.IPv4;

public class Connect implements Command {
      private IPv4 destination;

      /**
       * parsing arguments into internal attributes
       * @param args a arguments sequence like the one shown in help method
       * @throws IllegalArgumentException from IP constructor
       */
      private void parseArgs(String[] args) throws IllegalArgumentException {
            // if IPv6 implemented should be modified
            this.destination = new IPv4(args[2], 0);
      }

      /**
       * @throws IllegalArgumentException from parseArgs or if app is not an instance of NetsimMsg
       * @throws RuntimException from node.getDestinationMac,
       *                         if connection is not reached
       */
      public void execute(App app, NetworkNode node, String[] args) throws IllegalArgumentException, RuntimeException {
            if(!(app instanceof NetsimMsg))
                  throw new IllegalArgumentException("Connect: command not called from NetsimMsg");
            
            NetsimMsg netsimMsgApp = (NetsimMsg) app;
            
            this.parseArgs(args);
      }

      public String help() {
            return "connect <server IP>: connects to a NetsimMsg server";
      }
}
