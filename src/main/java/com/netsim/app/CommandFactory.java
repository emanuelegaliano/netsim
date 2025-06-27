package com.netsim.app;

public interface CommandFactory {
      /**
       * Using factory method, this method returns an
       * instance of the desired Command through a string key
       * @param cmd the key identifier for the method
       * @return an instance of a command
       */
      Command get(String cmd);
}
