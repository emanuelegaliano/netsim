package com.netsim.app.msg.commands;

import com.netsim.app.Command;

public class Connect extends Command {
      public Connect() {
            super("connect");
      }

      public void execute(String args) {
            if(args != "")
                  throw new IllegalArgumentException("Connect: expected no parameters");

            System.out.println(this.app.getName());
      }

      public String help() {
            return "Connects to the nearest MsgApp server";
      }
}
