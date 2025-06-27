package com.netsim.network.server;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.app.CommandFactory;
import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.SimpleDLL.SimpleDLLProtocol;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;

/**
 * Minimal tests for {@link Server}, covering constructor and method argument validation.
 */
public class ServerTest {
    private Server<App>      server;
    private ProtocolPipeline pipeline;
    private RoutingInfo      stubRoute;

    private static CommandFactory fakeFactory = new CommandFactory() {
        public Command get(String key) {
            return null;
        }
    };

    /**
     * Simple App stub for test instantiation.
     */
    private static class DummyApp extends App {
        DummyApp() { super("dummy","usage", fakeFactory, null); }
        @Override public void start(NetworkNode n) { }
        @Override public void receive(byte[] data) { }
        @Override public void printAppMessage(String m) { }
    }

    @Before
    public void setUp() {
        // create a pipeline with at least one protocol so encapsulate/decapsulate won't NPE
        this.pipeline = new ProtocolPipeline();
        this.pipeline.push(new SimpleDLLProtocol(Mac.broadcast(),Mac.broadcast()));

        // a stub route
        NetworkAdapter adapter = new NetworkAdapter("eth0",1500,Mac.broadcast());
        this.stubRoute = new RoutingInfo(adapter,new IPv4("10.0.0.1",24));

        // valid server
        this.server = new Server<>(
            "srv",
            new RoutingTable(),
            new ArpTable(),
            Collections.emptyList(),
            new DummyApp()
        );
    }

    // -------- constructor argument validation --------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullName() {
        new Server<>(null,new RoutingTable(),new ArpTable(),Collections.emptyList(),new DummyApp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullRoutingTable() {
        new Server<>("s",null,new ArpTable(),Collections.emptyList(),new DummyApp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullArpTable() {
        new Server<>("s",new RoutingTable(),null,Collections.emptyList(),new DummyApp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullInterfaces() {
        new Server<>("s",new RoutingTable(),new ArpTable(),null,new DummyApp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullApp() {
        new Server<>("s",new RoutingTable(),new ArpTable(),Collections.emptyList(),null);
    }

    // -------- send(...) argument validation --------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void sendNullRouteThrows() {
        this.server.send(null,this.pipeline,new byte[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendNullPipelineThrows() {
        this.server.send(this.stubRoute,null,new byte[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendNullDataThrows() {
        this.server.send(this.stubRoute,this.pipeline,null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendEmptyDataThrows() {
        this.server.send(this.stubRoute,this.pipeline,new byte[0]);
    }

    // -------- receive(...) argument validation -----------------------------

    @Test(expected = IllegalArgumentException.class)
    public void receiveNullPipelineThrows() {
        this.server.receive(null,new byte[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void receiveNullDataThrows() {
        this.server.receive(this.pipeline,null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void receiveEmptyDataThrows() {
        this.server.receive(this.pipeline,new byte[0]);
    }
}