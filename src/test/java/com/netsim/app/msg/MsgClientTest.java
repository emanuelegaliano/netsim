package com.netsim.app.msg;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Port;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.MSG.MSGProtocol;
import com.netsim.protocols.UDP.UDPProtocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingTable;

import org.junit.Before;
import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class MsgClientTest {
      private MsgClient client;
      private IPv4 serverIP;

      @Before
      public void setUp() {
            serverIP = new IPv4("10.0.0.1", 24);
            client = new MsgClient(new DummyNode(), serverIP);
      }

      @Test(expected = IllegalArgumentException.class)
      public void testSendWithNullStack() {
            client.send(null, "hello".getBytes());
      }

      @Test(expected = IllegalArgumentException.class)
      public void testSendWithEmptyData() {
            client.send(new ProtocolPipeline(), new byte[0]);
      }

      @Test
      public void sendPushesUdpProtocolOnStack() {
            DummyNode node = new DummyNode();
            MsgClient client = new MsgClient(node, new IPv4("192.168.0.1", 24));
            ProtocolPipeline stack = new ProtocolPipeline();

            byte[] data = "Hello".getBytes(StandardCharsets.UTF_8);
            client.send(stack, data);

            assertEquals(1, stack.size());
            Protocol popped = stack.pop();
            assertTrue(popped instanceof UDPProtocol);
      }

      @Test(expected = IllegalArgumentException.class)
      public void testReceiveWithNullData() {
            client.receive(new ProtocolPipeline(), null);
      }

      @Test
      public void testReceivePrintsFormattedMessage() {
            String username = "testUser";
            String message = "hello world";

            ProtocolPipeline stack = new ProtocolPipeline();
            MSGProtocol msgProto = new MSGProtocol(username);
            byte[] msgPayload = msgProto.encapsulate(message.getBytes(StandardCharsets.UTF_8));

            UDPProtocol udpProto = new UDPProtocol(512, new Port("1234"), MSGProtocol.port());
            byte[] udpPayload = udpProto.encapsulate(msgPayload);

            stack.push(msgProto);
            stack.push(udpProto);

            client.receive(stack, udpPayload);
            // Just test that no exception is thrown
      }

      // Dummy NetworkNode for testing
      private static class DummyNode extends NetworkNode {
            public DummyNode() {
                  super("dummy", new RoutingTable(), new ArpTable(), new ArrayList<>());
            }

            @Override
            public void send(IPv4 destination, ProtocolPipeline stack, byte[] data) {
                  // no-op
            }

            @Override
            public void receive(ProtocolPipeline stack, byte[] packets) {
                  // no-op
            }

            @Override
            public int getMTU() {
                  return 1500;
            }

            @Override
            public Port randomPort() {
                  return new Port("1234");
            }
      }
}