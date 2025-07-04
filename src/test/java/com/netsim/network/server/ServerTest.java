package com.netsim.network.server;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.app.CommandFactory;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.CabledAdapter;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingTable;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class ServerTest {
      private IPv4 ip;
      private NetworkAdapter adapter;
      private Interface iface;
      private DummyApp app;

      @Before
      public void setUp() {
            ip = new IPv4("192.168.1.1", 24);
            Mac mac = new Mac("aa:bb:cc:dd:ee:ff");  // MAC valido
            adapter = new CabledAdapter("eth0", 1500, mac);
            iface = new Interface(adapter, ip);
            app = new DummyApp();
      }

      @Test
      public void isForMeReturnsTrueWhenMatch() {
            Server<DummyApp> server = new Server<>("srv", new RoutingTable(), new ArpTable(), Collections.singletonList(iface));
            server.setApp(app);
            assertTrue(server.isForMe(new IPv4("192.168.1.1", 24)));
      }

      @Test
      public void isForMeReturnsFalseWhenNoMatch() {
            Server<DummyApp> server = new Server<>("srv", new RoutingTable(), new ArpTable(), Collections.singletonList(iface));
            server.setApp(app);
            assertFalse(server.isForMe(new IPv4("10.0.0.1", 24)));
      }

      static class DummyFactory implements CommandFactory {
            @Override
            public Command get(String name) {
                  return null;
            }
      }

      static class DummyApp extends App {

            public DummyApp() {
                  super("dummy", "usage", new DummyFactory(), null); // passa null come owner solo se supportato
            }

            @Override
            public void start() {}

            @Override
            public void send(ProtocolPipeline stack, byte[] data) {}

            @Override
            public void receive(ProtocolPipeline stack, byte[] data) {}
      }
}