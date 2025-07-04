package com.netsim.app.msg;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Port;
import com.netsim.network.Interface;
import com.netsim.network.CabledAdapter;
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
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Unit tests for MsgClient, incl. the new register() method.
 */
public class MsgClientTest {
    private MsgClient client;
    private IPv4      serverIP;

    @Before
    public void setUp() {
        serverIP = new IPv4("10.0.0.1", 24);
        client   = new MsgClient(new DummyNode(), serverIP);
        client.setUsername("tester");
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
        DummyNode node   = new DummyNode();
        MsgClient client = new MsgClient(node, new IPv4("192.168.0.1", 24));
        client.setUsername("user");
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
        ProtocolPipeline stack    = new ProtocolPipeline();
        String           username = "testUser";
        String           message  = "hello world";

        MSGProtocol msgProto = new MSGProtocol(username);
        byte[]      msgBytes = msgProto.encapsulate(message.getBytes(StandardCharsets.UTF_8));

        UDPProtocol udpProto = new UDPProtocol(512, new Port("1234"), MSGProtocol.port());
        byte[]      udpBytes = udpProto.encapsulate(msgBytes);

        stack.push(msgProto);
        stack.push(udpProto);

        // non deve sollevare eccezioni
        client.receive(stack, udpBytes);
    }

    @Test
    public void testRegisterSendsOwnIP() {
        IPv4           myIp   = new IPv4("192.168.42.7", 24);
        CapturingNode  node   = new CapturingNode(myIp);
        MsgClient      client = new MsgClient(node, serverIP);
        client.setUsername("user");

        client.register();

        assertTrue(node.sendCalled);

        ProtocolPipeline sentStack = node.lastStack;
        assertNotNull(sentStack);
        assertEquals(2, sentStack.size());

        Protocol first  = sentStack.pop();
        Protocol second = sentStack.pop();
        assertTrue(first  instanceof UDPProtocol);
        assertTrue(second instanceof MSGProtocol);
    }

    // ─── DummyNode for basic send/receive ───────────────────────────

    private static class DummyNode extends NetworkNode {
        public DummyNode() {
            super("dummy", new RoutingTable(), new ArpTable(), Collections.emptyList());
        }

        @Override
        public void send(IPv4 dest, ProtocolPipeline stack, byte[] data) {
            // no-op
        }

        @Override
        public void receive(ProtocolPipeline stack, byte[] data) {
            // no-op
        }

        @Override
        public int getMTU() {
            // Must be >20+20 to get a positive MSS
            return 1500;
        }

        @Override
        public Port randomPort() {
            // Return a valid ephemeral port
            return new Port("1234");
        }
    }

    // ─── CapturingNode for register() test ──────────────────────────

    private static class CapturingNode extends NetworkNode {
        boolean sendCalled = false;
        ProtocolPipeline lastStack;

        public CapturingNode(IPv4 ifaceIp) {
            super("capture",
                  new RoutingTable(),
                  new ArpTable(),
                  Collections.singletonList(
                      new Interface(
                          new CabledAdapter("if0", 1500, com.netsim.addresses.Mac.broadcast()),
                          ifaceIp
                      )
                  )
            );
        }

        @Override
        public void send(IPv4 destination, ProtocolPipeline stack, byte[] data) {
            this.sendCalled = true;
            this.lastStack  = stack;
        }

        @Override
        public void receive(ProtocolPipeline stack, byte[] data) {
            // not used in this test
        }
    }
}