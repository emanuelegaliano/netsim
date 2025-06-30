package com.netsim.network.router;

import java.util.List;

import com.netsim.addresses.IPv4;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.IPv4.IPv4Protocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;
import com.netsim.utils.Logger;

public class Router extends NetworkNode {
      public Router(String name, RoutingTable routingTable, ArpTable arpTable, List<Interface> interfaces)
      throws IllegalArgumentException {
            super(name, routingTable, arpTable, interfaces);
      }

      public void send(IPv4 destination, ProtocolPipeline stack, byte[] data) throws IllegalArgumentException {
            if(destination == null || stack == null || data == null || data.length == 0)
                  throw new IllegalArgumentException("Router: invalid arguments");

            Logger logger = Logger.getInstance();
            RoutingInfo route;
            try {
                  // look up next-hop info
                  route = this.getRoute(destination);
            } catch(final RuntimeException e) {
                  logger.error("Router does not know destination");
                  logger.debug(e.getLocalizedMessage());
                  return; // drop it
            }

            NetworkAdapter outAdapter = route.getDevice();
            outAdapter.send(stack, data);
      }

      public void receive(ProtocolPipeline stack, byte[] packets) throws IllegalArgumentException {
            if(stack == null || packets == null || packets.length == 0)
                  throw new IllegalArgumentException("Router: invalid arguments");

            Protocol p = stack.pop();
            if(!(p instanceof IPv4Protocol))
                  throw new RuntimeException("Router: expected ipv4 protocol");

            IPv4Protocol ipProtocol = (IPv4Protocol) p;
            IPv4 dest = ipProtocol.extractDestination(packets);
            byte[] payload = ipProtocol.decapsulate(packets);

            int oldTTL = ipProtocol.getTtl();
            if(oldTTL == 0)
                  return;

            IPv4Protocol newIp = new IPv4Protocol(
                  ipProtocol.getSource(),
                  dest,
                  ipProtocol.getIHL(),
                  ipProtocol.getTypeOfService(),
                  ipProtocol.getIdentification(),
                  ipProtocol.getFlags(),
                  oldTTL-1,
                  ipProtocol.getProtocol(),
                  ipProtocol.getMTU()
            );

            byte[] encapsulated = newIp.encapsulate(payload);

            stack.push(newIp);
            this.send(dest, stack, encapsulated);
      }
}
