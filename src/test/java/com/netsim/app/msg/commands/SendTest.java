package com.netsim.app.msg.commands;

import com.netsim.app.App;
import com.netsim.app.CommandFactory;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.protocols.MSG.MSGProtocol;

import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class SendTest {
    private Send sendCmd;
    private DummyApp dummyApp;

    @Before
    public void setUp() {
        sendCmd = new Send();
        dummyApp = new DummyApp();
    }

    @Test
    public void testExecuteEncapsulatesMessage() {
        String message = "Hello from test!";
        sendCmd.execute(dummyApp, message);

        ProtocolPipeline pipeline = dummyApp.lastStack;
        byte[] payload = dummyApp.lastData;

        assertNotNull(pipeline);
        assertNotNull(payload);
        assertFalse(pipeline.isEmpty());
        assertTrue(pipeline.peek() instanceof MSGProtocol);

        MSGProtocol proto = (MSGProtocol) pipeline.peek();
        assertEquals("testuser", proto.getUser());

        String decoded = new String(proto.decapsulate(payload), StandardCharsets.UTF_8);
        assertEquals(message, decoded);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteNullArgsThrows() {
        sendCmd.execute(dummyApp, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteEmptyArgsThrows() {
        sendCmd.execute(dummyApp, "");
    }

    // ────────────────────────────────────────────────────────────────────────────────

    private static class DummyApp extends App {
        ProtocolPipeline lastStack;
        byte[] lastData;

        public DummyApp() {
            super("testApp", "usage", new DummyFactory(), null);
            this.setUsername("testuser");
        }

        @Override
        public void start() {}

        @Override
        public void send(ProtocolPipeline stack, byte[] data) {
            this.lastStack = stack;
            this.lastData = data;
        }

        @Override
        public void receive(ProtocolPipeline stack, byte[] data) {
            // not needed for Send test
        }
    }

    private static class DummyFactory implements CommandFactory {
        @Override
        public com.netsim.app.Command get(String cmd) {
            return null; // not relevant for this test
        }
    }
}