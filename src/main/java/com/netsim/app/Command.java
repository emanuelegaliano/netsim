package com.netsim.app;

public abstract class Command {
      protected final String name;
      protected App app;

      protected Command(String name) {
            if(name == null)
                  throw new IllegalArgumentException(this.getClass().getSimpleName() + ": invalid argument");

            this.name = name;
            this.app = null;
      }

      public abstract void execute(String args);
      /**
       * A constant help message description
       * @return short message description
       */
      public abstract String help();
      
      public String name() {
            return this.name;
      }

      public void setApp(App newApp) {
            if(app == null)
                  throw new IllegalArgumentException(this.getClass().getSimpleName() + ": app cannot be null");

            this.app = newApp;
      }
}