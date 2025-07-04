package com.netsim.network.router;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.CabledAdapter;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.IPv4.IPv4Protocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingTable;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

public class RouterTest {
      private Router router;
      private NetworkAdapter adapter1;
      private NetworkAdapter adapter2;
      private Interface iface1;
      private Interface iface2;
      private IPv4 destIP;
      private IPv4 localIP1;
      private IPv4 localIP2;

      @Before
      public void setUp() {
            adapter1 = new CabledAdapter("eth0", 1500, new Mac("aa:bb:cc:00:00:01"));
            adapter2 = new CabledAdapter("eth1", 1500, new Mac("aa:bb:cc:00:00:02"));
            localIP1 = new IPv4("10.0.0.1", 24);
            localIP2 = new IPv4("192.168.1.1", 24);
            destIP = new IPv4("192.168.1.99", 32);

            iface1 = new Interface(adapter1, localIP1);
            iface2 = new Interface(adapter2, localIP2);

            RoutingTable rt = new RoutingTable();
            ArpTable at = new ArpTable();
            rt.add(new IPv4("192.168.1.0", 24), new com.netsim.table.RoutingInfo(adapter2, null));
            at.add(destIP, adapter2.getMacAddress());

            router = new Router("router1", rt, at, Arrays.asList(iface1, iface2));
      }

      @Test(expected = IllegalArgumentException.class)
      public void sendRejectsNullDestination() {
            router.send(null, new ProtocolPipeline(), new byte[]{1, 2, 3});
      }

      @Test(expected = IllegalArgumentException.class)
      public void sendRejectsEmptyPayload() {
            router.send(destIP, new ProtocolPipeline(), new byte[0]);
      }

      @Test(expected = IllegalArgumentException.class)
      public void receiveRejectsNullStack() {
            router.receive(null, new byte[]{1});
      }

      @Test
      public void receiveDropsPacketWithTTLZero() {
            byte[] payload = "Hello".getBytes();
            IPv4Protocol ip = new IPv4Protocol(localIP1, destIP, 5, 0, 0, 0, 0, 0, 1500);
            byte[] encoded = ip.encapsulate(payload);
            ProtocolPipeline stack = new ProtocolPipeline();
            stack.push(ip); // Simuliamo che sia stato encapsulato

            router.receive(stack, encoded); // TTL = 0 → deve essere droppato
            // Non c'è assert diretto, ma nessuna eccezione è OK.
      }

      @Test
      public void receiveForwardsWithDecrementedTTL() {
            byte[] payload = "Ping".getBytes();
            IPv4Protocol ip = new IPv4Protocol(localIP1, destIP, 5, 0, 0, 0, 3, 0, 1500);
            byte[] encoded = ip.encapsulate(payload);

            ProtocolPipeline stack = new ProtocolPipeline();
            stack.push(ip);

            // Collegamento logico bidirezionale tra adapter2 e un adapter fittizio
            CabledAdapter dummyAdapter = new CabledAdapter("dummy", 1500, new Mac("aa:aa:aa:aa:aa:aa"));
            dummyAdapter.setRemoteAdapter(adapter2);
            adapter2.setRemoteAdapter(dummyAdapter);

            // Set owner per evitare eccezione
            adapter1.setOwner(router);
            adapter2.setOwner(router);
            dummyAdapter.setOwner(new DummyNode());

            router.receive(stack, encoded); // dovrebbe inoltrare senza errori
      }

      @Test
      public void sendDropsIfNoRouteExists() {
            IPv4 unreachable = new IPv4("172.16.0.5", 32);
            ProtocolPipeline stack = new ProtocolPipeline();
            byte[] data = {1, 2, 3, 4};
            router.send(unreachable, stack, data); // logga errore, non crasha
      }

      @Test
      public void constructorAndBasicsWork() {
            assertEquals("router1", router.getName());
            assertEquals(2, router.getInterfaces().size());
      }

      private static class DummyNode extends NetworkNode {
            public DummyNode() {
                  super("dummy", new RoutingTable(), new ArpTable(), List.of());
            }

            @Override
            public void receive(ProtocolPipeline stack, byte[] data) {
                  // No-op
            }
            
            @Override
            public void send(IPv4 ip, ProtocolPipeline stack, byte[] data) {
                  // No-op
            }
      }
}