package com.netsim.app;

import com.netsim.node.NetworkNode;

public interface Command {
      /**
       * This method makes the Command able to execute 
       * command using NetworkNode methods and attributes.
       * @param node the network node
       * @param args the arguments of the Command
       */
      void execute(NetworkNode node, String[] args);
      /**
       * A costant help message description
       * @return short message description
       */
      String help();
}
