package com.netsim.app.msg;

import java.nio.charset.StandardCharsets;
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

/**
 * A client application that sends and receives MSG messages
 * over UDP/IP from a specified server.
 */
public class MsgClient extends App {
    private final Logger              logger   = Logger.getInstance();
    private final Scanner             input;
    private final IPv4                serverIP;
    private static final String       CLS       = MsgClient.class.getSimpleName();

    /**
     * Constructs a MsgClient bound to a NetworkNode and server IP.
     *
     * @param node      the underlying network node (non-null)
     * @param serverIP  the IPv4 address of the server (non-null)
     * @throws IllegalArgumentException if serverIP is null
     */
    public MsgClient(NetworkNode node, IPv4 serverIP) {
        super("msg",
              "<command> <parameters> (print help for a list of commands)",
              new MsgCommandFactory(),
              node);
        if (serverIP == null) {
            String msg = "invalid server IP: null";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(CLS + ": " + msg);
        }
        this.serverIP = serverIP;
        this.input    = new Scanner(System.in);
        logger.info("[" + CLS + "] initialized for server " + this.serverIP.stringRepresentation());
    }

    /**
     * Prompts the user for their username.
     */
    public void askName() {
        this.printAppMessage("Tell me your name: ");
        this.setUsername(this.input.nextLine());
    }

    /**
     * Starts the interactive command loop of the MSG client.
     */
    @Override
    public void start() {
        System.out.println("Welcome to " + this.name);
        this.askName();
        this.printAppMessage("Hello " + this.username + "\n");
        logger.info("[" + CLS + "] started for user: " + this.username);

        while (true) {
            this.printAppMessage("Write the command (type help for a list of commands): ");
            String line = this.input.nextLine();
            String[] parts = line.split("\\s+", 2);
            String cmdIdentifier = parts[0];
            String params        = parts.length > 1 ? parts[1] : "";

            try {
                Command cmd = this.commands.get(cmdIdentifier);
                cmd.execute(this, params);
                logger.info("[" + CLS + "] executed command: " + cmdIdentifier);
            } catch (RuntimeException e) {
                logger.debug("[" + CLS + "] error executing `" + cmdIdentifier + "`: " + e.getLocalizedMessage());
                this.printAppMessage(e.getLocalizedMessage());
            }
        }
    }

    /**
     * Handles incoming frames: UDP → MSG → deliver to user.
     *
     * @param stack the protocol pipeline (non-null)
     * @param data  the raw frame bytes (non-null, non-empty)
     * @throws IllegalArgumentException if arguments are invalid
     * @throws RuntimeException         if protocol mismatch or decapsulation fails
     */
    @Override
    public void receive(ProtocolPipeline stack, byte[] data) throws IllegalArgumentException, RuntimeException {
        if (stack == null || data == null || data.length == 0) {
            String msg = "receive: invalid arguments";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(CLS + ".receive: " + msg);
        }

        try {
            // 1) UDP decapsulation
            Protocol p1 = stack.pop();
            if (!(p1 instanceof UDPProtocol)) {
                String msg = "receive: expected UDP protocol";
                logger.error("[" + CLS + "] " + msg);
                throw new RuntimeException(CLS + ".receive: " + msg);
            }
            UDPProtocol udpProto = (UDPProtocol) p1;
            byte[] msgFrame = udpProto.decapsulate(data);

            // 2) MSG decapsulation
            Protocol p2 = stack.pop();
            if (!(p2 instanceof MSGProtocol)) {
                String msg = "receive: expected MSG protocol";
                logger.error("[" + CLS + "] " + msg);
                throw new RuntimeException(CLS + ".receive: " + msg);
            }
            MSGProtocol msgProto = (MSGProtocol) p2;
            byte[] payloadBytes = msgProto.decapsulate(msgFrame);

            // 3) deliver to user
            String sender  = msgProto.getUser();
            String message = new String(payloadBytes, StandardCharsets.UTF_8);
            this.printAppMessage(sender + ": " + message + "\n");
            logger.info("[" + CLS + "] received message from: " + sender);

        } catch (RuntimeException e) {
            logger.debug("[" + CLS + "] receive failed: " + e.getLocalizedMessage());
            throw e;
        }
    }

    /**
     * Sends data to the configured server via UDP & IP.
     *
     * @param stack the protocol pipeline (non-null)
     * @param data  the application payload (non-null, non-empty)
     * @throws IllegalArgumentException if arguments are invalid
     * @throws RuntimeException         if sending fails
     */
    @Override
    public void send(ProtocolPipeline stack, byte[] data) throws IllegalArgumentException, RuntimeException {
        if (stack == null || data == null || data.length == 0) {
            String msg = "send: invalid arguments";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(CLS + ": " + msg);
        }
        if (this.owner == null) {
            String msg = "send: owner node is null";
            logger.error("[" + CLS + "] " + msg);
            throw new RuntimeException(CLS + ": " + msg);
        }

        try {
            int segmentSize = this.owner.getMTU() - 20 - 20; // reserve IPv4 + UDP headers
            UDPProtocol udpProto = new UDPProtocol(
                segmentSize,
                this.owner.randomPort(),
                MSGProtocol.port()
            );
            byte[] encapsulated = udpProto.encapsulate(data);

            stack.push(udpProto);
            this.owner.send(this.serverIP, stack, encapsulated);
            logger.info("[" + CLS + "] sent message to server " + this.serverIP.stringRepresentation());

        } catch (RuntimeException e) {
            logger.debug("[" + CLS + "] send failed: " + e.getLocalizedMessage());
            throw e;
        }
    }
}