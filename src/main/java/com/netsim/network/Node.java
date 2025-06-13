package com.netsim.network;

import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.table.RoutingInfo;

public interface Node {
      public void send(RoutingInfo route, ProtocolPipeline protocols, byte[] data);
      public void receive(ProtocolPipeline protocols, byte[] data);
}
