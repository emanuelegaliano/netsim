// client/Main.java

package com.netsim.client;

import java.nio.charset.StandardCharsets;

import com.netsim.addresses.Port;
import com.netsim.networkstack.IdentityProtocol;
import com.netsim.standard.HTTP.HTTP;
import com.netsim.standard.HTTP.HTTPMethods;
import com.netsim.standard.IPv4.IPv4Protocol;
import com.netsim.standard.SimpleDLL.SimpleDLLProtocol;
import com.netsim.standard.UDP.UDP;
import com.netsim.addresses.*;

public class Main {
    public static void main(String[] args) {
        HTTP http = new HTTP(HTTPMethods.POST, "/submit", "api.example.com");
        UDP udp = new UDP(20, new Port("4000"), new Port("8080"));
        IPv4Protocol ipv4 = new IPv4Protocol(
            new IPv4("192.168.1.1", 24), 
            new IPv4("192.170.2.1", 24), 
            5, 
            0, 
            0, 
            0, 
            64,
            17, 
            1500);
        SimpleDLLProtocol dll = new SimpleDLLProtocol(
            new Mac("AA:BB:CC:DD:FF:EE"), 
            new Mac("AA:BB:CC:EE:FF:DD")
        );
        IdentityProtocol identity = new IdentityProtocol();

        http.setNext(udp);
        udp.setNext(ipv4);
        ipv4.setNext(dll);
        dll.setNext(identity);

        dll.setPrevious(ipv4);
        ipv4.setPrevious(udp);
        udp.setPrevious(http);
        http.setPrevious(identity);

        String message = "Ciao, mi chiamo Manu";
        byte[] appPayload = message.getBytes(StandardCharsets.US_ASCII);
        System.out.println("Original payload length: " + appPayload.length + " bytes");

        byte[] wire = http.encapsulate(appPayload);
        System.out.println("Wire-format length: " + wire.length + " bytes");

        byte[] rawHttp = dll.decapsulate(wire);
        String httpText = new String(rawHttp, StandardCharsets.US_ASCII);
        System.out.println("\nReassembled HTTP request:\n" + httpText);
    }
}
