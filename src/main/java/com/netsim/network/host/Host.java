package com.netsim.network.host;

import java.util.List;

import com.netsim.addresses.IPv4;
import com.netsim.app.App;
import com.netsim.network.Interface;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.IPv4.IPv4Protocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;
import com.netsim.utils.Logger;

/**
 * A Host node that runs a single App, and sends/receives
 * packets over a point-to-point DLL link.
 */
public class Host extends NetworkNode {
      private App runningApp;

      /**
       * Constructs a Host with the given name, routing and ARP tables,
       * and network interfaces.
       *
       * @param name          non-null host name
       * @param routingTable  non-null routing table
       * @param arpTable      non-null ARP table
       * @param interfaces    non-null list of interfaces
       * @throws IllegalArgumentException if any argument is null
       */
      public Host(String name, RoutingTable routingTable, ArpTable arpTable,List<Interface> interfaces) 
      throws IllegalArgumentException {
            super(name, routingTable, arpTable, interfaces);
            this.runningApp = null;
      }

      /**
       * Sets the application to run on this Host.
       *
       * @param newApp non-null App instance
       * @throws IllegalArgumentException if newApp is null
       */
      public void setApp(App newApp) {
            if(newApp == null) 
                  throw new IllegalArgumentException("Host: app cannot be null");
            this.runningApp = newApp;
      }

      /**
       * Starts the application assigned to this Host.
       *
       * @throws IllegalArgumentException if no App has been set
       */
      public void runApp() throws IllegalArgumentException {
            if(this.runningApp == null) 
                  throw new IllegalArgumentException("Host: no App set");
            this.runningApp.start();
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

      /**
       * Sends application data over the network stack:
       * encapsulate in IP→DLL, frame it, and rely on adapter to deliver.
       *
       * @param destination non-null destination
       * @param stack    non-null protocol pipeline (IP then above)
       * @param data     non-null, non-empty application payload
       * @throws IllegalArgumentException if any argument is invalid
       */
      public void send(IPv4 destination, ProtocolPipeline stack, byte[] data) {
            if(destination == null || stack == null || data == null || data.length == 0)
                  throw new IllegalArgumentException("Host: invalid arguments");

            RoutingInfo route;
            try {
                  route = this.getRoute(destination);
            } catch(final RuntimeException e) {
                  Logger logger = Logger.getInstance();
                  logger.error("Invalid IP");
                  logger.debug(e.getLocalizedMessage());
                  return;
            }

            IPv4Protocol ipProto = new IPv4Protocol(
                  this.getInterface(route.getDevice()).getIP(), 
                  destination, 
                  5,
                  0,
                  0,
                  0,
                  64, // not implemented yet, maybe in future
                  0,
                  this.getMTU()
            );

            byte[] encapsulated = ipProto.encapsulate(data);
            stack.push(ipProto);
            route.getDevice().send(stack, encapsulated);
      }

      /**
       * Receives a raw DLL frame, strips DLL and IP headers,
       * then hands the payload to the application.
       *
       * @param stack   non-null protocol pipeline (IP then above)
       * @param frame   non-null, non-empty frame bytes
       * @throws IllegalArgumentException if any argument is invalid
       * @throws RuntimeException         if no App has been set
       */
      public void receive(ProtocolPipeline stack, byte[] packets) {
            if(stack == null || packets == null ||  packets.length == 0)
                  throw new IllegalArgumentException("Host: invalid arguments");

            if(this.runningApp == null)
                  throw new RuntimeException("Host: no application set");

            Protocol p = stack.pop();
            if(!(p instanceof IPv4Protocol))
                  throw new RuntimeException("Host: expected IPv4 protocol");

            IPv4Protocol ipProtocol = (IPv4Protocol) p;
            IPv4 destination = ipProtocol.extractDestination(packets);

            // checking if an interface with destination IP exist
            if(!this.isForMe(destination)) {
                  Logger logger = Logger.getInstance();
                  logger.error("Packet not for Host " + this.name);
                  return;
            }

            byte[] transport = ipProtocol.decapsulate(packets);
            this.runningApp.receive(stack, transport);
      }
}