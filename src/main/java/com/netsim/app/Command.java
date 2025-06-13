package com.netsim.app;

import com.netsim.network.NetworkNode;

public interface Command {
      /**
       * This method makes the Command able to execute 
       * command using NetworkNode methods and attributes.
       * @param node the network node
       * @param args the arguments of the Command
       */
      void execute(App app, NetworkNode node, String[] args);
      /**
       * A costant help message description
       * @return short message description
       */
      String help();
}
