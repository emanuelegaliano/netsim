package com.netsim.network;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.networkstack.ProtocolPipeline;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NetworkAdapterTest {

    private Mac mac1;
    private Mac mac2;
    private NetworkAdapter adapter1;
    private NetworkAdapter adapter2;

    @Before
    public void setup() {
        mac1 = new Mac("aa:bb:cc:11:22:33");
        mac2 = new Mac("aa:bb:cc:44:55:66");
        adapter1 = new NetworkAdapter("eth0", 1500, mac1);
        adapter2 = new NetworkAdapter("eth1", 1500, mac2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullName() {
        new NetworkAdapter(null, 1500, mac1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullMac() {
        new NetworkAdapter("ethX", 1500, null);
    }

    @Test
    public void getPropertiesWorkCorrectly() {
        assertEquals("eth0", adapter1.getName());
        assertEquals(1500, adapter1.getMTU());
        assertEquals(mac1, adapter1.getMacAddress());
        assertTrue(adapter1.isUp());
    }

    @Test
    public void equalsAndHashCodeBasedOnMac() {
        NetworkAdapter clone = new NetworkAdapter("eth9", 1500, new Mac("aa:bb:cc:11:22:33"));
        assertEquals(adapter1, clone);
        assertEquals(adapter1.hashCode(), clone.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOwnerRejectsNull() {
        adapter1.setOwner(null);
    }

    @Test(expected = NullPointerException.class)
    public void getNodeThrowsIfNotSet() {
        adapter1.getNode();
    }

    @Test
    public void setAndGetOwnerWorks() {
        Node mockNode = new Node() {
            public void receive(ProtocolPipeline stack, byte[] pdu) {}
            public void send(IPv4 ip, ProtocolPipeline stack, byte[] pdu) {}  
        };
        adapter1.setOwner(mockNode);
        assertEquals(mockNode, adapter1.getNode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setRemoteAdapterRejectsNull() {
        adapter1.setRemoteAdapter(null);
    }

    @Test(expected = NullPointerException.class)
    public void getLinkedAdapterFailsIfNotLinked() {
        adapter1.getLinkedAdapter();
    }

    @Test
    public void setAndGetRemoteAdapterWorks() {
        adapter1.setRemoteAdapter(adapter2);
        assertEquals(adapter2, adapter1.getLinkedAdapter());
    }

    @Test
    public void setUpAndSetDownChangeStatus() {
        adapter1.setDown();
        assertFalse(adapter1.isUp());
        adapter1.setUp();
        assertTrue(adapter1.isUp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendRejectsNullArgs() {
        adapter1.send(null, null);
    }

    @Test(expected = RuntimeException.class)
    public void sendFailsIfAdapterDown() {
        adapter1.setRemoteAdapter(adapter2);
        adapter1.setDown();
        adapter1.send(new ProtocolPipeline(), new byte[]{1, 2, 3});
    }

    @Test(expected = RuntimeException.class)
    public void receiveFailsIfOwnerNotSet() {
        adapter1.setRemoteAdapter(adapter2);
        ProtocolPipeline stack = new ProtocolPipeline();
        adapter1.receive(stack, new byte[]{0, 0, 0, 0});
    }

    // Further testing send/receive interaction requires full protocol stack simulation,
    // which would be best tested as integration/system tests.

}