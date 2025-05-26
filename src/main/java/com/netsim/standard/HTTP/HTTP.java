package com.netsim.standard.HTTP;

import com.netsim.networkstack.Protocol;

/**
 * Simplified version HTTP 1/0
 */
public class HTTP implements Protocol<String, HTTPRequest> {
    private final HTTPMethods method;
    private final String path;
    private final String host;

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
    }

    /**
     * Encapsulates a plain String into an HTTPRequest using
     * the configured method, path, and host.
     *
     * @param content the request body (for GET can be empty or ignored)
     * @return an HTTPRequest ready to be sent downstream
     */
    public HTTPRequest encapsulate(String pdu) throws IllegalArgumentException {
        if(pdu == null) 
            throw new IllegalArgumentException("HTTPRequest decapsulation is null");
        
        return new HTTPRequest(method, path, host, pdu);
    }

    /**
     * Decapsulates an HTTPRequest back into the original String body.
     *
     * @param request the HTTPRequest to decode
     * @return the raw String content of the request
     * @throws IllegalArgumentException if request is null
     */
    public String decapsulate(HTTPRequest pdu) throws IllegalArgumentException {
        if(pdu == null) 
            throw new IllegalArgumentException("HTTPRequest decapsulation is null");
        
        return pdu.getContent();
    }
}
