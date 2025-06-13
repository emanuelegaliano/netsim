package com.netsim.table;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Mac;

public class ArpTableTest {
    private ArpTable arpTable;
    private IPv4 ip1;
    private IPv4 ip2;
    private Mac mac1;
    private Mac mac2;

    @Before
    public void setUp() {
        arpTable = new ArpTable();
        ip1 = new IPv4("192.168.0.10", 32);
        ip2 = new IPv4("10.0.0.5",     32);
        mac1 = new Mac("aa:bb:cc:00:11:22");
        mac2 = new Mac("aa:bb:cc:00:33:44");
    }

    // —— Tests for add(...) and lookup(...) —— //

    @Test
    public void addAndLookupSingleEntry() {
        arpTable.add(ip1, mac1);
        Mac result = arpTable.lookup(ip1);
        assertEquals("Lookup should return the same Mac that was added", mac1, result);
    }

    @Test
    public void addThenUpdateEntry() {
        arpTable.add(ip1, mac1);
        Mac first = arpTable.lookup(ip1);
        assertEquals(mac1, first);

        // Overwrite the entry at ip1
        arpTable.add(ip1, mac2);
        Mac second = arpTable.lookup(ip1);
        assertEquals("After update, lookup should return new Mac", mac2, second);
    }

    @Test
    public void addMultipleDistinctEntries() {
        arpTable.add(ip1, mac1);
        arpTable.add(ip2, mac2);

        Mac r1 = arpTable.lookup(ip1);
        Mac r2 = arpTable.lookup(ip2);

        assertEquals("ip1 should map to mac1", mac1, r1);
        assertEquals("ip2 should map to mac2", mac2, r2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRejectsNullKey() {
        arpTable.add(null, mac1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRejectsNullValue() {
        arpTable.add(ip1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void lookupRejectsNullKey() {
        arpTable.lookup(null);
    }

    @Test(expected = NullPointerException.class)
    public void lookupNonExistentKeyThrows() {
        // No entries added yet → should throw NullPointerException
        arpTable.lookup(ip1);
    }

    // —— Tests for remove(...) —— //

    @Test
    public void removeSingleEntryLeavesTableEmpty() {
        arpTable.add(ip1, mac1);
        arpTable.remove(ip1);

        try {
            arpTable.lookup(ip1);
            fail("Expected NullPointerException after removing the only entry");
        } catch (NullPointerException ignored) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeRejectsNullKey() {
        arpTable.remove(null);
    }

    @Test(expected = NullPointerException.class)
    public void removeNonExistentKeyThrows() {
        arpTable.remove(ip1);
    }

    @Test
    public void removeNonExistentEntryDoesNothingIfKeyPresentButValueWasDifferent() {
        // First add ip1→mac1
        arpTable.add(ip1, mac1);
        // Now remove ip1 even though mac doesn't matter in new signature
        // so actually remove will delete ip1
        arpTable.remove(ip1);
        try {
            arpTable.lookup(ip1);
            fail("Expected NullPointerException because entry was removed");
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void removeOneOfMultipleEntries() {
        arpTable.add(ip1, mac1);
        arpTable.add(ip2, mac2);

        // Remove only ip1
        arpTable.remove(ip1);
        try {
            arpTable.lookup(ip1);
            fail("Expected NullPointerException for ip1 after removal");
        } catch (NullPointerException ignored) {
        }
        // ip2 should still be present
        Mac remaining = arpTable.lookup(ip2);
        assertEquals("ip2 entry should still exist", mac2, remaining);
    }
}
