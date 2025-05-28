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
        HTTP http = new HTTP(HTTPMethods.POST, "/submit", "api.example.com");
        UDP  udp  = new UDP(20, new Port("4000"), new Port("8080"));

        http.setNext(udp);
        udp.setPrevious(http);

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

        String message = "This is a longer message that will be segmented by UDP.";
        byte[] appPayload = message.getBytes(StandardCharsets.US_ASCII);
        System.out.println("Original payload length: " + appPayload.length + " bytes");

        byte[] wire = http.encapsulate(appPayload);
        System.out.println("Wire-format length: " + wire.length + " bytes");

        byte[] rawHttp = udp.decapsulate(wire);
        String httpText = new String(rawHttp, StandardCharsets.US_ASCII);
        System.out.println("\nReassembled HTTP request:\n" + httpText);
    }
}
