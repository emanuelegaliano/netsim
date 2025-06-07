package com.netsim.standard.HTTP;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.netsim.networkstack.Protocol;
import com.netsim.addresses.Address;

/**
 * Simplified version HTTP 1/0
 */
public class HTTP implements Protocol {
    private final HTTPMethods method;
    private final String path;
    private final String host;
    
    private Protocol nextProtocol;
    private Protocol previousProtocol;

    /**
     * @param method the HTTP method to use (GET, POST, etc.)
     * @param path the request path (e.g. "/index.html")
     * @param host the Host header value (e.g. "www.example.com")
     */
    public HTTP(HTTPMethods method, String path, String host) throws IllegalArgumentException {
        if(method == null || path == null || host == null)
            throw new IllegalArgumentException("Invalid HTTP connection arguments");
        
        this.method = method;
        this.path = path;
        this.host = host;

        this.nextProtocol = null;
        this.previousProtocol = null;
    }

    /**
     * Encapsulates a plain String into an HTTPRequest using
     * the configured method, path, and host.
     *
     * @param content the request body (for GET can be empty or ignored)
     * @return an HTTPRequest ready to be sent downstream
     * @throws IllegalArgumentException if pdu is null
     * @throws NullPointerException if next protocol of the chain is 
     * not defined (aka nextProtocolDefined = false)
     */
    public byte[] encapsulate(byte[] upperLayerPDU) throws IllegalArgumentException, NullPointerException {
        if(upperLayerPDU.length == 0 || upperLayerPDU == null) 
            throw new IllegalArgumentException("HTTPRequest: pdu is null or its length is 0");
            
        if(this.nextProtocol == null)
            throw new NullPointerException("HTTP: next protocol is not defined");


        HTTPRequest request = new HTTPRequest(this.method, this.path, this.host, upperLayerPDU);
        return this.nextProtocol.encapsulate(request.toByte());
    }

    /**
     * Decapsulates an HTTPRequest back into the original String body.
     *
     * @param request the HTTPRequest to decode
     * @return an instance of StringPayload
     * @throws IllegalArgumentException if request is null
     * @throws NullPointerException if previous protocol of the chain is 
     * not defined (aka previousProtocolDefined = false)
     */
    @Override
    public byte[] decapsulate(byte[] lowerLayerPDU) throws IllegalArgumentException, NullPointerException {
        if(lowerLayerPDU == null)
            throw new IllegalArgumentException("HTTPRequest decapsulation is null");
        if(this.previousProtocol == null)
            throw new NullPointerException("HTTP: previous protocol is not defined");

        String raw = new String(lowerLayerPDU, StandardCharsets.US_ASCII);
        int sep = raw.indexOf("\r\n\r\n");
        if (sep < 0)
            throw new IllegalArgumentException(
                "HTTP: malformed request (missing header/body separator)");
        
        // estrai il body vero e proprio
        byte[] body = Arrays.copyOfRange(lowerLayerPDU, sep + 4, lowerLayerPDU.length);

        // lo passi al livello superiore (o lo restituisci direttamente)
        return this.previousProtocol.decapsulate(body);
    }

    /**
     * @param nextProtocol the next protocol of the chain
     * @throws NullPointerException if prevProtocol is null
     */
    public void setNext(Protocol nextProtocol) throws NullPointerException {
        if(nextProtocol == null)
            throw new NullPointerException("HTTP: next protocol cannot be null");
        
        this.nextProtocol = nextProtocol;
    }

    /**
     * @param prevProtocol the previous protocol of the chain
     * @throws NullPointerException if prevProtocol is null
     */
    public void setPrevious(Protocol previousProtocol) throws NullPointerException {
        if(previousProtocol == null)
            throw new NullPointerException("HTTP: previous protocol cannot be null");
        
        this.previousProtocol = previousProtocol;
    }

    /**
     * In HTTP There's no source
     */
    public Address extractSource(byte[] pdu) {
        return null;
    }

    /**
     * In HTTP There's no destination
     */
    public Address extractDestination(byte[] pdu) {
        return null;
    }
}
