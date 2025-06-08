// client/Main.java

package com.netsim.client;

import java.nio.charset.StandardCharsets;

import com.netsim.addresses.Port;
import com.netsim.networkstack.NetworkAdapter;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.networkstack.ProtocolPipelineBuilder;
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

        ProtocolPipeline pipeline = new ProtocolPipelineBuilder().addProtocol(http)
                                                                 .addProtocol(udp)
                                                                 .addProtocol(ipv4)
                                                                 .addProtocol(dll)
                                                                 .build();



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

        String message = "Ciao, mi chiamo Manu";
        byte[] appPayload = message.getBytes(StandardCharsets.US_ASCII);
        System.out.println("Original payload length: " + appPayload.length + " bytes");

        byte[] wire = pipeline.encapsulate(appPayload);
        System.out.println("Wire-format length: " + wire.length + " bytes");

        sender.collectFrames(wire);
        sender.sendFrames(receiver);
        byte[] received = receiver.releaseFrames();

        byte[] rawHttp = pipeline.decapsulate(received);
        String httpText = new String(rawHttp, StandardCharsets.US_ASCII);
        System.out.println("\nReassembled HTTP request:\n" + httpText);
    }
}
