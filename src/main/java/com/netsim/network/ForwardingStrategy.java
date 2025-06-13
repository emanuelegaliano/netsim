package com.netsim.network;

import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.table.RoutingInfo;

public interface ForwardingStrategy {
      void forward(Node node, RoutingInfo route, ProtocolPipeline routingProtocols, byte[] data);
}