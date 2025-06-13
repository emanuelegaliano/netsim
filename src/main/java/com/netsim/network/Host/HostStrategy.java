package com.netsim.network.Host;

import com.netsim.network.ForwardingStrategy;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.Node;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.table.RoutingInfo;

public class HostStrategy implements ForwardingStrategy {
      public void forward(Node node, RoutingInfo route, ProtocolPipeline routingProtocols, byte[] data) 
      throws IllegalArgumentException {
            if(node == null || route == null || routingProtocols == null || data == null || data.length == 0)
                  throw new IllegalArgumentException("HostStrategy: 9invalid arguments");

            NetworkAdapter outAdapter = route.getDevice();
            NetworkAdapter remoteAdapter = outAdapter.getLinkedAdapter();
            Node remoteNode = remoteAdapter.getNode();

            Protocol framingProtocol = routingProtocols.getProtocolAt(routingProtocols.size());

            outAdapter.collectFrames(framingProtocol, data);
            outAdapter.sendFrames();
            remoteNode.receive(routingProtocols, remoteAdapter.releaseFrames(framingProtocol));           
      }
}
