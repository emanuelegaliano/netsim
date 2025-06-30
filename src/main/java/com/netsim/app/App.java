package com.netsim.app;

import com.netsim.network.NetworkNode;
import com.netsim.networkstack.ProtocolPipeline;

public abstract class App {
      protected final String name;
      protected final String usage;
      protected final CommandFactory commands;
      protected final NetworkNode owner;
      protected String username;

      /**
       * protected constructor in order to make sure sub-classes
       * creates one and call this
       * @param name the name of the App
       * @param usage the command usage explanation for calling App commands
       * @param factory the factory method class used for retrieving commands
       * @param node the node that runs the app, can be null in constructor (for test)
       * @throws IllegalArgumentException if either name or usage is null
       */
      protected App(String name, String usage, CommandFactory factory, NetworkNode node) throws IllegalArgumentException {
            if(name == null || usage == null || factory == null) 
                  throw new IllegalArgumentException(this.getClass().getSimpleName() + ": name or usage cannot be null");

            this.name = name;
            this.usage = usage;
            this.commands = factory;
            this.owner = node;
      }
      
      public void setUsername(String newUsername) {
            if(newUsername == null)
                  throw new IllegalArgumentException(this.getClass().getSimpleName() + ": invalid username");

            this.username = newUsername;
      }
      
      public abstract void start();
      public abstract void send(ProtocolPipeline stack, byte[] data);
      public abstract void receive(ProtocolPipeline stack, byte[] data);

      /**
       * Prints some message in System.out
       * @param message
       */
      public void printAppMessage(String message) {
            System.out.print(this.name + ": " + message);
      }

      /** @return names of the App */
      public String getName() {
            return this.name;
      }

      /** @return current User's username */
      public String getUsername() {
            return this.username;
      }

      /** @return current owner of the app */
      public NetworkNode getOwner() {
            return this.owner;
      }

      /**
       * Retrieves a Command by its name.
       * 
       * @param name the command name to look up (non-null)
       * @return the Command if found, otherwise null
       * @throws IllegalArgumentException if name is null
       */
      public Command getCommand(String name) {
            if (name == null)
                  throw new IllegalArgumentException(this.getClass().getSimpleName() + ": command name cannot be null");

            return this.commands.get(name);
      }
}