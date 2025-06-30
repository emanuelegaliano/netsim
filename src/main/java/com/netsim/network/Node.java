package com.netsim.network;

import com.netsim.addresses.IPv4;
import com.netsim.networkstack.ProtocolPipeline;

public interface Node {
      /**
       * @param destination IPv4 destination
       * @param protocols the protocol pipeline to apply (e.g. UDP + IP + framing)
       * @param data the payload bytes to send (must be non-null and non-empty)
       */
      public void send(IPv4 destination, ProtocolPipeline protocols, byte[] data);

      /**
       * Receives a bloc of raw bytes from the network and delivers it up the
       * protocol stack. The incoming {@code data} will be decapsulated by the suppled
       * {@code protocols} and then handed by an higher-layer handler 
       * (probably an {@link com.netsim.app.App App})
       * @param protocols the protocol pipeline to apply
       * @param data the war bytes received (must be non-null and non-empty)
       */
      public void receive(ProtocolPipeline protocols, byte[] data);
            
      /** @return name of the node */
      public String getName();
}
