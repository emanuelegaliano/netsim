// File: src/main/java/com/netsim/client/Demo1.java
package com.netsim.client;

import java.util.Scanner;
import java.util.Arrays;
import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.app.Command;
import com.netsim.app.msg.MsgClient;
import com.netsim.app.msg.MsgServerApp;
import com.netsim.network.Interface;
import com.netsim.network.NetworkAdapter;
import com.netsim.network.host.Host;
import com.netsim.table.ArpTable;
import com.netsim.table.RoutingTable;
import com.netsim.table.RoutingInfo;

public class Demo1 {
    public static void main(String[] args) {
        // 1. Configurazione degli indirizzi IP e MAC per server, Alice e Bob
        IPv4 serverIp1 = new IPv4("10.0.0.1", 24);  // Server <-> Alice network
        IPv4 aliceIp   = new IPv4("10.0.0.2", 24);
        IPv4 serverIp2 = new IPv4("10.0.1.1", 24);  // Server <-> Bob network
        IPv4 bobIp     = new IPv4("10.0.1.2", 24);
        Mac macA  = new Mac("02:00:00:00:00:01");   // Alice MAC
        Mac macS1 = new Mac("02:00:00:00:00:02");   // Server MAC sulla interfaccia verso Alice
        Mac macB  = new Mac("02:00:00:00:00:03");   // Bob MAC
        Mac macS2 = new Mac("02:00:00:00:00:04");   // Server MAC sulla interfaccia verso Bob

        // 2. Creazione degli adattatori di rete (MTU 1500 byte per tutti i collegamenti)
        NetworkAdapter adapterA  = new NetworkAdapter("adapterA", 1500, macA);
        NetworkAdapter adapterS1 = new NetworkAdapter("adapterS1", 1500, macS1);
        NetworkAdapter adapterB  = new NetworkAdapter("adapterB", 1500, macB);
        NetworkAdapter adapterS2 = new NetworkAdapter("adapterS2", 1500, macS2);

        // Collegamento punto-punto: Alice <-> Server e Bob <-> Server
        adapterA.setRemoteAdapter(adapterS1);
        adapterS1.setRemoteAdapter(adapterA);
        adapterB.setRemoteAdapter(adapterS2);
        adapterS2.setRemoteAdapter(adapterB);

        // 3. Inizializzazione delle tabelle di routing e ARP per ciascun nodo
        RoutingTable rtServer = new RoutingTable();
        ArpTable arpServer    = new ArpTable();
        RoutingTable rtAlice  = new RoutingTable();
        ArpTable arpAlice     = new ArpTable();
        RoutingTable rtBob    = new RoutingTable();
        ArpTable arpBob       = new ArpTable();

        // Aggiunta delle rotte statiche:
        // Server conosce entrambe le reti /24 (directly connected)
        rtServer.add(new IPv4("10.0.0.0", 24), new RoutingInfo(adapterS1, aliceIp));
        rtServer.add(new IPv4("10.0.1.0", 24), new RoutingInfo(adapterS2, bobIp));
        // Alice conosce la rete di Bob tramite il server
        rtAlice.add(new IPv4("10.0.1.0", 24), new RoutingInfo(adapterA, serverIp1));
        // Bob conosce la rete di Alice tramite il server
        rtBob.add(new IPv4("10.0.0.0", 24), new RoutingInfo(adapterB, serverIp2));

        // Popolamento delle tabelle ARP con gli indirizzi MAC noti (il server fa da "gateway")
        arpAlice.add(serverIp1, macS1);   // MAC del server (S1) per raggiungere serverIp1
        arpServer.add(aliceIp, macA);    // MAC di Alice per il suo IP
        arpBob.add(serverIp2, macS2);    // MAC del server (S2) per raggiungere serverIp2
        arpServer.add(bobIp, macB);      // MAC di Bob per il suo IP

        // 4. Creazione dei nodi Host (Server, Alice, Bob) con relative interfacce di rete
        Host serverHost = new Host("Server", rtServer, arpServer,
                            Arrays.asList(new Interface(adapterS1, serverIp1),
                                          new Interface(adapterS2, serverIp2)));
        Host aliceHost  = new Host("Alice", rtAlice, arpAlice,
                            Arrays.asList(new Interface(adapterA, aliceIp)));
        Host bobHost    = new Host("Bob", rtBob, arpBob,
                            Arrays.asList(new Interface(adapterB, bobIp)));

        // Assegnazione dei nodi proprietari agli adattatori (necessario per il recapito dei frame)
        adapterS1.setOwner(serverHost);
        adapterS2.setOwner(serverHost);
        adapterA.setOwner(aliceHost);
        adapterB.setOwner(bobHost);

        // 5. Creazione delle applicazioni di messaggistica per ciascun nodo
        MsgServerApp serverApp = new MsgServerApp(serverHost);
        MsgClient aliceClient  = new MsgClient(aliceHost, serverIp1);  // serverIp1 è l'indirizzo del server visto da Alice
        MsgClient bobClient    = new MsgClient(bobHost, serverIp2);    // serverIp2 è l'indirizzo del server visto da Bob

        // Associazione delle App ai rispettivi Host
        serverHost.setApp(serverApp);
        aliceHost.setApp(aliceClient);
        bobHost.setApp(bobClient);

        // 6. Autenticazione interattiva: richiesta del nome utente per Alice e Bob
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to msg");
        System.out.print("msg: Tell me your name: ");
        String nameAlice = scanner.nextLine();
        aliceClient.setUsername(nameAlice);              // memorizza lo username nel client Alice
        System.out.println("msg: Hello " + nameAlice);
        System.out.println("Welcome to msg");
        System.out.print("msg: Tell me your name: ");
        String nameBob = scanner.nextLine();
        bobClient.setUsername(nameBob);                  // memorizza lo username nel client Bob
        System.out.println("msg: Hello " + nameBob);

        // 7. Registrazione automatica dei client sul server inviando il proprio IP come primo messaggio
        try {
            aliceClient.getCommand("send").execute(aliceClient, aliceIp.stringRepresentation());
        } catch(Exception e) {
            System.err.println("Error registering Alice: " + e.getMessage());
        }
        try {
            bobClient.getCommand("send").execute(bobClient, bobIp.stringRepresentation());
        } catch(Exception e) {
            System.err.println("Error registering Bob: " + e.getMessage());
        }
        // A questo punto il server avrà stampato la conferma di registrazione per Alice e Bob.

        System.out.println("Both clients are now registered. You can send messages.");  // Messaggio informativo

        // 8. Loop interattivo per l'invio dei messaggi tra Alice e Bob
        while(true) {
            System.out.print("Enter command (e.g., Alice: send Bob:Hello): ");
            String line = scanner.nextLine();
            if(line.equalsIgnoreCase("exit")) {
                break;  // uscita dal loop su comando "exit"
            }
            if(line.trim().isEmpty()) {
                continue;
            }
            // Parsing dell'input utente: "Nome: comando parametri"
            String[] split = line.split(":", 2);
            if(split.length < 2) {
                System.out.println("Invalid input format. Use Name: command");
                continue;
            }
            String nodeName    = split[0].trim();
            String commandPart = split[1].trim();
            // Identificazione del nodo sorgente (Alice o Bob)
            MsgClient clientApp;
            if(nodeName.equalsIgnoreCase(nameAlice) || nodeName.equalsIgnoreCase("Alice")) {
                clientApp = aliceClient;
            } else if(nodeName.equalsIgnoreCase(nameBob) || nodeName.equalsIgnoreCase("Bob")) {
                clientApp = bobClient;
            } else {
                System.out.println("Unknown client name: " + nodeName);
                continue;
            }
            // Estrazione del comando e dei suoi parametri
            String cmd, params;
            int spaceIdx = commandPart.indexOf(' ');
            if(spaceIdx >= 0) {
                cmd    = commandPart.substring(0, spaceIdx);
                params = commandPart.substring(spaceIdx + 1);
            } else {
                cmd    = commandPart;
                params = "";
            }
            // Esecuzione del comando sul client selezionato
            try {
                Command actualCmd = clientApp.getCommand(cmd);
                actualCmd.execute(clientApp, params);
            } catch(Exception e) {
                System.out.println("Error executing command: " + e.getMessage());
            }
        }

        scanner.close();
    }
}