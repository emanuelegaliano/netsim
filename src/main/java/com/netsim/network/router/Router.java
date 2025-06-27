package com.netsim.network.router;

import java.util.List;

import com.netsim.addresses.IPv4;
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
 * A simple point-to-point IPv4 router.
 * <p>
 * On receive: strips DLL then IP headers, decrements TTL,
 * then forwards using the same pipeline and payload.
 * </p>
 */
public class Router extends NetworkNode {
      /**
       * Constructs a Router with the given tables and interfaces.
       *
       * @param name          non-null node name
       * @param routingTable  non-null routing table
       * @param arpTable      non-null ARP table (unused here)
       * @param interfaces    non-null list of interfaces
       * @throws IllegalArgumentException if any argument is null
       */
      public Router(String name, RoutingTable routingTable, ArpTable arpTable, List<Interface> interfaces) 
      throws IllegalArgumentException {
            super(name, routingTable, arpTable, interfaces);
      }

      /**
       * Sends data by framing it with DLL and handing off to the next hop.
       *
       * @param route   non-null routing info
       * @param stack   non-null protocol pipeline
       * @param data    non-null, non-empty payload
       * @throws IllegalArgumentException if any argument is invalid
       */
      @Override
      public void send(RoutingInfo route, ProtocolPipeline stack, byte[] data) 
      throws IllegalArgumentException {
            if(route==null
            || stack==null
            || data==null
            || data.length==0)
                  throw new IllegalArgumentException("Router: invalid arguments");

            NetworkAdapter outAdapter = route.getDevice();
            Interface     localIface  = this.getInterface(outAdapter);
            Mac           srcMac      = localIface.getAdapter().getMacAddress();
            Mac           dstMac      = outAdapter.getLinkedAdapter().getMacAddress();

            outAdapter.collectFrames(data);
            SimpleDLLProtocol dll = new SimpleDLLProtocol(srcMac, dstMac);
            outAdapter.sendFrames(dll, stack);
      }

      /**
       * Receives a framed packet, strips DLL and IP, decrements TTL,
       * then forwards via send().
       *
       * @param stack  non-null protocol pipeline
       * @param frame  non-null, non-empty raw frame
       * @throws IllegalArgumentException if any argument is invalid
       */
      @Override
      public void receive(ProtocolPipeline stack, byte[] frame) 
      throws IllegalArgumentException {
            if(stack==null
            || frame==null
            || frame.length==0)
                  throw new IllegalArgumentException("Router: invalid arguments");

            // strip DLL
            Protocol rawDll = stack.pop();
            if(!(rawDll instanceof SimpleDLLProtocol))
                  throw new IllegalArgumentException("Router: expected DLL protocol");
            byte[] afterDll = ((SimpleDLLProtocol)rawDll).decapsulate(frame);

            // strip IP
            Protocol rawIp = stack.pop();
            if(!(rawIp instanceof IPv4Protocol))
                  throw new IllegalArgumentException("Router: expected IPv4Protocol");
            IPv4Protocol ipProto = (IPv4Protocol)rawIp;

            IPv4   dest    = ipProto.extractDestination(afterDll);
            byte[] payload = ipProto.decapsulate(afterDll);

            // decrement TTL
            int oldTtl = ipProto.getTtl();
            if(oldTtl<=1) return;

            // rebuild IP header with TTL-1 and push back onto stack
            IPv4Protocol newIp = new IPv4Protocol(
                  ipProto.getSourceIP(),
                  dest,
                  ipProto.getIHL(),
                  ipProto.getTypeOfService(),
                  ipProto.getIdentification(),
                  ipProto.getFlags(),
                  oldTtl-1,
                  ipProto.getProtocol(),
                  ipProto.getMTU()
            );
            stack.push(newIp);

            // lookup next hop
            RoutingInfo nextRoute;
            try {
                  nextRoute = this.routingTable.lookup(dest);
            } catch(NullPointerException e) {
                  return;
            }

            // forward
            this.send(nextRoute, stack, payload);
      }
}