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

public class MsgServer extends App {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = MsgServer.class.getSimpleName();

    private final Map<String, IPv4> users = new HashMap<>();
    private IPv4 pendingDest;
    private MSGProtocol lastMsgProto;

    /**
     * @param node the Server node hosting this App (non‐null)
     */
    public MsgServer(NetworkNode node) {
        super(
            "msg",
            "<no CLI commands on server>",
            new MsgCommandFactory(),
            node
        );
        logger.info("[" + CLS + "] initialized on node: " + node.getName());
    }

    @Override
    public void start() {
        // no-op for server
    }

    @Override
    public void receive(ProtocolPipeline stack, byte[] data) throws IllegalArgumentException {
        logger.info("[" + CLS + "] receive called");
        validateArgs(stack, data);

        byte[] afterUdp;
        try {
            afterUdp = stripUDP(stack, data);
        } catch (RuntimeException e) {
            logger.error("MsgServerApp: failed to strip UDP");
            logger.debug(e.getLocalizedMessage());
            throw e;
        }

        byte[] afterMsg;
        try {
            afterMsg = stripMSG(stack, afterUdp);
        } catch (RuntimeException e) {
            logger.error("MsgServerApp: failed to strip MSG");
            logger.debug(e.getLocalizedMessage());
            throw e;
        }

        String user = lastMsgProto.getUser();
        String payload = new String(afterMsg, StandardCharsets.UTF_8);
        logger.info("[" + CLS + "] message from user \"" + user + "\": " + payload);

        if (!users.containsKey(user)) {
            register(user, payload);
        } else {
            route(user, payload);
        }
    }

    @Override
    public void send(ProtocolPipeline stack, byte[] data) throws IllegalArgumentException, RuntimeException {
        logger.info("[" + CLS + "] send called");
        validateArgs(stack, data);
        if (pendingDest == null) {
            logger.error("[" + CLS + "] no destination set");
            throw new RuntimeException("MsgServerApp: no destination set");
        }
        NetworkNode node = getOwner();

        // wrap in UDP
        int segmentSize = node.getMTU() - 20 - 20;
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

    private void validateArgs(ProtocolPipeline stack, byte[] data) {
        if (stack == null || data == null || data.length == 0) {
            logger.error("[" + CLS + "] invalid arguments to receive/send");
            throw new IllegalArgumentException("MsgServerApp: invalid arguments");
        }
    }

    private byte[] stripUDP(ProtocolPipeline stack, byte[] frame) {
        Object p = stack.pop();
        if (!(p instanceof UDPProtocol)) {
            throw new RuntimeException("MsgServerApp: expected UDP");
        }
        return ((UDPProtocol) p).decapsulate(frame);
    }

    private byte[] stripMSG(ProtocolPipeline stack, byte[] udpPayload) {
        Object p = stack.pop();
        if (!(p instanceof MSGProtocol)) {
            throw new RuntimeException("MsgServerApp: expected MSG");
        }
        lastMsgProto = (MSGProtocol) p;
        return lastMsgProto.decapsulate(udpPayload);
    }

    private void register(String user, String payload) {
        logger.info("[" + CLS + "] registering user \"" + user + "\" with IP \"" + payload + "\"");
        try {
            int mask = getOwner().getInterfaces().get(0).getIP().getMask();
            IPv4 ip = new IPv4(payload, mask);
            users.put(user, ip);
            // inform the user on the wire
            ProtocolPipeline pipeline = new ProtocolPipeline();
            // header with server’s node name
            MSGProtocol replyProto = new MSGProtocol(getOwner().getName());
            byte[] confirmation = "registrazione effettuata".getBytes(StandardCharsets.UTF_8);
            byte[] framed = replyProto.encapsulate(confirmation);
            pipeline.push(replyProto);

            // delegate to send(), which wraps UDP/IP/etc.
            pendingDest = ip;
            send(pipeline, framed);

            // local feedback
            this.printAppMessage("Registered " + user + " at " + ip.stringRepresentation() + "\n");
            logger.info("[" + CLS + "] user \"" + user + "\" registered at " + ip.stringRepresentation());
        } catch (Exception e) {
            logger.error("[" + CLS + "] invalid IP format for registration: " + payload);
            logger.debug(e.getLocalizedMessage());
            throw new RuntimeException(
                "MsgServerApp: invalid IP format for registration: " + payload
            );
        }
    }

    private void route(String sender, String payload) {
        logger.info("[" + CLS + "] routing from \"" + sender + "\": " + payload);
        int sep = payload.indexOf(':');
        if (sep < 1) {
            logger.error("[" + CLS + "] malformed payload, missing ':'");
            throw new RuntimeException("MsgServerApp: expected \"recipient:message\"");
        }
        String recipient = payload.substring(0, sep);
        String body      = payload.substring(sep + 1);

        IPv4 destIp = users.get(recipient);
        if (destIp == null) {
            logger.error("[" + CLS + "] unknown recipient: " + recipient);
            throw new RuntimeException("MsgServerApp: unknown recipient: " + recipient);
        }

        pendingDest = destIp;
        setUsername(sender);
        logger.info("[" + CLS + "] will forward to " + recipient + "@" + destIp.stringRepresentation());

        // delegate to the “send” command, which calls our send(...)
        Command sendCmd = commands.get("send");
        sendCmd.execute(this, body);
    }
}