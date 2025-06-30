package com.netsim.app.msg;

import com.netsim.app.Command;
import com.netsim.app.msg.commands.Help;
import com.netsim.app.msg.commands.Send;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MsgCommandFactoryTest {
      private MsgCommandFactory factory;

      @Before
      public void setUp() {
            factory = new MsgCommandFactory();
      }

      @Test
      public void testGetHelpCommand() {
            Command cmd = factory.get("help");
            assertNotNull(cmd);
            assertTrue(cmd instanceof Help);
      }

      @Test
      public void testGetSendCommand() {
            Command cmd = factory.get("send");
            assertNotNull(cmd);
            assertTrue(cmd instanceof Send);
      }

      @Test(expected = IllegalArgumentException.class)
      public void testGetUnknownCommandThrows() {
            factory.get("unknown");
      }
}
