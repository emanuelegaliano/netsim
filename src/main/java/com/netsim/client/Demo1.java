package com.netsim.client;

import java.util.Arrays;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.app.msg.MsgClient;
import com.netsim.app.msg.MsgServer;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.CabledAdapter;
import com.netsim.network.host.Host;
import com.netsim.network.router.Router;
import com.netsim.network.server.Server;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;

public class Demo1 {
    public static void main(String[] args) {
        // 1. Indirizzi IP e MAC
        IPv4 ipHost    = new IPv4("10.0.0.2", 24);
        IPv4 ipR1      = new IPv4("10.0.0.1", 24);
        IPv4 ipR2      = new IPv4("10.0.1.1", 24);
        IPv4 ipServer  = new IPv4("10.0.1.2", 24);

        Mac macHost    = new Mac("02:00:00:00:00:01");
        Mac macR1      = new Mac("02:00:00:00:00:02");
        Mac macR2      = new Mac("02:00:00:00:00:03");
        Mac macServer  = new Mac("02:00:00:00:00:04");

        // 2. Adattatori e collegamenti
        NetworkAdapter adapterHost   = new CabledAdapter("adapterHost", 1500, macHost);
        NetworkAdapter adapterR1     = new CabledAdapter("adapterR1", 1500, macR1);
        NetworkAdapter adapterR2     = new CabledAdapter("adapterR2", 1500, macR2);
        NetworkAdapter adapterServer = new CabledAdapter("adapterServer", 1500, macServer);

        adapterHost.setRemoteAdapter(adapterR1);
        adapterR1.setRemoteAdapter(adapterHost);
        adapterR2.setRemoteAdapter(adapterServer);
        adapterServer.setRemoteAdapter(adapterR2);

        // 3. Tabelle Routing
        RoutingTable rtHost = new RoutingTable();
        RoutingTable rtRouter = new RoutingTable();
        RoutingTable rtServer = new RoutingTable();

        rtHost.add(new IPv4("10.0.1.0", 24), new RoutingInfo(adapterHost, ipR1));
        rtRouter.add(new IPv4("10.0.0.0", 24), new RoutingInfo(adapterR1, ipHost));
        rtRouter.add(new IPv4("10.0.1.0", 24), new RoutingInfo(adapterR2, ipServer));
        rtServer.add(new IPv4("10.0.0.0", 24), new RoutingInfo(adapterServer, ipR2));

        // 4. Tabelle ARP
        ArpTable arpHost = new ArpTable();
        ArpTable arpRouter = new ArpTable();
        ArpTable arpServer = new ArpTable();

        arpHost.add(ipR1, macR1);
        arpRouter.add(ipHost, macHost);
        arpRouter.add(ipServer, macServer);
        arpServer.add(ipR2, macR2);

        // 5. Nodo Router
        Router router = new Router("Router", rtRouter, arpRouter, Arrays.asList(
            new Interface(adapterR1, ipR1),
            new Interface(adapterR2, ipR2)
        ));

        // 6. Nodo Server
        Interface serverInterface = new Interface(adapterServer, ipServer);
        Server<MsgServer> server = new Server<>(
            "Server",
            rtServer,
            arpServer,
            Arrays.asList(serverInterface)
        );

        MsgServer serverApp = new MsgServer(server);
        server.setApp(serverApp);

        // 7. Nodo Host
        Interface hostInterface = new Interface(adapterHost, ipHost);
        Host host = new Host("Host", rtHost, arpHost, Arrays.asList(hostInterface));
        MsgClient clientApp = new MsgClient(host, ipServer);
        host.setApp(clientApp);

        // 8. Owner degli adapter
        adapterHost.setOwner(host);
        adapterR1.setOwner(router);
        adapterR2.setOwner(router);
        adapterServer.setOwner(server);

        // 9. Avvio del client interattivo
        host.runApp();
    }
}