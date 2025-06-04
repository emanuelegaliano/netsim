package com.netsim.table;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;
import com.netsim.node.NetworkAdapter;

public class RoutingTableTest {
    private RoutingTable routingTable;
    private IPv4 dest1;
    private IPv4 dest2;
    private IPv4 nextHop1;
    private IPv4 nextHop2;
    private NetworkAdapter adapterA;
    private NetworkAdapter adapterB;

    @Before
    public void setUp() {
        routingTable = new RoutingTable();
        // Example IPv4 addresses
        dest1 = new IPv4("192.168.1.0", 24);
        dest2 = new IPv4("10.0.0.0", 24);
        nextHop1 = new IPv4("192.168.1.254", 32);
        nextHop2 = new IPv4("10.0.0.254", 32);
        // Example adapters
        adapterA = new NetworkAdapter("eth0", 1500, new Mac("aa:bb:cc:00:11:22"));
        adapterB = new NetworkAdapter("eth1", 1500, new Mac("aa:bb:cc:00:33:44"));
    }

    // ----------------------------------------------------
    // Tests for add(...) and lookup(...)
    // ----------------------------------------------------

    @Test
    public void addAndLookupSingleEntry() {
        routingTable.add(dest1, nextHop1, adapterA);
        IPv4 result = routingTable.lookup(dest1, adapterA);
        assertEquals("Lookup should return the same nextHop added", nextHop1, result);
    }

    @Test
    public void addMultipleAdaptersUnderSameDestination() {
        routingTable.add(dest1, nextHop1, adapterA);
        routingTable.add(dest1, nextHop2, adapterB);

        IPv4 r1 = routingTable.lookup(dest1, adapterA);
        IPv4 r2 = routingTable.lookup(dest1, adapterB);

        assertEquals("AdapterA should map to nextHop1", nextHop1, r1);
        assertEquals("AdapterB should map to nextHop2", nextHop2, r2);
    }

    @Test
    public void addDifferentDestinationsSameAdapter() {
        routingTable.add(dest1, nextHop1, adapterA);
        routingTable.add(dest2, nextHop2, adapterA);

        IPv4 r1 = routingTable.lookup(dest1, adapterA);
        IPv4 r2 = routingTable.lookup(dest2, adapterA);

        assertEquals("dest1 should map to nextHop1 on adapterA", nextHop1, r1);
        assertEquals("dest2 should map to nextHop2 on adapterA", nextHop2, r2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void lookupRejectsNullDestination() {
        routingTable.lookup(null, adapterA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void lookupRejectsNullAdapter() {
        routingTable.lookup(dest1, null);
    }

    @Test(expected = NullPointerException.class)
    public void lookupNonExistentDestinationThrows() {
        // No entries added yet → lookup must throw NullPointerException
        routingTable.lookup(dest1, adapterA);
    }

    @Test(expected = NullPointerException.class)
    public void lookupNonExistentAdapterUnderExistingDestinationThrows() {
        routingTable.add(dest1, nextHop1, adapterA);
        // adapterB was never added under dest1 → should throw
        routingTable.lookup(dest1, adapterB);
    }

    // ----------------------------------------------------
    // Tests for remove(...)
    // ----------------------------------------------------

    @Test
    public void removeSingleEntryLeavesTableEmpty() {
        routingTable.add(dest1, nextHop1, adapterA);
        // Remove that exact entry
        routingTable.remove(dest1, nextHop1, adapterA);
        // After removal, lookup on dest1/adapterA must throw
        try {
            routingTable.lookup(dest1, adapterA);
            fail("Expected NullPointerException after removing the only entry");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void removeOneOfMultipleAdaptersUnderSameDestination() {
        routingTable.add(dest1, nextHop1, adapterA);
        routingTable.add(dest1, nextHop2, adapterB);
        // Remove only adapterA entry
        routingTable.remove(dest1, nextHop1, adapterA);
        // adapterA lookup should throw
        try {
            routingTable.lookup(dest1, adapterA);
            fail("Expected NullPointerException for adapterA after removal");
        } catch (NullPointerException e) {
            // expected
        }
        // adapterB should still be present
        IPv4 remaining = routingTable.lookup(dest1, adapterB);
        assertEquals("adapterB entry should still exist", nextHop2, remaining);
    }

    @Test
    public void removeLastAdapterThenDestinationRemoved() {
        routingTable.add(dest1, nextHop1, adapterA);
        routingTable.add(dest1, nextHop2, adapterB);
        // Remove adapterA entry
        routingTable.remove(dest1, nextHop1, adapterA);
        // Remove adapterB entry (last one under dest1)
        routingTable.remove(dest1, nextHop2, adapterB);
        // Now the entire destination dest1 should be gone
        try {
            routingTable.lookup(dest1, adapterB);
            fail("Expected NullPointerException after removing last entry under destination");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeRejectsNullDestination() {
        routingTable.remove(null, nextHop1, adapterA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeRejectsNullNextHop() {
        routingTable.remove(dest1, null, adapterA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeRejectsNullAdapter() {
        routingTable.remove(dest1, nextHop1, null);
    }

    @Test(expected = NullPointerException.class)
    public void removeNonExistentDestinationThrows() {
        // dest1 not in table → should throw NullPointerException
        routingTable.remove(dest1, nextHop1, adapterA);
    }

    @Test
    public void removeNonExistentAdapterUnderExistingDestinationDoesNothing() {
        routingTable.add(dest1, nextHop1, adapterA);
        // adapterB not present under dest1 → this call should not throw, and table remains unchanged
        routingTable.remove(dest1, nextHop2, adapterB);
        // adapterA must still be retrievable
        IPv4 result = routingTable.lookup(dest1, adapterA);
        assertEquals("adapterA entry should remain after attempting to remove non‐existent adapter", nextHop1, result);
    }

    // ----------------------------------------------------
    // Tests for clear() and size()
    // ----------------------------------------------------

    @Test
    public void clearEmptiesTable() {
        routingTable.add(dest1, nextHop1, adapterA);
        routingTable.add(dest2, nextHop2, adapterB);
        assertEquals("Size before clear", 2, routingTable.size());
        routingTable.clear();
        assertEquals("Size after clear should be 0", 0, routingTable.size());
        // Any lookup should now throw
        try {
            routingTable.lookup(dest1, adapterA);
            fail("Expected NullPointerException after clear");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void sizeReflectsNumberOfEntries() {
        assertEquals(0, routingTable.size());
        routingTable.add(dest1, nextHop1, adapterA);
        assertEquals(1, routingTable.size());
        routingTable.add(dest1, nextHop2, adapterB);
        assertEquals(2, routingTable.size());
        routingTable.add(dest2, nextHop1, adapterA);
        assertEquals(3, routingTable.size());
        // Remove one entry
        routingTable.remove(dest1, nextHop1, adapterA);
        assertEquals(2, routingTable.size());
        // Remove another
        routingTable.remove(dest1, nextHop2, adapterB);
        assertEquals(1, routingTable.size());
    }
}