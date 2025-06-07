package com.netsim.table;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.IP;
import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.networkstack.NetworkAdapter;

public class RoutingTableTest {
    private RoutingTable routingTable;
    private IP dest1;
    private IP dest2;
    private NetworkAdapter adapter1;
    private NetworkAdapter adapter2;
    private RoutingInfo info1;
    private RoutingInfo info2;

    @Before
    public void setUp() {
        routingTable = new RoutingTable();
        dest1     = new IPv4("192.168.1.0", 24);
        dest2     = new IPv4("10.0.0.0", 24);
        adapter1  = new NetworkAdapter("eth0", 1500, new Mac("aa:bb:cc:00:11:22"));
        adapter2  = new NetworkAdapter("eth1", 1500, new Mac("aa:bb:cc:00:33:44"));
        info1     = new RoutingInfo(adapter1, new IPv4("192.168.2.1", 32));
        info2     = new RoutingInfo(adapter2, new IPv4("10.0.0.254", 32));
    }

    // —— Tests for lookup(...) —— //

    @Test(expected = IllegalArgumentException.class)
    public void lookupRejectsNullDestination() {
        routingTable.lookup((IP) null);
    }

    @Test(expected = NullPointerException.class)
    public void lookupNonExistentDestinationThrows() {
        // No entries added yet → should throw NullPointerException
        routingTable.lookup(dest1);
    }

    @Test
    public void lookupExistingRouteReturnsRoutingInfo() {
        routingTable.add(dest1, info1);
        RoutingInfo retrieved = routingTable.lookup(dest1);
        assertSame("lookup should return the exact RoutingInfo instance", info1, retrieved);
    }

    // —— Tests for add(...) —— //

    @Test(expected = IllegalArgumentException.class)
    public void addRejectsNullDestination() {
        routingTable.add(null, info1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRejectsNullRoutingInfo() {
        routingTable.add(dest1, null);
    }

    @Test(expected = RuntimeException.class)
    public void addDuplicateDestinationThrows() {
        routingTable.add(dest1, info1);
        // Second add with same key should throw RuntimeException
        routingTable.add(dest1, info2);
    }

    @Test
    public void addMultipleEntriesIncreasesSize() {
        assertEquals(0, routingTable.size());
        routingTable.add(dest1, info1);
        assertEquals(1, routingTable.size());
        routingTable.add(dest2, info2);
        assertEquals(2, routingTable.size());
    }

    // —— Tests for remove(...) —— //

    @Test(expected = IllegalArgumentException.class)
    public void removeRejectsNullDestination() {
        routingTable.remove((IP) null);
    }

    @Test(expected = NullPointerException.class)
    public void removeNonExistentDestinationThrows() {
        routingTable.remove(dest1);
    }

    @Test
    public void removeExistingDestinationDecreasesSize() {
        routingTable.add(dest1, info1);
        routingTable.add(dest2, info2);
        assertEquals(2, routingTable.size());
        routingTable.remove(dest1);
        assertEquals(1, routingTable.size());
        // Now dest1 should not be found
        try {
            routingTable.lookup(dest1);
            fail("Expected NullPointerException after removing dest1");
        } catch (NullPointerException ignored) {}
        // dest2 still present
        assertSame(info2, routingTable.lookup(dest2));
    }

    // —— Tests for size() and clear() —— //

    @Test
    public void sizeReflectsNumberOfEntries() {
        assertEquals(0, routingTable.size());
        routingTable.add(dest1, info1);
        assertEquals(1, routingTable.size());
        routingTable.add(dest2, info2);
        assertEquals(2, routingTable.size());
        routingTable.remove(dest2);
        assertEquals(1, routingTable.size());
        routingTable.remove(dest1);
        assertEquals(0, routingTable.size());
    }

    @Test
    public void clearEmptiesTable() {
        routingTable.add(dest1, info1);
        routingTable.add(dest2, info2);
        assertEquals(2, routingTable.size());
        routingTable.clear();
        assertEquals(0, routingTable.size());
        // After clear, lookup should throw for any key
        try {
            routingTable.lookup(dest1);
            fail("Expected NullPointerException after clear");
        } catch (NullPointerException ignored) {}
        try {
            routingTable.lookup(dest2);
            fail("Expected NullPointerException after clear");
        } catch (NullPointerException ignored) {}
    }
}