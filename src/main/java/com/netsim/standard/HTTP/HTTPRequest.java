package com.netsim.standard.HTTP;

import com.netsim.networkstack.ApplicationPDU;
import java.nio.charset.StandardCharsets;

/**
 * Simplified HTTPRequest 1/0
 */
public class HTTPRequest extends ApplicationPDU {
    private final HTTPMethods method;
    private final String path;
    private final String host;
    private final String header;

    /**
     * @param method  form HTTPMethods
     * @param path e.g. "/index.html"
     * @param host e.g. "www.example.com"
     * @param content body of the request
     */
    public HTTPRequest(HTTPMethods method, String path, String host, String content) {
        super(content);
        this.method = method;
        this.path = path;
        this.host = host;
        this.header = getHeader();
    }

    /**
     * @return header of the request
     */
    protected String getHeader() {
        StringBuilder sb = new StringBuilder();
        // Request‐line
        sb.append(method.name())
          .append(' ')
          .append(path)
          .append(" HTTP/1.0")
          .append("\r\n");
        // Host
        sb.append("Host: ")
          .append(host)
          .append("\r\n");
        // if method is post lenght is needed
        if(method == HTTPMethods.POST) {
            int len = content.getBytes(StandardCharsets.US_ASCII).length;
            sb.append("Content-Length: ")
              .append(len)
              .append("\r\n");
        }
        // End of header
        sb.append("\r\n");
        return sb.toString();
    }

    /**
     * Combine header and body in a single raw byte array
     * @return byte array of header and body combined
     */
    @Override
    public byte[] toByte() {
        byte[] headerBytes = header.getBytes(StandardCharsets.US_ASCII);
        byte[] bodyBytes = content.getBytes(StandardCharsets.US_ASCII);
        byte[] all = new byte[headerBytes.length + bodyBytes.length];
        System.arraycopy(headerBytes, 0, all, 0, headerBytes.length);
        System.arraycopy(bodyBytes, 0, all, headerBytes.length, bodyBytes.length);
        return all;
    }
}
