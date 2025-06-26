package com.netsim.network.server;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.netsim.app.App;
import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;

/**
 * Unit tests for {@link ServerBuilder}, reflecting the new setApp-based configuration.
 */
public class ServerBuilderTest {
    private ServerBuilder<MyApp> builder;
    private MyApp app;
    private NetworkAdapter adapter;
    private Interface iface;
    private IPv4 ip;
    private Mac mac;

    /**
     * Simple App stub for testing.
     */
    private static class MyApp extends App {
        MyApp() { super("app", "user"); }
        @Override public void start() { }
        @Override public void receive(IPv4 s, byte[] d) { }
        @Override public void printAppMessage(String m) { }
    }

    /**
     * Prepare builder and sample network objects.
     */
    @Before
    public void setUp() throws Exception {
        this.builder = new ServerBuilder<>();
        this.app     = new MyApp();
        this.adapter = new NetworkAdapter(
            "eth0", 1500,
            Mac.bytesToMac(new byte[]{0,1,2,3,4,5})
        );
        this.ip      = new IPv4("192.168.1.10", 24);
        this.iface   = new Interface(this.adapter, this.ip);
        this.mac     = Mac.bytesToMac(new byte[]{1,2,3,4,5,6});
    }

    // -------- setApp validation -----------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void setAppRejectsNull() {
        this.builder.setApp(null);
    }

    @Test
    public void setAppAcceptsValid() {
        ServerBuilder<MyApp> b = this.builder.setApp(this.app);
        assertSame("setApp should return builder itself", this.builder, b);
    }

    // -------- inherited builder methods ---------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void setNameRejectsNull() throws Exception {
        this.builder.setName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addInterfaceRejectsNull() throws Exception {
        this.builder.addInterface(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addArpEntryRejectsNullIp() throws Exception {
        this.builder.addArpEntry(null, this.mac);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addArpEntryRejectsNullMac() throws Exception {
        this.builder.addArpEntry(this.ip, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRouteRejectsNullSubnet() throws Exception {
        this.builder.addRoute(null, "eth0", this.ip);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRouteRejectsNullAdapterName() throws Exception {
        this.builder.addRoute(this.ip, null, this.ip);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRouteRejectsNullNextHop() throws Exception {
        this.builder.addRoute(this.ip, "eth0", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRouteRejectsUnknownAdapter() throws Exception {
        this.builder.addRoute(this.ip, "eth0", this.ip);
    }

    // -------- build() validation ---------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void buildRejectsWithoutApp() {
        // configure tables and interfaces but do not setApp()
        this.builder
            .setName("s1")
            .addInterface(this.iface)
            .addArpEntry(this.ip, this.mac)
            .addRoute(new IPv4("10.0.0.0", 8), "eth0", this.ip)
            .build(); // Server constructor should reject null app
    }

    @Test(expected = RuntimeException.class)
    public void buildRejectsEmptyRoutingTable() {
        this.builder
            .setApp(this.app)
            .setName("s1")
            .addInterface(this.iface)
            .addArpEntry(this.ip, this.mac)
            .build(); // routingTable empty
    }

    @Test(expected = RuntimeException.class)
    public void buildRejectsEmptyArpTable() {
        this.builder
            .setApp(this.app)
            .setName("s1")
            .addInterface(this.iface)
            .addRoute(new IPv4("10.0.0.0", 8), "eth0", this.ip)
            .build(); // arpTable empty
    }

    @Test(expected = RuntimeException.class)
    public void buildRejectsEmptyInterfaces() {
        this.builder
            .setApp(this.app)
            .setName("s1")
            .addArpEntry(this.ip, this.mac)
            .addRoute(new IPv4("10.0.0.0", 8), "eth0", this.ip)
            .build(); // interfaces empty
    }

    @Test
    public void buildSucceedsWhenAllSet() {
        Server<MyApp> server = this.builder
            .setApp(this.app)
            .setName("s1")
            .addInterface(this.iface)
            .addArpEntry(this.ip, this.mac)
            .addRoute(new IPv4("10.0.0.0", 8), "eth0", this.ip)
            .build();

        assertNotNull("build() must return a Server", server);
        assertEquals("Name should be set on Server", "s1", server.getName());
    }
}
