package com.netsim.client;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.app.msg.MsgClient;
import com.netsim.app.msg.MsgServer;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.CabledAdapter;
import com.netsim.network.host.Host;
import com.netsim.network.host.HostBuilder;
import com.netsim.network.router.Router;
import com.netsim.network.router.RouterBuilder;
import com.netsim.network.server.Server;
import com.netsim.network.server.ServerBuilder;

public class Demo2 {
    public static void main(String[] args) {
        // 1) Definisco IP e MAC
        IPv4 ipH1  = new IPv4("10.0.0.2", 30);
        IPv4 ipH2  = new IPv4("10.0.1.2", 30);
        IPv4 ipSrv = new IPv4("10.0.2.2", 30);
        IPv4 ipR1  = new IPv4("10.0.0.1", 30);
        IPv4 ipR2  = new IPv4("10.0.1.1", 30);
        IPv4 ipR3  = new IPv4("10.0.2.1", 30);

        Mac macH1  = new Mac("02:00:00:00:00:11");
        Mac macH2  = new Mac("02:00:00:00:00:22");
        Mac macSrv = new Mac("02:00:00:00:00:33");
        Mac macR1  = new Mac("02:00:00:00:00:41");
        Mac macR2  = new Mac("02:00:00:00:00:42");
        Mac macR3  = new Mac("02:00:00:00:00:43");

        // 2) Creo gli adapter e li collego punto-punto
        NetworkAdapter aH1  = new CabledAdapter("h1-adapter", 1500, macH1);
        NetworkAdapter aH2  = new CabledAdapter("h2-adapter", 1500, macH2);
        NetworkAdapter aSrv = new CabledAdapter("srv-adapter",1500, macSrv);
        NetworkAdapter aR1  = new CabledAdapter("r1-adapter", 1500, macR1);
        NetworkAdapter aR2  = new CabledAdapter("r2-adapter", 1500, macR2);
        NetworkAdapter aR3  = new CabledAdapter("r3-adapter", 1500, macR3);

        aH1.setRemoteAdapter(aR1);  aR1.setRemoteAdapter(aH1);
        aH2.setRemoteAdapter(aR2);  aR2.setRemoteAdapter(aH2);
        aSrv.setRemoteAdapter(aR3); aR3.setRemoteAdapter(aSrv);

        // 3) Costruisco router con 3 interfacce, rotte e ARP
        Router router = new RouterBuilder()
            .setName("Router")
            .addInterface(new Interface(aR1, ipR1))
            .addInterface(new Interface(aR2, ipR2))
            .addInterface(new Interface(aR3, ipR3))
            .addRoute(new IPv4("10.0.0.0",30), "r1-adapter", null)
            .addRoute(new IPv4("10.0.1.0",30), "r2-adapter", null)
            .addRoute(new IPv4("10.0.2.0",30), "r3-adapter", null)
            .addArpEntry(ipH1, macH1)
            .addArpEntry(ipH2, macH2)
            .addArpEntry(ipSrv, macSrv)
            .build();

        // 4) Costruisco server con interfaccia, rotte e ARP
        Server<MsgServer> server = new ServerBuilder<MsgServer>()
            .setName("Server")
            .addInterface(new Interface(aSrv, ipSrv))
            .addRoute(new IPv4("10.0.0.0",30), "srv-adapter", ipR3)
            .addRoute(new IPv4("10.0.1.0",30), "srv-adapter", ipR3)
            .addArpEntry(ipR3, macR3)
            .build();

        // Configuro l’applicazione server
        MsgServer srvApp = new MsgServer(server);
        server.setApp(srvApp);

        // 5) Costruisco Host1
        Host h1 = new HostBuilder()
            .setName("Host1")
            .addInterface(new Interface(aH1, ipH1))
            .addRoute(new IPv4("0.0.0.0",0), "h1-adapter", ipR1)
            .addArpEntry(ipR1, macR1)
            .build();
        
        MsgClient client1 = new MsgClient(h1, ipSrv);
        h1.setApp(client1);

        // 6) Costruisco Host2
        Host h2 = new HostBuilder()
            .setName("Host2")
            .addInterface(new Interface(aH2, ipH2))
            .addRoute(new IPv4("0.0.0.0",0), "h2-adapter", ipR2)
            .addArpEntry(ipR2, macR2)
            .build();
        
        MsgClient client2 = new MsgClient(h2, ipSrv);
        h2.setApp(client2);

        // 7) Imposto gli owner sugli adapter
        aH1.setOwner(h1);
        aH2.setOwner(h2);
        aSrv.setOwner(server);
        aR1.setOwner(router);
        aR2.setOwner(router);
        aR3.setOwner(router);

        // 8) Registro i due client
        client1.setUsername("Manu");
        client2.setUsername("Alice");
        client1.register();
        client2.register();

        // 9) Avvio l’app su Host1
        h1.runApp();
    }
}