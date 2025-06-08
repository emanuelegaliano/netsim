package com.netsim.networkstack;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a chain using this pattern: IdentityProtcol <-> ... protocols ... <-> IdentityProtocol
 */
public class ProtocolPipelineBuilder {
      private List<Protocol> protocols;

      /**
       * Add under the hood an identity protocol as lower layer
       */
      public ProtocolPipelineBuilder() {
            this.protocols = new ArrayList<>();
            this.protocols.add(new IdentityProtocol());
      }

      /**
       * add the protocol to the chain (internal list), it calls
       * lastProtocol.setNext (last protocol of the list) and 
       * newProtocol.setPrevious
       * @param newProtocol new layer of the chain
       * @return instance of ProtocolPipelineBuilder in order to use builder design pattern
       */
      public ProtocolPipelineBuilder addProtocol(Protocol newProtocol) {
            if(newProtocol == null)
                  throw new IllegalArgumentException("ProtocolPipelineBuilder: protocol cannot be null");

            Protocol lastProtocol = this.protocols.getLast();
            lastProtocol.setNext(newProtocol);
            newProtocol.setPrevious(lastProtocol);
            this.protocols.add(newProtocol);

            return this;
      }

      /**
       * In order to use builder design pattern returns the pipeline
       * @return a new ProtocolPipeline with internal list of protocols
       */
      public ProtocolPipeline build() {
            this.protocols.getLast().setNext(new IdentityProtocol());

            return new ProtocolPipeline(this.protocols);
      }
}
