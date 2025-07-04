package com.netsim.app.msg;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.addresses.Port;
import com.netsim.network.Interface;
import com.netsim.network.CabledAdapter;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.UDP.UDPProtocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingTable;

import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class MsgServerTest {
      private MsgServer app;
      private DummyNode node;

      @Before
      public void setUp() {
            node = new DummyNode();
            app = new MsgServer(node);
      }

      @Test(expected = RuntimeException.class)
      public void testReceiveFailsOnMissingUDP() {
            ProtocolPipeline stack = new ProtocolPipeline();
            byte[] data = "ciao".getBytes(StandardCharsets.UTF_8);
            app.receive(stack, data); // missing UDP
      }

      @Test(expected = RuntimeException.class)
      public void testReceiveFailsOnMissingMSG() {
            ProtocolPipeline stack = new ProtocolPipeline();
            byte[] data = "ciao".getBytes(StandardCharsets.UTF_8);

            UDPProtocol udp = new UDPProtocol(64, new Port("1111"), new Port("2222"));
            byte[] encapsulated = udp.encapsulate(data);
            stack.push(udp);

            app.receive(stack, encapsulated); // missing MSG
      }

      @Test(expected = RuntimeException.class)
      public void testSendFailsWithoutPendingDest() {
            ProtocolPipeline stack = new ProtocolPipeline();
            byte[] data = "ciao".getBytes(StandardCharsets.UTF_8);
            app.send(stack, data); // no pendingDest
      }

      // Dummy class

      private static class DummyNode extends NetworkNode {
            public DummyNode() {
                  super(
                  "dummy",
                  new RoutingTable(),
                  new ArpTable(),
                  Collections.singletonList(
                        new Interface(
                              new CabledAdapter("eth0", 1500, new Mac("00:11:22:33:44:55")),
                              new IPv4("10.0.0.1", 24)
                        )
                  )
                  );
            }

            @Override
            public void receive(ProtocolPipeline stack, byte[] packets) {
                  // no-op
            }

            @Override
            public void send(IPv4 destination, ProtocolPipeline stack, byte[] data) {
                  // no-op
            }

            @Override
            public Port randomPort() {
                  return new Port("4321");
            }
      }
}