package com.netsim.app.netsimmsg;

import java.util.Arrays;
import java.util.Scanner;

import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.network.NetworkNode;
import com.netsim.addresses.IPv4;

public class NetsimMsg extends App {
      private String username;
      private final NetworkNode node;
      private IPv4 serverIP;
      private Scanner input;

      public NetsimMsg(NetworkNode node) throws IllegalArgumentException {
            super(
                  "Netsim-msg", 
                  "netsim-msg <command> <parameters> ('help' for more)"
            );
            if(node == null)
                  throw new IllegalArgumentException("NetsimMsg: node cannot be null");
            
            this.node = node;
            this.input = new Scanner(System.in);
            this.serverIP = null;
      }

      /**
       * server setted by Connect Command
       * @param server ip of server
       * @throws IllegalArgumentException if server is null,
       *                                  if server ip is a subnet, 
       *                                     should be a single ip
       * 
       */
      public void setServer(IPv4 server) throws IllegalArgumentException {
            if(server == null)
                  throw new IllegalArgumentException("NetsimMsg: server ip cannot be null");
            if(server.getMask() != 0)
                  throw new IllegalArgumentException("NetsimMsg: server ip cannot be a subnet");
            
            this.serverIP = server;
      }

      public IPv4 getServer() {
            return this.serverIP;
      }

      /** first method called on start() in order to set username */
      private void askUsername() {
            System.out.print("Tell me your username: ");
            this.username = this.input.nextLine();
      }

      /** prints to sout the usage of the class*/
      private void printUsage() {
            System.out.println("Usage: " + this.usage);
      }

      /**
       * Starts the apps in a while, waiting for commands
       * that be created through factory method and executed on 
       * the node
       */
      public void start() {   
            this.askUsername();
            System.out.println(
                  "Hello " 
                  + this.username 
                  + ", Welcome to " 
                  + this.name 
                  + "\nType help for more information\nType exit for closing app\n"
            );
            
            boolean running = true;
            while(running) {
                  System.out.print("Tell me the command\n>> ");
                  String stringCommand = input.nextLine();

                  System.out.println(stringCommand);
                  
                  // checking if exit has been type
                  if(stringCommand.equals("exit")) {
                        running = false;
                        break;
                  }

                  // tokenizing command
                  String[] tokens = stringCommand.trim().split("\\s+");
                  String cmdName = tokens[0];

                  // executing command
                  String[] cmdArgs = Arrays.copyOfRange(tokens, 1, tokens.length);
                  try {
                        Command cmd = NetsimMsgCommandFactory.get(cmdName);
                        cmd.execute(this, this.node, cmdArgs);
                  } catch(final IllegalArgumentException e) {
                        this.printUsage();
                        continue;
                  } catch(final RuntimeException e) {
                        System.out.println(e.getLocalizedMessage());
                  }    
            }
      }

      public void receive(IPv4 source, byte[] data) {

      }

      public void printAppMessage(String message) {
            
      }
}
