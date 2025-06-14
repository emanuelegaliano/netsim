package com.netsim.network;

import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.table.RoutingInfo;

public interface Node {
      /**
       * Sends a block of data over the network. 
       * The data will be encapsulated by the supplied {@code protocols}
       * handed to the networrk adapter indicate by {@code route.getDevice()}
       * and forwarded toward the next hop given by {@code route.getNextHop()}.
       * @param route the routing information (adapter + nextHop address)
       * @param protocols the protocol pipeline to apply (e.g. UDP + IP + framing)
       * @param data the payload bytes to send (must be non-null and non-empty)
       */
      public void send(RoutingInfo route, ProtocolPipeline protocols, byte[] data);

      /**
       * Receives a bloc of raw bytes from the network and delivers it up the
       * protocol stack. The incoming {@code data} will be decapsulated by the suppled
       * {@code protocols} and then handed by an higher-layer handler 
       * (probably an {@link com.netsim.app.App App})
       * @param protocols the protocol pipeline to apply
       * @param data the war bytes received (must be non-null and non-empty)
       */
      public void receive(ProtocolPipeline protocols, byte[] data);
}
