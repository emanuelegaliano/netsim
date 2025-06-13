package com.netsim.app;

import com.netsim.networkstack.ProtocolPipeline;

public abstract class App {
      protected final String name;
      protected final String usage;
      protected ProtocolPipeline protocols;

      /**
       * protected constructor in order to make sure sub-classes
       * creates one and call this
       * @param name the name of the App
       * @param usage the command usage explanation for calling App commands
       * @throws IllegalArgumentException if either name or usage is null
       */
      protected App(String name, String usage) throws IllegalArgumentException {
            if(name == null || usage == null)
                  throw new IllegalArgumentException(this.getClass().getSimpleName() + ": name or usage cannot be null");

            this.usage = usage;
            this.name = name;
      }
      
      public abstract void start();
      public abstract void receive(byte[] data);
      public abstract void printAppMessage(String message);

      /** @return names of the App */
      public String getName() {
            return this.name;
      }
}
