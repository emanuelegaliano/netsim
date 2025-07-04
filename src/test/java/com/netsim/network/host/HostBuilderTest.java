package com.netsim.network.host;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.network.Interface;
import com.netsim.network.CabledAdapter;

/**
 * Unit tests for {@link HostBuilder} to verify builder validation and build logic.
 */
public class HostBuilderTest {
      private HostBuilder builder;
      private CabledAdapter adapter;
      private Interface iface;
      private IPv4 ip;
      private Mac mac;

      /**
       * Prepare a fresh builder and sample interface and ARP parameters.
       */
      @Before
      public void setUp() throws Exception {
            builder = new HostBuilder();
            adapter = new CabledAdapter(
                  "eth0", 1500,
                  Mac.bytesToMac(new byte[]{0,1,2,3,4,5})
            );
            ip = new IPv4("192.168.0.1", 24);
            iface = new Interface(adapter, ip);
            mac   = Mac.bytesToMac(new byte[]{1,2,3,4,5,6});
      }

      // -------- setName / addInterface argument validation ------------------------

      @Test(expected = IllegalArgumentException.class)
      public void setNameRejectsNull() throws Exception {
            builder.setName(null);
      }

      @Test(expected = IllegalArgumentException.class)
      public void addInterfaceRejectsNull() throws Exception {
            builder.addInterface(null);
      }

      // -------- addArpEntry argument validation ------------------------------------

      @Test(expected = IllegalArgumentException.class)
      public void addArpEntryRejectsNullIp() throws Exception {
            builder.addArpEntry(null, mac);
      }

      @Test(expected = IllegalArgumentException.class)
      public void addArpEntryRejectsNullMac() throws Exception {
            builder.addArpEntry(ip, null);
      }

      // -------- addRoute argument validation ---------------------------------------

      @Test(expected = IllegalArgumentException.class)
      public void addRouteRejectsNullSubnet() throws Exception {
            builder.addRoute(null, "eth0", ip);
      }

      @Test(expected = IllegalArgumentException.class)
      public void addRouteRejectsNullAdapterName() throws Exception {
            builder.addRoute(ip, null, ip);
      }

      @Test(expected = IllegalArgumentException.class)
      public void addRouteRejectsNullNextHop() throws Exception {
            builder.addRoute(ip, "eth0", null);
      }

      @Test(expected = IllegalArgumentException.class)
      public void addRouteRejectsUnknownAdapter() throws Exception {
            // no interface added yet, so adapterName "eth0" is unknown
            builder.addRoute(ip, "eth0", ip);
      }

      // -------- build() argument validation ----------------------------------------

      @Test(expected = RuntimeException.class)
      public void buildRejectsEmptyRoutingTable() throws Exception {
            builder
                  .setName("h1")
                  .addInterface(iface)
                  .addArpEntry(ip, mac)
                  .build(); // routingTable is empty -> RuntimeException
      }

      @Test(expected = RuntimeException.class)
      public void buildRejectsEmptyArpTable() throws Exception {
            builder
                  .setName("h1")
                  .addInterface(iface)
                  // add a valid route so routingTable is non-empty
                  .addRoute(new IPv4("10.0.0.0", 8), "eth0", ip)
                  .build(); // arpTable is empty -> RuntimeException
      }

      @Test(expected = RuntimeException.class)
      public void buildRejectsEmptyInterfaces() throws Exception {
            builder
                  .setName("h1")
                  // add arp and route, but interfaces list is still empty
                  .addArpEntry(ip, mac)
                  .addRoute(new IPv4("10.0.0.0", 8), "eth0", ip)
                  .build(); // interfaces is empty -> RuntimeException
      }

      // -------- successful build ----------------------------------------

      @Test
      public void buildSucceedsWhenAllConfigured() throws Exception {
            Host host = builder
                  .setName("h1")              // valid name
                  .addInterface(iface)         // at least one interface
                  .addArpEntry(ip, mac)        // at least one ARP entry
                  .addRoute(new IPv4("10.0.0.0", 8), "eth0", ip) // at least one route
                  .build();                    // should not throw

            assertNotNull("build() must return a Host", host);
            assertEquals("Name should be set on Host", "h1", host.getName());
      }
}