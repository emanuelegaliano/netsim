package com.netsim.app.msg;

import java.util.LinkedList;
import java.util.Scanner;

import com.netsim.addresses.IPv4;
import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.network.Interface;
import com.netsim.network.NetworkNode;
import com.netsim.network.host.Host;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.MSG.MSGProtocol;
import com.netsim.protocols.UDP.UDPProtocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingTable;

public class MsgClient extends App {
      private Scanner input;
      private IPv4 serverIP;

      public MsgClient(NetworkNode node, IPv4 serverIP) throws IllegalArgumentException {
            super(
                  "msg", 
                  "<command> <parameters> (print help for a list of commands)",
                  new MsgCommandFactory(),
                  node      
            );
            if(serverIP == null)
                  throw new IllegalArgumentException("MsgClient: invalid server ip");

            this.serverIP = serverIP;
            this.input = new Scanner(System.in);
      }

      public void askName() {
            this.printAppMessage("Tell me your name: ");
            this.setUsername(this.input.nextLine());
      }

      public void start(NetworkNode node) {
            // autentication
            System.out.println("Welcome to " + this.name);
            this.askName();

            // connecting to the nearest server
            Command connect = this.commands.get("connect");
            connect.execute(this, "");

            // authentication complete
            this.printAppMessage("Hello " + this.username + "\n");

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
                        cmd.execute(this, params);


                  } catch(final RuntimeException e) {
                        this.printAppMessage(e.getLocalizedMessage());
                  }
            }
      }

      public void receive(byte[] data) {

      }

      /**
       * Adds transport protocol in the stack
       */
      public void send(ProtocolPipeline stack, byte[] data) throws IllegalArgumentException, RuntimeException {
            if(stack == null || data == null || data.length == 0)
                  throw new IllegalArgumentException("MsgClient: invalid arguments");
            if(this.owner == null)
                  throw new RuntimeException("MsgClient: node is null");

            stack.push(
                  new UDPProtocol(
                        this.owner.getMTU() - 20 - 20, // MTU - MSGProtocolheader - UDPProtocolHeader 
                        this.owner.randomPort(), 
                        MSGProtocol.port()
                        )
                  );
            
            this.owner.send(serverIP, stack, data);

      }

      public IPv4 getDestination() {
            return this.serverIP;
      }

      public static void main(String[] args) {
            Host test = new Host("s", new RoutingTable(), new ArpTable(), new LinkedList<Interface>());
            MsgClient app = new MsgClient(
                  null, 
                  new IPv4("192.168.1.1", 24)
                  );
            app.start(test);
      }
}
