// client/Main.java

package com.netsim.client;

import java.nio.charset.StandardCharsets;

import com.netsim.addresses.Port;
import com.netsim.networkstack.IdentityProtocol;
import com.netsim.networkstack.NetworkAdapter;
import com.netsim.protocols.HTTP.HTTP;
import com.netsim.protocols.HTTP.HTTPMethods;
import com.netsim.protocols.IPv4.IPv4Protocol;
import com.netsim.protocols.SimpleDLL.SimpleDLLProtocol;
import com.netsim.protocols.UDP.UDP;
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
            30);
        SimpleDLLProtocol dll = new SimpleDLLProtocol(
            new Mac("AA:BB:CC:DD:EE:11"), 
            new Mac("AA:BB:CC:DD:EE:22")
        );
        IdentityProtocol identity = new IdentityProtocol();

        NetworkAdapter sender = new NetworkAdapter(
            "A", 
            1500, 
            new Mac("AA:BB:CC:DD:EE:11")
        );

        NetworkAdapter receiver = new NetworkAdapter(
            "B", 
            1500, 
            new Mac("AA:BB:CC:DD:EE:22")
        );

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

        sender.collectFrames(wire);
        sender.sendFrames(receiver);
        byte[] received = receiver.releaseFrames();

        byte[] rawHttp = dll.decapsulate(received);
        String httpText = new String(rawHttp, StandardCharsets.US_ASCII);
        System.out.println("\nReassembled HTTP request:\n" + httpText);

        Integer a = 1;

        System.out.println(a.getClass().getSimpleName());
    }
}
