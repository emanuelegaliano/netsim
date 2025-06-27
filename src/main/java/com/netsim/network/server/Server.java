package com.netsim.network.server;

import java.util.List;

import com.netsim.app.App;
import com.netsim.addresses.Mac;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.SimpleDLL.SimpleDLLProtocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;

/**
 * A Server node that wraps raw application data through the network
 * stack and delivers it point-to-point over a Simple DLL link.
 *
 * @param <AppType> the application type running on this server
 */
public class Server<AppType extends App> extends NetworkNode {
      private final AppType app;

      /**
       * @param name          non-null node name
       * @param routingTable  non-null routing table
       * @param arpTable      non-null ARP table (unused in point-to-point)
       * @param interfaces    non-null list of interfaces
       * @param app           non-null application instance
       * @throws IllegalArgumentException if any argument is invalid
       */
      public Server(String name,RoutingTable routingTable,ArpTable arpTable,List<Interface> interfaces,AppType app) 
      throws IllegalArgumentException {
            super(name,routingTable,arpTable,interfaces);
            if(app==null) 
                  throw new IllegalArgumentException("Server: app cannot be null");
            this.app = app;
      }

      /**
       * Encapsulate the application payload through IP, then DLL,
       * send via the adapter, and let the linked adapter deliver to peer.
       *
       * @param route non-null next-hop routing info
       * @param stack non-null pipeline (IP and above)
       * @param data  non-null, non-empty application payload
       * @throws IllegalArgumentException if any argument is invalid
       */
      public void send(RoutingInfo route,ProtocolPipeline stack,byte[] data) 
      throws IllegalArgumentException {
            if(route==null||stack==null||data==null||data.length==0)
                  throw new IllegalArgumentException("Server: invalid arguments");

            // 1) find outbound adapter and its interface for MACs
            NetworkAdapter outAdapter = route.getDevice();
            Interface localIface = this.getInterface(outAdapter);
            Mac srcMac = localIface.getAdapter().getMacAddress();
            Mac dstMac = outAdapter.getLinkedAdapter().getMacAddress();

            // 2) buffer the IP datagram for framing
            outAdapter.collectFrames(data);

            // 3) frame and send via DLL
            SimpleDLLProtocol dll = new SimpleDLLProtocol(srcMac,dstMac);
            outAdapter.sendFrames(dll,stack);
      }

      /**
       * Receives a framed network packet from the adapter.
       * Decapsulates IP and higher layers in one shot and
       * delivers the resulting payload to the application.
       *
       * @param stack   non-null pipeline (IP and above)
       * @param payload non-null, non-empty payload (post-DLL strip)
       * @throws IllegalArgumentException if any argument is invalid
       */
      public void receive(ProtocolPipeline stack,byte[] payload) {
            if(stack==null||payload==null||payload.length==0)
                  throw new IllegalArgumentException("Server: invalid arguments");

            // decapsulate IP and upper layers in one go
            byte[] appData = stack.decapsulate(payload);

            // hand off to the application
            this.app.receive(appData);
      }
}