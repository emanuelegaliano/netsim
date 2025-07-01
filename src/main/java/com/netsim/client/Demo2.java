package com.netsim.client;

import java.util.Arrays;
import java.util.Collections;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.app.msg.MsgClient;
import com.netsim.app.msg.MsgServer;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.host.Host;
import com.netsim.network.router.Router;
import com.netsim.network.server.Server;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;

public class Demo2 {
    public static void main(String[] args) {
        // 1. Define IP and MAC addresses
        IPv4 ipH1     = new IPv4("10.0.0.2", 30);
        IPv4 ipH2     = new IPv4("10.0.1.2", 30);
        IPv4 ipSrv    = new IPv4("10.0.2.2", 30);
        IPv4 ipR1     = new IPv4("10.0.0.1", 30);
        IPv4 ipR2     = new IPv4("10.0.1.1", 30);
        IPv4 ipR3     = new IPv4("10.0.2.1", 30);

        Mac macH1     = new Mac("02:00:00:00:00:11");
        Mac macH2     = new Mac("02:00:00:00:00:22");
        Mac macSrv    = new Mac("02:00:00:00:00:33");
        Mac macR1     = new Mac("02:00:00:00:00:41");
        Mac macR2     = new Mac("02:00:00:00:00:42");
        Mac macR3     = new Mac("02:00:00:00:00:43");

        // 2. Create adapters and link point-to-point
        NetworkAdapter adapterH1  = new NetworkAdapter("h1-adapter", 1500, macH1);
        NetworkAdapter adapterH2  = new NetworkAdapter("h2-adapter", 1500, macH2);
        NetworkAdapter adapterSrv = new NetworkAdapter("srv-adapter", 1500, macSrv);
        NetworkAdapter adapterR1  = new NetworkAdapter("r1-adapter", 1500, macR1);
        NetworkAdapter adapterR2  = new NetworkAdapter("r2-adapter", 1500, macR2);
        NetworkAdapter adapterR3  = new NetworkAdapter("r3-adapter", 1500, macR3);

        // host1 <-> router net0
        adapterH1.setRemoteAdapter(adapterR1);
        adapterR1.setRemoteAdapter(adapterH1);

        // host2 <-> router net1
        adapterH2.setRemoteAdapter(adapterR2);
        adapterR2.setRemoteAdapter(adapterH2);

        // server <-> router net2
        adapterSrv.setRemoteAdapter(adapterR3);
        adapterR3.setRemoteAdapter(adapterSrv);

        // 3. Build routing tables

        // Host1 routes everything via router on net0
        RoutingTable rtH1 = new RoutingTable();
        rtH1.add(new IPv4("0.0.0.0", 0), new RoutingInfo(adapterH1, ipR1));

        // Host2 routes everything via router on net1
        RoutingTable rtH2 = new RoutingTable();
        rtH2.add(new IPv4("0.0.0.0", 0), new RoutingInfo(adapterH2, ipR2));

        // Server routes net0 & net1 via router adapters
        RoutingTable rtSrv = new RoutingTable();
        rtSrv.add(new IPv4("10.0.0.0", 30), new RoutingInfo(adapterSrv, ipR3));
        rtSrv.add(new IPv4("10.0.1.0", 30), new RoutingInfo(adapterSrv, ipR3));

        // Router routes each /30 directly
        RoutingTable rtR = new RoutingTable();
        rtR.add(new IPv4("10.0.0.0", 30), new RoutingInfo(adapterR1, null));
        rtR.add(new IPv4("10.0.1.0", 30), new RoutingInfo(adapterR2, null));
        rtR.add(new IPv4("10.0.2.0", 30), new RoutingInfo(adapterR3, null));

        // 4. Build ARP tables

        ArpTable arpH1 = new ArpTable();
        arpH1.add(ipR1, macR1);

        ArpTable arpH2 = new ArpTable();
        arpH2.add(ipR2, macR2);

        ArpTable arpSrv = new ArpTable();
        arpSrv.add(ipR3, macR3);

        ArpTable arpR = new ArpTable();
        arpR.add(ipH1, macH1);
        arpR.add(ipH2, macH2);
        arpR.add(ipSrv,  macSrv);

        // 5. Instantiate Router
        Router router = new Router(
            "Router",
            rtR,
            arpR,
            Arrays.asList(
                new Interface(adapterR1, ipR1),
                new Interface(adapterR2, ipR2),
                new Interface(adapterR3, ipR3)
            )
        );

        // 6. Instantiate Server
        Interface srvIface = new Interface(adapterSrv, ipSrv);
        Server<MsgServer> server = new Server<>(
            "Server",
            rtSrv,
            arpSrv,
            Collections.singletonList(srvIface)
        );
        MsgServer serverApp = new MsgServer(server);
        server.setApp(serverApp);

        // 7. Instantiate Host1
        Interface h1Iface = new Interface(adapterH1, ipH1);
        Host    h1       = new Host("Host1", rtH1, arpH1, Collections.singletonList(h1Iface));
        MsgClient client1= new MsgClient(h1, ipSrv);
        h1.setApp(client1);

        // 8. Instantiate Host2
        Interface h2Iface = new Interface(adapterH2, ipH2);
        Host    h2       = new Host("Host2", rtH2, arpH2, Collections.singletonList(h2Iface));
        MsgClient client2= new MsgClient(h2, ipSrv);
        h2.setApp(client2);

        // 9. Set adapter owners
        adapterH1.setOwner(h1);
        adapterH2.setOwner(h2);
        adapterSrv.setOwner(server);
        adapterR1.setOwner(router);
        adapterR2.setOwner(router);
        adapterR3.setOwner(router);

        // 10. Register both hosts
        client1.setUsername("Manu");
        client2.setUsername("Alice");

        client1.register();
        client2.register();

        // 11. Start Host1's application loop
        h1.runApp();
    }
}