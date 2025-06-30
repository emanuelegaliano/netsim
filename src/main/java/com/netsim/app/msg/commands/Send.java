package com.netsim.app.msg.commands;

import java.nio.charset.StandardCharsets;

import com.netsim.app.App;
import com.netsim.app.Command;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.MSG.MSGProtocol;
import com.netsim.utils.Logger;

/**
 * Command to send a text message via the MSG application protocol.
 */
public class Send extends Command {
    private static final Logger logger = Logger.getInstance();

    /**
     * Constructs the Send command.
     */
    public Send() {
        super("send");
    }

    /**
     * Executes the send command by encapsulating the message and
     * handing it off to the application's transport stack.
     *
     * @param app  the application context used to send and retrieve username
     * @param args the message text to send
     * @throws IllegalArgumentException if args is null or empty
     * @throws RuntimeException         if an error occurs during encapsulation or sending
     */
    @Override
    public void execute(App app, String args) throws IllegalArgumentException, RuntimeException {
        String cls = this.getClass().getSimpleName();

        if (args == null || args.isEmpty()) {
            String msg = "Message cannot be null or empty";
            logger.error("[" + cls + "] " + msg);
            throw new IllegalArgumentException(cls + ": " + msg);
        }

        try {
            // build application-level protocol pipeline
            ProtocolPipeline pipeline = new ProtocolPipeline();

            // wrap the raw text in MSGProtocol
            MSGProtocol msgProto = new MSGProtocol(this.getUsername(app));
            byte[] raw = args.getBytes(StandardCharsets.UTF_8);
            byte[] encapsulated = msgProto.encapsulate(raw);
            pipeline.push(msgProto);

            // hand off to app.send (adds UDP, IP, DLL, etc.)
            app.send(pipeline, encapsulated);

            logger.info("[" + cls + "] Message sent successfully by user " + this.getUsername(app));
        } catch (RuntimeException e) {
            logger.debug("[" + cls + "] Exception during send: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Returns a brief description of the send command.
     *
     * @return the help text
     */
    @Override
    public String help() {
        return "send <message>    Send the given message to the server";
    }

    /**
     * Retrieves the current username from the application.
     *
     * @param app the application context
     * @return the current username
     */
    private String getUsername(App app) {
        return app.getUsername();
    }
}