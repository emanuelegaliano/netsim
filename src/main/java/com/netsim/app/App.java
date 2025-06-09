package com.netsim.app;

import com.netsim.networkstack.ProtocolPipeline;

public abstract class App {
      protected final String name;
      protected final String usage;
      protected ProtocolPipeline protocols;

      protected App(String name, String usage) throws IllegalArgumentException {
            if(name == null || usage == null)
                  throw new IllegalArgumentException(this.getClass().getSimpleName() + ": name or usage cannot be null");

            this.usage = usage;
            this.name = name;
      }

      public String getName() {
            return this.name;
      }
}
