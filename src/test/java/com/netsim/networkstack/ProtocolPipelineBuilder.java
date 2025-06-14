package com.netsim.networkstack;

import java.util.ArrayList;
import java.util.List;

public class ProtocolPipelineBuilder {
    private final List<Protocol> protocols = new ArrayList<>();

    /** Start with one identity at the bottom. */
    public ProtocolPipelineBuilder() {
        this.protocols.add(new IdentityProtocol());
    }

    /**
     * Append a new layer: wire prev→newProtocol and newProtocol→prev,
     * then add it to the list.
     */
    public ProtocolPipelineBuilder addProtocol(Protocol newProtocol) {
        if (newProtocol == null) {
            throw new IllegalArgumentException("ProtocolPipelineBuilder: protocol cannot be null");
        }
        Protocol last = this.protocols.get(this.protocols.size() - 1);
        last.setNext(newProtocol);
        newProtocol.setPrevious(last);
        this.protocols.add(newProtocol);
        return this;
    }

    /**
     * Finish the chain by tacking on a trailing identity, 
     * then hand off to ProtocolPipeline.
     */
    public ProtocolPipeline build() {
        Protocol last = this.protocols.get(this.protocols.size() - 1);
        IdentityProtocol tail = new IdentityProtocol();
        last.setNext(tail);
        tail.setPrevious(last);

        // Give Pipeline its own copy of the list:
        return new ProtocolPipeline(new ArrayList<>(this.protocols));
    }
}