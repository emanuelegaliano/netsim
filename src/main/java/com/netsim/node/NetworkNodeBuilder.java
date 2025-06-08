package com.netsim.node;

import com.netsim.addresses.IP;
import com.netsim.networkstack.NetworkAdapter;

public interface NetworkNodeBuilder<Node extends NetworkNode> {
      NetworkNodeBuilder<Node> setName(String name);
      NetworkNodeBuilder<Node> addAdapter(NetworkAdapter adapter);
      NetworkNodeBuilder<Node> addIP(IP ip);
      NetworkNodeBuilder<Node> addDestination(IP subnet, String adapterName, IP nextHop);
      Node build();
}