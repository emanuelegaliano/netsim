package com.netsim.networkstack.PDUs;

import com.netsim.addresses.Port;

public abstract class TransportPDU extends PDU {
    public TransportPDU(Port src, Port dst) {
        super(src, dst);
    }
}