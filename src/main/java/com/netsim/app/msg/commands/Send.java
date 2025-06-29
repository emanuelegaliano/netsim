package com.netsim.app.msg.commands;

import java.nio.charset.StandardCharsets;

import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.MSG.MSGProtocol;

/**
 * Send command: wraps the user’s message in the MSGProtocol
 * and delegates to the client’s send(...) method.
 */
public class Send extends Command {
    public Send() {
        super("send");
    }

    /**
     * Execute the send command.
     *
     * @param app  the running MsgClient application
     * @param args the message text to send (non-null, non-empty)
     * @throws IllegalArgumentException if args is null or empty
     * @throws RuntimeException         if app is not a MsgClient
     */
    public void execute(App app, String args) {
        if(args == null || args.isEmpty())
            throw new IllegalArgumentException("send: message cannot be empty");

        // 1) build an application‐level pipeline
        ProtocolPipeline pipeline = new ProtocolPipeline();

        // 2) wrap the raw text in MSGProtocol
        MSGProtocol msgProto = new MSGProtocol(app.getUsername());
        byte[] raw = args.getBytes(StandardCharsets.UTF_8);
        byte[] encapsulated = msgProto.encapsulate(raw);
        pipeline.push(msgProto);

        // 3) hand off to the client’s send (which will add UDP, IP, DLL, etc)
        app.send(pipeline, encapsulated);
    }

    /**
     * @return help text for the send command
     */
    public String help() {
        return "send <message>    Send the given message to the server";
    }
}