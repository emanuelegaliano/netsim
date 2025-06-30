package com.netsim.app.msg;

import java.util.Scanner;

import com.netsim.addresses.IPv4;
import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.Protocol;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.MSG.MSGProtocol;
import com.netsim.protocols.UDP.UDPProtocol;
import com.netsim.utils.Logger;

public class MsgClient extends App {
    private final Logger logger = Logger.getInstance();
    private final Scanner input;
    private final IPv4 serverIP;

    public MsgClient(NetworkNode node, IPv4 serverIP) {
        super(
            "msg",
            "<command> <parameters> (print help for a list of commands)",
            new MsgCommandFactory(),
            node
        );
        String cls = this.getClass().getSimpleName();
        if (serverIP == null) {
            logger.error("[" + cls + "] invalid server IP: null");
            throw new IllegalArgumentException(cls + ": invalid server ip");
        }
        this.serverIP = serverIP;
        this.input = new Scanner(System.in);
        logger.info("[" + cls + "] initialized for server " + serverIP.stringRepresentation());
    }

    public void askName() {
        this.printAppMessage("Tell me your name: ");
        this.setUsername(this.input.nextLine());
    }

    @Override
    public void start() {
        String cls = this.getClass().getSimpleName();
        System.out.println("Welcome to " + this.name);
        this.askName();
        this.printAppMessage("Hello " + this.username + "\n");
        logger.info("[" + cls + "] started for user: " + this.username);

        while (true) {
            this.printAppMessage("Write the command (type help for a list of commands): ");
            String line = this.input.nextLine();
            String[] parts = line.split("\\s+", 2);
            String cmdIdentifier = parts[0];
            String params = parts.length > 1 ? parts[1] : "";

            try {
                Command cmd = this.commands.get(cmdIdentifier);
                cmd.execute(this, params);
                logger.info("[" + cls + "] executed command: " + cmdIdentifier);
            } catch (RuntimeException e) {
                logger.debug("[" + cls + "] error executing `" + cmdIdentifier + "`: " + e.getLocalizedMessage());
                this.printAppMessage(e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void receive(ProtocolPipeline stack, byte[] data) {
        String cls = this.getClass().getSimpleName();
        if (stack == null || data == null || data.length == 0) {
            logger.error("[" + cls + "] receive: invalid arguments");
            throw new IllegalArgumentException(cls + ".receive: invalid arguments");
        }

        try {
            // 1) UDP decapsulation
            Protocol p1 = stack.pop();
            if (!(p1 instanceof UDPProtocol)) {
                logger.error("[" + cls + "] receive: expected UDP protocol");
                throw new RuntimeException(cls + ".receive: expected UDP protocol");
            }
            UDPProtocol udp = (UDPProtocol) p1;
            byte[] msgFrame = udp.decapsulate(data);

            // 2) MSG decapsulation
            Protocol p2 = stack.pop();
            if (!(p2 instanceof MSGProtocol)) {
                logger.error("[" + cls + "] receive: expected MSG protocol");
                throw new RuntimeException(cls + ".receive: expected MSG protocol");
            }
            MSGProtocol msgProto = (MSGProtocol) p2;
            byte[] payloadBytes = msgProto.decapsulate(msgFrame);

            // 3) Deliver to user
            String sender = msgProto.getUser();
            String message = new String(payloadBytes, java.nio.charset.StandardCharsets.UTF_8);
            this.printAppMessage(sender + ": " + message + "\n");
            logger.info("[" + cls + "] received message from: " + sender);

        } catch (RuntimeException e) {
            logger.debug("[" + cls + "] receive failed: " + e.getLocalizedMessage());
            throw e;
        }
    }

    @Override
    public void send(ProtocolPipeline stack, byte[] data) {
        String cls = this.getClass().getSimpleName();
        if (stack == null || data == null || data.length == 0) {
            logger.error("[" + cls + "] send: invalid arguments");
            throw new IllegalArgumentException(cls + ": invalid arguments");
        }
        if (this.owner == null) {
            logger.error("[" + cls + "] send: owner node is null");
            throw new RuntimeException(cls + ": node is null");
        }

        try {
            int segmentSize = this.owner.getMTU() - 20 - 20; // reserve for headers
            UDPProtocol protocol = new UDPProtocol(
                segmentSize,
                this.owner.randomPort(),
                MSGProtocol.port()
            );
            byte[] encapsulated = protocol.encapsulate(data);
            stack.push(protocol);
            this.owner.send(serverIP, stack, encapsulated);
            logger.info("[" + cls + "] sent message to server " + serverIP.stringRepresentation());
        } catch (RuntimeException e) {
            logger.debug("[" + cls + "] send failed: " + e.getLocalizedMessage());
            throw e;
        }
    }
}