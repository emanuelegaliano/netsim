package com.netsim.networkstack;

import java.util.List;

public interface Protocol {
    public List<PDU> encapsulate(PDU payload);
    public PDU decapsulate(PDU payload);
}
