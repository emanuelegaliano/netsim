package com.netsim.app;

import com.netsim.networkstack.ProtocolPipeline;

public abstract class App {
      private final String name;
      private ProtocolPipeline protocols;

      protected App(String appName) throws IllegalArgumentException {
            if(appName == null)
                  throw new IllegalArgumentException(this.getClass().getSimpleName() + ": appName cannot be null");
            
            this.name = appName;
      }

      public String getName() {
            return this.name;
      }

      public abstract void run();
}
