package com.netsim.networkstack.PDUs;

import com.netsim.addresses.Address;

import java.nio.charset.StandardCharsets;

public class ApplicationPDU extends PDU {
    private final String header;
    private final String content;

    /**
     * @param src the source address
     * @param dst the destination address
     * @param header the application-level header (e.g., HTTP GET line)
     * @param content the body or message payload
     */
    public ApplicationPDU(Address src, Address dst, String header, String content) {
        super(src, dst); 
        this.header = header;
        this.content = content;
    }

    public String getHeader() {
        return this.header;
    }

    public String getContent() {
        return this.content;
    }

    @Override
    public byte[] toByte() {
        return (header + "\r\n\r\n" + content).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "ApplicationPDU[src=" + source +
               ", dst=" + destination +
               ", header=" + header +
               ", content=" + content + "]";
    }
}
