package com.netsim.app.msg;

import java.util.Scanner;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.NetworkNode;
import com.netsim.network.host.Host;
import com.netsim.network.host.HostBuilder;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.MSG.MSGProtocol;
import com.netsim.protocols.UDP.UDPProtocol;

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

      public void start() {
            // autentication
            System.out.println("Welcome to " + this.name);
            this.askName();

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

      public void receive(ProtocolPipeline stack, byte[] data) {
            // validate arguments
            if(stack==null||data==null||data.length==0)
                  throw new IllegalArgumentException("MsgClient.receive: invalid arguments");

            // 1) strip off UDP transport
            Protocol p1 = stack.pop();
            if(!(p1 instanceof UDPProtocol))
                  throw new RuntimeException("MsgClient.receive: expected UDP protocol");
            UDPProtocol udp = (UDPProtocol)p1;
            byte[] msgFrame = udp.decapsulate(data);

            // 2) strip off MSG application layer
            Protocol p2 = stack.pop();
            if(!(p2 instanceof MSGProtocol))
                  throw new RuntimeException("MsgClient.receive: expected MSG protocol");
            MSGProtocol msgProto = (MSGProtocol)p2;
            byte[] payloadBytes = msgProto.decapsulate(msgFrame);

            // 3) decode and display
            String sender = msgProto.getUser();
            String message = new String(payloadBytes, java.nio.charset.StandardCharsets.UTF_8);
            this.printAppMessage(sender + ": " + message + "\n");
      }

      /**
       * Adds transport protocol in the stack
       */
      public void send(ProtocolPipeline stack, byte[] data) throws IllegalArgumentException, RuntimeException {
            if(stack == null || data == null || data.length == 0)
                  throw new IllegalArgumentException("MsgClient: invalid arguments");
            if(this.owner == null)
                  throw new RuntimeException("MsgClient: node is null");

            UDPProtocol protocol = new UDPProtocol(
                  this.owner.getMTU() - 20 - 20, // MTU - MSGProtocolheader - UDPProtocolHeader 
                  this.owner.randomPort(), 
                  MSGProtocol.port()
            );
            
            byte[] encapsulated = protocol.encapsulate(data);
            stack.push(protocol);
            this.owner.send(serverIP, stack, encapsulated);
      }

      public static void main(String[] args) {
            Host test = new HostBuilder()
            .addInterface(
                  new Interface(
                        new NetworkAdapter(
                              "d", 
                              1500,
                              new Mac("AA:BB:CC:DD:EE:FF")
                        ),
                  new IPv4("192.168.11", 24)
                  )
            )
            .build();


            MsgClient app = new MsgClient(
                  test, 
                  new IPv4("192.168.1.1", 24)
                  );
            app.start();
      }
}
