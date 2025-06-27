package com.netsim.network.host;

import java.util.List;

import com.netsim.app.App;
import com.netsim.addresses.Mac;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.IPv4.IPv4Protocol;
import com.netsim.protocols.SimpleDLL.SimpleDLLProtocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;

/**
 * A Host node that runs a single App, and sends/receives
 * packets over a point-to-point DLL link.
 */
public class Host extends NetworkNode {
      private App runningApp;

      /**
       * Constructs a Host with the given name, routing and ARP tables,
       * and network interfaces.
       *
       * @param name          non-null host name
       * @param routingTable  non-null routing table
       * @param arpTable      non-null ARP table
       * @param interfaces    non-null list of interfaces
       * @throws IllegalArgumentException if any argument is null
       */
      public Host(String name, RoutingTable routingTable, ArpTable arpTable,List<Interface> interfaces) 
      throws IllegalArgumentException {
            super(name,routingTable,arpTable,interfaces);
            this.runningApp = null;
      }

      /**
       * Sets the application to run on this Host.
       *
       * @param newApp non-null App instance
       * @throws IllegalArgumentException if newApp is null
       */
      public void setApp(App newApp) {
            if(newApp==null) throw new IllegalArgumentException("Host: app cannot be null");
            this.runningApp = newApp;
      }

      /**
       * Starts the application assigned to this Host.
       *
       * @throws IllegalArgumentException if no App has been set
       */
      public void runApp() {
            if(this.runningApp==null) 
                  throw new IllegalArgumentException("Host: no App set");
            this.runningApp.start(this);
      }

      /**
       * Sends application data over the network stack:
       * encapsulate in IP→DLL, frame it, and rely on adapter to deliver.
       *
       * @param route    non-null next-hop routing info
       * @param stack    non-null protocol pipeline (IP then above)
       * @param data     non-null, non-empty application payload
       * @throws IllegalArgumentException if any argument is invalid
       */
      public void send(RoutingInfo route,ProtocolPipeline stack,byte[] data) {
            if(route==null||stack==null||data==null||data.length==0)
                  throw new IllegalArgumentException("Host: invalid arguments");

            // determine source/destination MACs
            NetworkAdapter out = route.getDevice();
            Interface iface = this.getInterface(out);
            Mac src = iface.getAdapter().getMacAddress();
            Mac dst   = out.getLinkedAdapter().getMacAddress();

            // buffer the IP datagram
            out.collectFrames(data);

            // frame and send via DLL
            SimpleDLLProtocol dll = new SimpleDLLProtocol(src,dst);
            out.sendFrames(dll,stack);
      }

      /**
       * Receives a raw DLL frame, strips DLL and IP headers,
       * then hands the payload to the application.
       *
       * @param stack   non-null protocol pipeline (IP then above)
       * @param frame   non-null, non-empty frame bytes
       * @throws IllegalArgumentException if any argument is invalid
       * @throws RuntimeException         if no App has been set
       */
      public void receive(ProtocolPipeline stack,byte[] frame) {
            if(stack==null||frame==null||frame.length==0)
                  throw new IllegalArgumentException("Host: invalid arguments");

            if(this.runningApp==null)
                  throw new RuntimeException("Host: no application set");

            // strip DLL layer
            Protocol rawDll = stack.pop();
            if(!(rawDll instanceof SimpleDLLProtocol))
                  throw new IllegalArgumentException("Host: expected DLL protocol");
            byte[] afterDll = ((SimpleDLLProtocol)rawDll).decapsulate(frame);

            // strip IP layer
            Protocol rawIp = stack.pop();
            if(!(rawIp instanceof IPv4Protocol))
                  throw new IllegalArgumentException("Host: expected IPv4Protocol");
            IPv4Protocol ip = (IPv4Protocol)rawIp;

            byte[] appData = ip.decapsulate(afterDll);

            // deliver to application
            this.runningApp.receive(appData);
      }
}