package com.netsim.network.host;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.app.CommandFactory;
import com.netsim.network.Interface;
import com.netsim.network.CabledAdapter;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class HostTest {
      private Host host;
      private IPv4 ip;
      private CabledAdapter adapter;
      private Interface iface;

      @Before
      public void setUp() {
            ip = new IPv4("192.168.0.1", 24);
            adapter = new CabledAdapter("eth0", 1500, new Mac("aa:bb:cc:dd:ee:ff"));
            iface = new Interface(adapter, ip);
            RoutingTable routingTable = new RoutingTable();
            routingTable.add(ip, new RoutingInfo(adapter, null));
            ArpTable arpTable = new ArpTable();
            host = new Host("test-host", routingTable, arpTable, Collections.singletonList(iface));
      }

      @Test
      public void testSetAndRunApp() {
            TestApp app = new TestApp();
            host.setApp(app);
            host.runApp();
            assertTrue("App should be marked as started", app.started);
      }

      @Test(expected = IllegalArgumentException.class)
      public void testRunAppWithoutSettingShouldThrow() {
            host.runApp(); // no app set
      }

      @Test(expected = IllegalArgumentException.class)
      public void testSendRejectsNullArguments() {
            host.send(null, new ProtocolPipeline(), new byte[]{1});
      }

      @Test
      public void testIsForMeReturnsTrueForMatchingIP() {
            assertTrue(host.isForMe(ip));
      }

      @Test
      public void testIsForMeReturnsFalseForUnknownIP() {
            assertFalse(host.isForMe(new IPv4("10.0.0.1", 24)));
      }

      // Dummy App subclass for testing
      static class TestApp extends App {
            public boolean started = false;
            public byte[] receivedData;

            public TestApp() {
                  super("test", "", new DummyCommandFactory(), null);
            }

            @Override
            public void start() {
                  this.started = true;
            }

            @Override
            public void send(ProtocolPipeline stack, byte[] data) {
            }

            @Override
            public void receive(ProtocolPipeline stack, byte[] data) {
                  this.receivedData = data;
            }
      }

            static class DummyCommandFactory implements CommandFactory {
                  @Override
                  public Command get(String cmd) {
                        return null;
                  }
            }

}