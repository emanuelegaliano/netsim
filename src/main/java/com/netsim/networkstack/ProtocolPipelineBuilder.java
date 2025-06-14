package com.netsim.networkstack;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a chain using this pattern: IdentityProtcol <-> ... protocols ... <-> IdentityProtocol
 */
public class ProtocolPipelineBuilder {
      private List<Protocol> protocols;

      public ProtocolPipelineBuilder() {
            this.protocols = new ArrayList<>();
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

            if(this.protocols.isEmpty()) {
                  this.protocols.add(newProtocol);
            } else {
                  Protocol lastProtocol = this.protocols.get(this.protocols.size()-1);
                  lastProtocol.setNext(newProtocol);
                  newProtocol.setPrevious(lastProtocol);
                  this.protocols.add(newProtocol);
            }

            return this;
      }

      /**
       * In order to use builder design pattern returns the pipeline
       * @return a new ProtocolPipeline with internal list of protocols
       */
      public ProtocolPipeline build() {
            return new ProtocolPipeline(new ArrayList<>(this.protocols));
      }
}
