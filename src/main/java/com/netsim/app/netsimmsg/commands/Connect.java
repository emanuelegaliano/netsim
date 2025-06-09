package com.netsim.app.netsimmsg.commands;

import com.netsim.app.Command;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.networkstack.ProtocolPipelineBuilder;
import com.netsim.node.NetworkNode;
import com.netsim.protocols.IPv4.IPv4Protocol;
import com.netsim.protocols.MSG.MSGProtocol;
import com.netsim.protocols.SimpleDLL.SimpleDLLProtocol;
import com.netsim.protocols.UDP.UDPProtocol;
import com.netsim.table.RoutingInfo;
import com.netsim.addresses.IP;
import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.addresses.Port;


public class Connect implements Command {
      private IP destination;

      /**
       * parsing arguments into internal attributes
       * @param args a arguments sequence like the one shown in help method
       * @throws IllegalArgumentException from IP constructor
       */
      private void parseArgs(String[] args) throws IllegalArgumentException {
            // if IPv6 implemented should be modified
            this.destination = new IPv4(args[2], 0);
      }

      /**
       * @throws IllegalArgumentException from parseArgs
       */
      public void execute(NetworkNode node, String[] args) throws IllegalArgumentException {
            this.parseArgs(args);

            try {
                  MSGProtocol msg = new MSGProtocol("");
                  
                  int srcPort;
                  do {
                        srcPort = node.randomPort();
                  } while(srcPort == MSGProtocol.port);
                  UDPProtocol udp = new UDPProtocol(
                        node.getMTU()-28,  // MSS = MTU - (IPv4 + UDP)
                        new Port(Integer.toString(srcPort)),
                        new Port(Integer.toString(MSGProtocol.port))
                  );

                  RoutingInfo route = node.getRoute(this.destination);

                  // check if nodes use other versions of IP rather then IPv4
                  IP srcIP = node.getIP();
                  IP dstIP = route.getNextHop();
                  if(!(srcIP instanceof IPv4) || !(dstIP instanceof IPv4))
                        throw new RuntimeException("Connect: only IP version implemented is IPv4");

                  IPv4Protocol ipv4 = new IPv4Protocol(
                        (IPv4) srcIP,
                        (IPv4) this.destination,
                        srcPort,
                        MSGProtocol.port,
                        0,
                        0,
                        0,
                        MSGProtocol.port,
                        node.getMTU()
                  );

                  SimpleDLLProtocol dll = new SimpleDLLProtocol(
                        route.getDevice().getMacAddress(), 
                        Mac.broadcast()
                  );

                  ProtocolPipeline protocols = new ProtocolPipelineBuilder().addProtocol(msg)
                                                                            .addProtocol(udp)
                                                                            .addProtocol(ipv4)
                                                                            .addProtocol(dll)
                                                                            .build();
                  
                  String connectMsg = "connect?";   
                  byte[] data = protocols.encapsulate(connectMsg.getBytes());
                  
                  

            } catch(final IllegalArgumentException e) {

            } catch(final RuntimeException e) {

            }

      }

      public String help() {
            return "connect <server IP>: connects to a NetsimMsg server";
      }
}
