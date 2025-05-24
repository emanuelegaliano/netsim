package com.netsim.networkstack;

import java.nio.charset.StandardCharsets;

public abstract class ApplicationPDU extends PDU {
    protected String header;
    protected final String content;

    /**
     * @param header the application-level header (e.g., HTTP GET line)
     * @param content the body or message payload
     */
    protected ApplicationPDU(String content) {
        super(null, null); 
        this.content = content;
    }

    /**
     * Method called in the constructor of the
     * class for getting the header of the protocol implemented.
     * In the case of ApplicationPDU: abstract 
     * @return the header 
     */
    protected abstract String getHeader();

    public String getContent() {
        return this.content;
    }

    public byte[] toByte() {
        return (header + "\r\n\r\n" + content).getBytes(StandardCharsets.UTF_8);
    }
}
