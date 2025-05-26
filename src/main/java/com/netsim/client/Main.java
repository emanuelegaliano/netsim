// client/Main.java

package com.netsim.client;

import java.util.List;

import com.netsim.standard.HTTP.HTTPMethods;
import com.netsim.standard.HTTP.HTTPRequest;
import com.netsim.standard.UDP.*;
import com.netsim.addresses.*;

public class Main {
    public static void main(String[] args) {
        UDP d = new UDP(2, new Port("80"), new Port("10"));
        List<UDPSegment> segment = d.encapsulate(
            new HTTPRequest(HTTPMethods.GET, "/path/example", "www.example.com", "/submit")
        );

        System.out.println((d.decapsulate(segment)).getContent());
    }
}
