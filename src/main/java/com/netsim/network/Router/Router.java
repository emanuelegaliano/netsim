package com.netsim.network.Router;

import java.util.List;

import com.netsim.addresses.IPv4;
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

public class Router extends NetworkNode {

      public Router(String name, RoutingTable routingTable, ArpTable arpTable, List<Interface> interfaces) 
      throws IllegalArgumentException {
            super(name, routingTable, arpTable, interfaces);
            
      }

      public void send(RoutingInfo route, ProtocolPipeline routingProtocols, byte[] data) 
      throws IllegalArgumentException {
            if(route == null || routingProtocols == null || data == null)
                  throw new IllegalArgumentException("Router: invalid arguments");

            Protocol framingProtocol = routingProtocols.getProtocolByClass(SimpleDLLProtocol.class);
            NetworkAdapter outAdapter = route.getDevice();
            Node remoteNode = outAdapter.getNode();

            outAdapter.collectFrames(framingProtocol, data);
            outAdapter.sendFrames(framingProtocol);
            remoteNode.receive(routingProtocols, outAdapter.releaseFrames(framingProtocol));
      }

      public void receive(ProtocolPipeline routingProtocols, byte[] data) 
      throws IllegalArgumentException, RuntimeException{
            if(routingProtocols == null || data == null || data.length == 0)
                  throw new IllegalArgumentException("Router: invalid arguments");
                  
            IPv4 dest = (IPv4) routingProtocols.extractDestinationFrom(IPv4Protocol.class, data);
            
            try {
                  RoutingInfo rInfo = this.routingTable.lookup(dest);
                  this.send(rInfo, routingProtocols, data);
            } catch(final NullPointerException e) {
                  // do nothing
            }
      }
}
