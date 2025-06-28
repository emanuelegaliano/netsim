package com.netsim.app;

public abstract class Command {
      protected final String name;

      protected Command(String name) {
            if(name == null)
                  throw new IllegalArgumentException(this.getClass().getSimpleName() + ": invalid argument");

            this.name = name;
      }

      public abstract void execute(App app, String args);
      /**
       * A constant help message description
       * @return short message description
       */
      public abstract String help();
      
      public String name() {
            return this.name;
      }
}