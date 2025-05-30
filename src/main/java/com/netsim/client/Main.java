// client/Main.java

package com.netsim.client;

import java.nio.charset.StandardCharsets;

import com.netsim.addresses.Port;
import com.netsim.networkstack.IdentityProtocol;
import com.netsim.standard.HTTP.HTTP;
import com.netsim.standard.HTTP.HTTPMethods;
import com.netsim.standard.UDP.UDP;

public class Main {
    public static void main(String[] args) {
        HTTP http = new HTTP(HTTPMethods.POST, "/submit", "api.example.com");
        UDP  udp  = new UDP(20, new Port("4000"), new Port("8080"));
        IdentityProtocol identity = new IdentityProtocol();

        http.setNext(udp);
        udp.setPrevious(http);

        udp.setNext(identity);
        http.setPrevious(identity);

        String message = "Suca Federico";
        byte[] appPayload = message.getBytes(StandardCharsets.US_ASCII);
        System.out.println("Original payload length: " + appPayload.length + " bytes");

        System.out.println("Printint message: ");
        for(int i = 0; i < appPayload.length; i++) {
            System.out.print(appPayload[i] + " ");
        }
        System.out.println();

        byte[] wire = http.encapsulate(appPayload);
        System.out.println("Wire-format length: " + wire.length + " bytes");

        System.out.println("Printing binary message: ");
        for(int i = 0; i < wire.length; i++) {
            System.out.printf("%02x ", wire[i]);
        }
        System.out.println();

        byte[] rawHttp = udp.decapsulate(wire);
        String httpText = new String(rawHttp, StandardCharsets.US_ASCII);
        System.out.println("\nReassembled HTTP request:\n" + httpText);
    }
}
