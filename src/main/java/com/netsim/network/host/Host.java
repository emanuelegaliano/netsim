package com.netsim.network.host;

import java.util.List;

import com.netsim.addresses.IPv4;
import com.netsim.app.App;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.NetworkNode;
import com.netsim.network.Node;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.IPv4.IPv4Protocol;
import com.netsim.protocols.SimpleDLL.SimpleDLLProtocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;

public class Host extends NetworkNode {
      private App runningApp;

      public Host(String name, RoutingTable routingTable, ArpTable arpTable, List<Interface> interfaces)
      throws IllegalArgumentException {
            super(name, routingTable, arpTable, interfaces);
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

      public void send(RoutingInfo route, ProtocolPipeline routingProtocols, byte[] data) 
      throws IllegalArgumentException {
            if(route == null || routingProtocols == null || data == null || data.length == 0)
                  throw new IllegalArgumentException("Host: invalid  arguments");

            NetworkAdapter outAdapter = route.getDevice();
            NetworkAdapter remoteAdapter = outAdapter.getLinkedAdapter();
            Node remoteNode = remoteAdapter.getNode();
            
            Protocol framingProtocol = routingProtocols.getProtocolByClass(SimpleDLLProtocol.class);

            outAdapter.collectFrames(framingProtocol, data);
            outAdapter.sendFrames(framingProtocol);
            remoteNode.receive(routingProtocols, remoteAdapter.releaseFrames(framingProtocol));           
      }     

      public void receive(ProtocolPipeline routingProtocols, byte[] data) 
      throws IllegalArgumentException, RuntimeException {
            if(routingProtocols == null || data == null || data.length == 0)
                  throw new IllegalArgumentException("Host: invalid arguments");
            
            IPv4 destinationIP = (IPv4) routingProtocols.extractDestinationFrom(IPv4Protocol.class, data);
            boolean hostIP = false;
            for(Interface iFace : this.interfaces) {
                  if(destinationIP.equals(iFace.getIP())) {
                        hostIP = true;
                        break;
                  }
            }            

            if(hostIP) {
                  if(this.runningApp == null)
                        throw new RuntimeException("Host: no application setted");

                  IPv4 source = (IPv4) routingProtocols.extractSourceFrom(IPv4Protocol.class, data);
                  this.runningApp.receive(source, routingProtocols.decapsulate(data));
            }
      }
}
