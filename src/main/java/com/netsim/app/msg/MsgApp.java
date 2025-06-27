package com.netsim.app.msg;

import java.util.LinkedList;
import java.util.Scanner;

import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.network.Interface;
import com.netsim.network.NetworkNode;
import com.netsim.network.host.Host;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingTable;

public class MsgApp extends App {
      private Scanner input;

      public MsgApp() {
            super(
                  "msg", 
                  "<command> <parameters> (print help for a list of commands)",
                  new MsgCommandFactory(),
                  null      
            );


            this.input = new Scanner(System.in);
      }

      public void askName() {
            this.printAppMessage("Tell me your name: ");
            this.setUsername(this.input.nextLine());
      }

      public void start(NetworkNode node) {
            System.out.println(this.name);
            System.out.println("Welcome to " + this.name);
            this.askName();
            Command connect = this.commands.get("connect");
            connect.setApp(this);
            connect.execute("");
            this.printAppMessage("Hello " + this.name + "\n");

            boolean running = true;
            while(running) {
                  this.printAppMessage("Write the command (type help for a list of commands): ");
                  // parsing
                  String line = this.input.nextLine();
                  String[] parts = line.split("\\s+", 2);
                  String cmdIdentifier = parts[0];
                  String params = parts.length > 1 ? parts[1] : "";
                  
                  // retrieving and running command
                  try {
                        Command cmd = this.commands.get(cmdIdentifier);
                        cmd.setApp(this);
                        cmd.execute(params);


                  } catch(final RuntimeException e) {
                        this.printAppMessage(e.getLocalizedMessage());
                  }
            }
      }

      public void receive(byte[] data) {

      }

      public static void main(String[] args) {
            Host test = new Host("s", new RoutingTable(), new ArpTable(), new LinkedList<Interface>());
            MsgApp app = new MsgApp();
            app.start(test);
      }
}
