package com.netsim.network.server;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.app.CommandFactory;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.ProtocolPipeline;

import org.junit.Test;
import static org.junit.Assert.*;

public class ServerBuilderTest {
      private static class DummyCommandFactory implements CommandFactory {
            @Override
            public Command get(String name) {
                  return null;
            }
      }

      private static class DummyApp extends App {
            public DummyApp() {
                  super("DummyApp", "Usage", new DummyCommandFactory(), (NetworkNode) null);
            }

            @Override public void start() {}
            @Override public void send(ProtocolPipeline stack, byte[] data) {}
            @Override public void receive(ProtocolPipeline stack, byte[] data) {}
      } 

      @Test(expected = RuntimeException.class)
      public void buildFailsWithoutName() {
            ServerBuilder<DummyApp> builder = new ServerBuilder<>();
            builder.build(); // should throw
      }

      @Test(expected = RuntimeException.class)
      public void buildFailsWithoutApp() {
            ServerBuilder<DummyApp> builder = new ServerBuilder<>();
            builder.setName("srv")
                  .build(); // should throw
      }

      @Test(expected = RuntimeException.class)
      public void buildFailsWithoutInterfaces() {
            ServerBuilder<DummyApp> builder = new ServerBuilder<>();
            builder.setName("srv")
                   .build(); // should throw
      }

      @Test(expected = RuntimeException.class)
      public void buildFailsWithoutRouting() {
            ServerBuilder<DummyApp> builder = new ServerBuilder<>();
            NetworkAdapter adapter = new NetworkAdapter("eth0", 1500, new Mac("AA:BB:CC:DD:EE:01"));
            Interface iface = new Interface(adapter, new IPv4("192.168.0.2", 24));

            builder.setName("srv")     
                   .addInterface(iface)
                   .build(); // should throw
      }

      @Test(expected = RuntimeException.class)
      public void buildFailsWithoutArp() {
            ServerBuilder<DummyApp> builder = new ServerBuilder<>();
            NetworkAdapter adapter = new NetworkAdapter("eth0", 1500, new Mac("AA:BB:CC:DD:EE:01"));
            Interface iface = new Interface(adapter, new IPv4("192.168.0.2", 24));

                  
            
            builder.setName("srv")
                   .addInterface(iface)
                   .addRoute(new IPv4("192.168.0.0", 24), "eth0", new IPv4("192.168.0.1", 24))
                   .build(); // should throw
      }

      @Test
      public void buildSucceedsWithAllParameters() {
            ServerBuilder<DummyApp> builder = new ServerBuilder<>();
            NetworkAdapter adapter = new NetworkAdapter("eth0", 1500, new Mac("AA:BB:CC:DD:EE:01"));
            Interface iface = new Interface(adapter, new IPv4("192.168.0.2", 24));

            Server<DummyApp> server = builder.setName("srv")     
                                             .addInterface(iface)
                                             .addRoute(new IPv4("192.168.0.0", 24), "eth0", new IPv4("192.168.0.1", 24))
                                             .addArpEntry(new IPv4("192.168.0.1", 24), new Mac("AA:BB:CC:DD:EE:02"))
                                             .build();

            assertEquals("srv", server.getName());
      }
}