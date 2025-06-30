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

/**
 * Message‐server application driving a {@link com.netsim.network.server.Server}.
 * <p>
 * On first contact from a username, registers their IP.
 * Thereafter parses “recipient:message” payloads and forwards them.
 * </p>
 */
public class MsgServer extends App {
    private final Map<String,IPv4> users = new HashMap<>();
    private IPv4 pendingDest;

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
    }

    public void start() {
        // usesless method here
    }

    public void receive(ProtocolPipeline stack, byte[] data) throws IllegalArgumentException {
        this.validateArgs(stack, data);
        byte[] afterUdp = stripUDP(stack, data);
        byte[] afterMsg = stripMSG(stack, afterUdp);

        // extract user and raw payload
        String user    = lastMsgProto.getUser();
        String payload = new String(afterMsg, StandardCharsets.UTF_8);

        if(!this.users.containsKey(user)) {
            this.register(user, payload);
        } else {
            this.route(user, payload);
        }
    }

    public void send(ProtocolPipeline stack, byte[] data) throws IllegalArgumentException, RuntimeException {
        this.validateArgs(stack, data);
        if(pendingDest == null)
            throw new RuntimeException("MsgServerApp: no destination set");
        NetworkNode node = getOwner();

        // wrap in UDP
        int segmentSize = node.getMTU()
                         - 20   // msg protocol header
                         - 20;  // udp protocol header
        UDPProtocol udp = new UDPProtocol(
            segmentSize,
            node.randomPort(),
            MSGProtocol.port()
        );
        byte[] udpBytes = udp.encapsulate(data);
        stack.push(udp);

        // delegate to network‐server
        node.send(pendingDest, stack, udpBytes);
        pendingDest = null;
    }

    // ─── internals ─────────────────────────────────────────────────────────────

    private void validateArgs(ProtocolPipeline stack, byte[] data) throws IllegalArgumentException {
        if (stack == null || data == null || data.length == 0)
            throw new IllegalArgumentException("MsgServerApp: invalid arguments");
    }

    private byte[] stripUDP(ProtocolPipeline stack, byte[] frame) {
        Object p = stack.pop();
        if (!(p instanceof UDPProtocol))
            throw new RuntimeException("MsgServerApp: expected UDP");
        return ((UDPProtocol) p).decapsulate(frame);
    }

    private MSGProtocol lastMsgProto;
    private byte[] stripMSG(ProtocolPipeline stack, byte[] udpPayload) {
        Object p = stack.pop();
        if (!(p instanceof MSGProtocol))
            throw new RuntimeException("MsgServerApp: expected MSG");
        lastMsgProto = (MSGProtocol) p;
        return lastMsgProto.decapsulate(udpPayload);
    }

    private void register(String user, String payload) throws RuntimeException {
        try {
            // reuse mask of first local interface
            int mask = getOwner().getInterfaces().get(0).getIP().getMask();
            IPv4 ip = new IPv4(payload, mask);
            this.users.put(user, ip);
            this.printAppMessage("Registered " + user + " at " + ip.stringRepresentation() + "\n");
        } catch (final Exception e) {
            throw new RuntimeException(
                "MsgServerApp: invalid IP format for registration: " + payload
            );
        }
    }

    private void route(String sender, String payload) throws RuntimeException {
        int sep = payload.indexOf(':');
        if (sep < 1)
            throw new RuntimeException("MsgServerApp: expected \"recipient:message\"");
        String recipient = payload.substring(0, sep);
        String body      = payload.substring(sep + 1);

        IPv4 destIp = users.get(recipient);
        if (destIp == null)
            throw new RuntimeException("MsgServerApp: unknown recipient: " + recipient);

        pendingDest = destIp;
        setUsername(sender);
        // delegate to the “send” command, which will call our send(...)
        Command sendCmd = commands.get("send");
        sendCmd.execute(this, body);
    }
}
