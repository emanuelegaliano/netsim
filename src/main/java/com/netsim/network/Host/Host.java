package com.netsim.network.Host;

import com.netsim.addresses.IPv4;
import com.netsim.app.App;
import com.netsim.network.Interface;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.IPv4.IPv4Protocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;

public class Host extends NetworkNode {
      private final HostStrategy strategy;
      private App runningApp;

      public Host(String name, RoutingTable rt, ArpTable arp) {
            super(name, rt, arp);
            this.strategy = new HostStrategy();
            this.runningApp = null;
      }

      public void setApp(App newApp) {
            this.runningApp = newApp;
      }

      public void runApp() {
            if(this.runningApp == null)
                  throw new IllegalArgumentException("Host: no App setted");

            this.runningApp.start();
      }

      public  void send(RoutingInfo route, ProtocolPipeline routingProtocols, byte[] data) 
      throws IllegalArgumentException {
            if(route == null || routingProtocols == null || data == null || data.length == 0)
                  throw new IllegalArgumentException("Host: invalid  arguments");

            this.strategy.forward(this, route, routingProtocols, data);
      }     

      public void receive(ProtocolPipeline routingProtocols, byte[] data) throws IllegalArgumentException {
            if(routingProtocols == null || data == null || data.length == 0)
                  throw new IllegalArgumentException("Host: invalid arguments");

            IPv4Protocol networkProtocol = routingProtocols.getProtocolByClass(IPv4Protocol.class);
            IPv4 destinationIP = networkProtocol.extractDestination(data);
            boolean hostIP = false;
            for(Interface iFace : this.interfaces) {
                  if(destinationIP.equals(iFace.getIP())) {
                        hostIP = true;
                        break;
                  }
            }            

            if(hostIP) {
                  this.runningApp.receive(routingProtocols.decapsulate(data));
            } else {
                  throw new RuntimeException("Host: wrong destination");
            }
      }
}
