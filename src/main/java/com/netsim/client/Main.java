// client/Main.java

package com.netsim.client;

import java.nio.charset.StandardCharsets;

import com.netsim.addresses.Port;
import com.netsim.networkstack.Protocol;
import com.netsim.standard.HTTP.HTTP;
import com.netsim.standard.HTTP.HTTPMethods;
import com.netsim.standard.UDP.UDP;

public class Main {
    public static void main(String[] args) {
        // 1) Create HTTP and UDP layers
        HTTP http = new HTTP(HTTPMethods.POST, "/submit", "api.example.com");
        UDP  udp  = new UDP(10, new Port("4000"), new Port("8080"));

        // 2) Chain outbound: HTTP → UDP
        http.setNext(udp);
        udp.setPrevious(http);

        // 3) Provide a “wire” identity layer so UDP and HTTP have a downstream
        Protocol identity = new Protocol() {
            @Override
            public byte[] encapsulate(byte[] pdu) {
                return pdu;
            }

            @Override
            public byte[] decapsulate(byte[] pdu) {
                return pdu;
            }

            @Override
            public void setNext(Protocol next) { }

            @Override
            public void setPrevious(Protocol prev) { }
        };
        udp.setNext(identity);
        http.setPrevious(identity);

        // 4) Application payload (long enough to fragment)
        String message = "This is a longer message that will be fragmented by UDP.";
        byte[] appPayload = message.getBytes(StandardCharsets.US_ASCII);
        System.out.println("Original payload length: " + appPayload.length + " bytes");

        // 5) Encapsulation: HTTP → UDP → identity
        byte[] wire = http.encapsulate(appPayload);
        System.out.println("Wire-format length: " + wire.length + " bytes");

        // 6) Decapsulation: identity → UDP → HTTP
        byte[] rawHttp = udp.decapsulate(wire);
        String httpText = new String(rawHttp, StandardCharsets.US_ASCII);
        System.out.println("\nReassembled HTTP request:\n" + httpText);

        // 7) Extract body
        int sep = httpText.indexOf("\r\n\r\n");
        String body = (sep >= 0)
            ? httpText.substring(sep + 4)
            : "";
        System.out.println("Extracted body: \"" + body + "\"");
    }
}
