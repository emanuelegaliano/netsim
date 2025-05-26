package com.netsim.standard.HTTP;

import com.netsim.networkstack.PDU;
import java.nio.charset.StandardCharsets;

public class HTTPRequest extends PDU {
    private final HTTPMethods method;
    private final String path;
    private final String host;
    private final String header;
    private final String content;

    /**
     * @param method  form HTTPMethods
     * @param path e.g. "/index.html"
     * @param host e.g. "www.example.com"
     * @param content body of the request
     */
    public HTTPRequest(HTTPMethods method, String path, String host, String content) {
        super(null, null);
        this.content = content;
        this.method = method;
        this.path = path;
        this.host = host;
        this.header = getStringHeader();
    }

    public String getContent() {
        return this.content;
    }

    private String getStringHeader() {
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
     * @return header of the request
     */
    protected byte[] getHeader() {
        return this.header.getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Combine header and body in a single raw byte array
     * @return byte array of header and body combined
     */
    @Override
    public byte[] toByte() {
        byte[] headerBytes = getHeader();
        byte[] bodyBytes = content.getBytes(StandardCharsets.US_ASCII);
        byte[] result = new byte[headerBytes.length + bodyBytes.length];
        System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
        System.arraycopy(bodyBytes,   0, result, headerBytes.length, bodyBytes.length);
        return result;
    }
}
