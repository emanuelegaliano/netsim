package com.netsim.app.netsimmsg;

import java.util.Arrays;
import java.util.Scanner;

import com.netsim.app.App;
import com.netsim.app.Command;

import com.netsim.node.NetworkNode;

public class NetsimMsg extends App {
      private String username;
      private final NetworkNode node;
      private Scanner input;

      public NetsimMsg(NetworkNode node) throws IllegalArgumentException {
            super(
                  "Netsim-msg", 
                  "netsim-msg <command> <parameters> ('Netsim-msg help' for more)"
            );
            if(node == null)
                  throw new IllegalArgumentException("NetsimMsg: node cannot be null");
            
            this.node = node;
            this.input = new Scanner(System.in);
      }

      private void askUsername() {
            System.out.print("Tell me your username: ");
            this.username = this.input.nextLine();
      }

      private void printUsage() {
            System.out.println("Usage: " + this.usage);
      }

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
                        cmd.execute(this.node, cmdArgs);
                  } catch(final IllegalArgumentException e) {
                        this.printUsage();
                        continue;
                  }                  
            }
      }
}
