package com.netsim.app.msg.commands;

import java.nio.charset.StandardCharsets;

import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.MSG.MSGProtocol;
import com.netsim.utils.Logger;

public class Send extends Command {
    private static final Logger logger = Logger.getInstance();

    public Send() {
        super("send");
    }

    @Override
    public void execute(App app, String args) {
        String cls = this.getClass().getSimpleName();

        if (args == null || args.isEmpty()) {
            logger.error("[" + cls + "] Message cannot be null or empty");
            throw new IllegalArgumentException(cls + ": message cannot be empty");
        }

        try {
            // 1) build application‐level pipeline
            ProtocolPipeline pipeline = new ProtocolPipeline();

            // 2) wrap the raw text in MSGProtocol
            MSGProtocol msgProto = new MSGProtocol(app.getUsername());
            byte[] raw = args.getBytes(StandardCharsets.UTF_8);
            byte[] encapsulated = msgProto.encapsulate(raw);
            pipeline.push(msgProto);

            // 3) hand off to the client’s send (which will add UDP, IP, DLL, etc)
            app.send(pipeline, encapsulated);

            logger.info("[" + cls + "] Message sent successfully by user " + app.getUsername());
        } catch (RuntimeException e) {
            logger.debug("[" + cls + "] Exception during send: " + e.getLocalizedMessage());
            throw e;
        }
    }

    @Override
    public String help() {
        return "send <message>    Send the given message to the server";
    }
}