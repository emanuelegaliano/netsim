package com.netsim.network.server;

import java.util.List;

import com.netsim.addresses.IPv4;
import com.netsim.app.App;
import com.netsim.network.Interface;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.IPv4.IPv4Protocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingTable;
import com.netsim.utils.Logger;

public class Server<AppType extends App> extends NetworkNode {
      private final AppType app;

      public Server(String name, RoutingTable routingTable, ArpTable arpTable, List<Interface> interfaces, AppType app) 
      throws IllegalArgumentException {
            super(name, routingTable, arpTable, interfaces);
            if(app == null)
                  throw new IllegalArgumentException("Server: app is null");

            this.app = app;
            this.app.start();
      }


      public boolean isForMe(IPv4 destination) {
            try {
                  this.getInterface(destination);
                  return true;
            } catch(final RuntimeException e) {
                  Logger.getInstance().debug(e.getLocalizedMessage());
                  return false;
            }
      }

      public void send(IPv4 destination, ProtocolPipeline protocols, byte[] data) {

      }
      public void receive(ProtocolPipeline stack, byte[] packets) {
            if(stack == null || packets == null ||  packets.length == 0)
                  throw new IllegalArgumentException("Server: invalid arguments");

            if(this.app == null)
                  throw new RuntimeException("Server: no application set");

            Protocol p = stack.pop();
            if(!(p instanceof IPv4Protocol))
                  throw new RuntimeException("Server: expected IPv4 protocol");

            IPv4Protocol ipProtocol = (IPv4Protocol) p;
            IPv4 destination = ipProtocol.extractDestination(packets);

            // checking if an interface with destination IP exist
            if(!this.isForMe(destination)) {
                  Logger logger = Logger.getInstance();
                  logger.error("Packet not for Server " + this.name);
                  return; // drop
            }

            byte[] transport = ipProtocol.decapsulate(packets);
            this.app.receive(stack, transport);
      }

}
