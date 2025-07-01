package com.netsim.app.msg;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.netsim.addresses.IPv4;
import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.network.NetworkNode;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.MSG.MSGProtocol;
import com.netsim.protocols.UDP.UDPProtocol;
import com.netsim.utils.Logger;

/**
 * Server‐side application for MSG: receives messages from clients,
 * registers new users, and routes messages to recipients.
 */
public class MsgServer extends App {
    private static final Logger        logger      = Logger.getInstance();
    private static final String        CLS         = MsgServer.class.getSimpleName();

    private final Map<String, IPv4>    users       = new HashMap<>();
    private IPv4                       pendingDest;
    private MSGProtocol                lastMsgProto;

    /**
     * Creates a new MsgServer bound to the given NetworkNode.
     *
     * @param node the server node (non‐null)
     * @throws IllegalArgumentException if node is null
     */
    public MsgServer(NetworkNode node) throws IllegalArgumentException {
        super(
            "msg",
            "<no CLI commands on server>",
            new MsgCommandFactory(),
            node
        );
        if (node == null) {
            String msg = "node cannot be null";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(CLS + ": " + msg);
        }
        logger.info("[" + CLS + "] initialized on node: " + this.getOwner().getName());
    }

    /** No‐op for server; CLI not used. */
    @Override
    public void start() {
        // no‐op
    }

    /**
     * Processes an incoming packet: UDP → MSG → register or route.
     *
     * @param stack the protocol pipeline (non‐null)
     * @param data  the received bytes (non‐null, non‐empty)
     * @throws IllegalArgumentException if arguments invalid
     * @throws RuntimeException         on UDP/MSG errors or routing failures
     */
    @Override
    public void receive(ProtocolPipeline stack, byte[] data)
            throws IllegalArgumentException, RuntimeException {
        logger.info("[" + CLS + "] receive called");
        validateArgs(stack, data);

        byte[] afterUdp = stripUDP(stack, data);
        byte[] afterMsg = stripMSG(stack, afterUdp);

        String user    = lastMsgProto.getUser();
        String payload = new String(afterMsg, StandardCharsets.UTF_8);
        logger.info("[" + CLS + "] message from user \"" + user + "\": " + payload);

        if (!users.containsKey(user)) {
            register(user, payload);
        } else {
            route(user, payload);
        }
    }

    /**
     * Sends data to the previously set pendingDest.
     *
     * @param stack the protocol pipeline (non‐null)
     * @param data  the payload bytes (non‐null, non‐empty)
     * @throws IllegalArgumentException if arguments invalid
     * @throws RuntimeException         if no destination or send failure
     */
    @Override
    public void send(ProtocolPipeline stack, byte[] data)
            throws IllegalArgumentException, RuntimeException {
        logger.info("[" + CLS + "] send called");
        validateArgs(stack, data);
        if (pendingDest == null) {
            String msg = "no destination set";
            logger.error("[" + CLS + "] " + msg);
            throw new RuntimeException("MsgServerApp: " + msg);
        }

        NetworkNode node = getOwner();
        int segmentSize  = node.getMTU() - 20 - 20;
        UDPProtocol udp = new UDPProtocol(
            segmentSize,
            node.randomPort(),
            MSGProtocol.port()
        );
        byte[] udpBytes = udp.encapsulate(data);

        stack.push(udp);
        logger.info("[" + CLS + "] sending UDP to " + pendingDest.stringRepresentation());
        node.send(pendingDest, stack, udpBytes);
        pendingDest = null;
    }

    // ─── internals ─────────────────────────────────────────────────────────────

    /**
     * Validates pipeline and data.
     *
     * @param stack the protocol pipeline
     * @param data  the bytes to validate
     * @throws IllegalArgumentException if stack or data invalid
     */
    private void validateArgs(ProtocolPipeline stack, byte[] data)
            throws IllegalArgumentException {
        if (stack == null || data == null || data.length == 0) {
            String msg = "invalid arguments to receive/send";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException("MsgServerApp: " + msg);
        }
    }

    /**
     * Pops and decapsulates UDP from frame.
     *
     * @param stack the protocol pipeline
     * @param frame the raw frame bytes
     * @return the inner payload bytes
     * @throws RuntimeException if top of stack is not UDPProtocol
     */
    private byte[] stripUDP(ProtocolPipeline stack, byte[] frame) {
        Object p = stack.pop();
        if (!(p instanceof UDPProtocol)) {
            String msg = "expected UDP";
            throw new RuntimeException("MsgServerApp: " + msg);
        }
        return ((UDPProtocol) p).decapsulate(frame);
    }

    /**
     * Pops and decapsulates MSG from UDP payload.
     *
     * @param stack      the protocol pipeline
     * @param udpPayload the UDP payload bytes
     * @return the inner message bytes
     * @throws RuntimeException if top of stack is not MSGProtocol
     */
    private byte[] stripMSG(ProtocolPipeline stack, byte[] udpPayload) {
        Object p = stack.pop();
        if (!(p instanceof MSGProtocol)) {
            String msg = "expected MSG";
            throw new RuntimeException("MsgServerApp: " + msg);
        }
        lastMsgProto = (MSGProtocol) p;
        return lastMsgProto.decapsulate(udpPayload);
    }

    /**
     * Registers a new user and sends confirmation.
     *
     * @param user    the username
     * @param payload the client IP as string
     * @throws RuntimeException if IP parsing or send fails
     */
    private void register(String user, String payload) {
        logger.info("[" + CLS + "] registering user \"" + user + "\" with IP \"" + payload + "\"");
        try {
            int mask = getOwner().getInterfaces().get(0).getIP().getMask();
            IPv4 ip = new IPv4(payload, mask);
            users.put(user, ip);

            ProtocolPipeline pipeline = new ProtocolPipeline();
            MSGProtocol replyProto = new MSGProtocol(getOwner().getName());
            byte[] confirmation = "registrazione effettuata".getBytes(StandardCharsets.UTF_8);
            byte[] framed       = replyProto.encapsulate(confirmation);
            pipeline.push(replyProto);

            pendingDest = ip;
            send(pipeline, framed);

            printAppMessage("Registered " + user + " at " + ip.stringRepresentation() + "\n");
            logger.info("[" + CLS + "] user \"" + user + "\" registered at " + ip.stringRepresentation());
        } catch (Exception e) {
            String msg = "invalid IP format for registration: " + payload;
            logger.error("[" + CLS + "] " + msg);
            logger.debug(e.getLocalizedMessage());
            throw new RuntimeException("MsgServerApp: " + msg, e);
        }
    }

    /**
     * Routes a message from sender to recipient, or responds "utente non trovato".
     *
     * @param sender  the original sender username
     * @param payload the "recipient:message" payload
     */
    private void route(String sender, String payload) {
        logger.info("[" + CLS + "] routing from \"" + sender + "\": " + payload);
        int sep = payload.indexOf(':');
        if (sep < 1) {
            String msg = "malformed payload, missing ':'";
            logger.error("[" + CLS + "] " + msg);
            throw new RuntimeException("MsgServerApp: " + msg);
        }

        String recipient = payload.substring(0, sep);
        String body      = payload.substring(sep + 1);
        IPv4 destIp      = users.get(recipient);

        if (destIp == null) {
            // destinatario non conosciuto → rispondiamo al mittente
            logger.error("[" + CLS + "] unknown recipient: " + recipient);

            IPv4 senderIp = users.get(sender);

            ProtocolPipeline pipeline = new ProtocolPipeline();
            MSGProtocol replyProto = new MSGProtocol(getOwner().getName());
            byte[] errorMsg  = "utente non trovato".getBytes(StandardCharsets.UTF_8);
            byte[] framedErr = replyProto.encapsulate(errorMsg);
            pipeline.push(replyProto);

            pendingDest = senderIp;
            send(pipeline, framedErr);

            logger.info("[" + CLS + "] sent 'utente non trovato' to " + sender);
            return;
        }

        // destinazione valida → inoltro normale
        pendingDest = destIp;
        setUsername(sender);
        logger.info("[" + CLS + "] will forward to " + recipient + "@" + destIp.stringRepresentation());

        Command sendCmd = this.commands.get("send");
        sendCmd.execute(this, body);
    }
}