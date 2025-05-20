package com.netsim.networkstack;

import java.io.Serializable;


public class Payload implements Serializable {
    private Object data;

    /**
     * @param data the data of the payload
     */
    public Payload(Object data) {
        this.data = data;
    }

    /**
     * 
     * @return Payload.data: real payload
     */
    public Object getData() {
        return this.data;
    }
}
