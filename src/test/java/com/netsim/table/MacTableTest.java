package com.netsim.table;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.Mac;
import com.netsim.networkstack.NetworkAdapter;

public class MacTableTest {
    private MacTable macTable;
    private Mac mac1;
    private Mac mac2;
    private NetworkAdapter adapter1;
    private NetworkAdapter adapter2;

    @Before
    public void setUp() {
        macTable = new MacTable();
        mac1 = new Mac("aa:bb:cc:00:11:22");
        mac2 = new Mac("aa:bb:cc:00:33:44");
        adapter1 = new NetworkAdapter("eth0", 1500, new Mac("11:22:33:44:55:66"));
        adapter2 = new NetworkAdapter("eth1", 1500, new Mac("77:88:99:aa:bb:cc"));
    }

    // —— Tests for lookup(...) —— //

    @Test(expected = IllegalArgumentException.class)
    public void lookupRejectsNullKey() {
        macTable.lookup(null);
    }

    @Test(expected = NullPointerException.class)
    public void lookupNonExistentKeyThrows() {
        macTable.lookup(mac1);
    }

    @Test
    public void lookupExistingEntryReturnsAdapter() {
        macTable.add(mac1, adapter1);
        NetworkAdapter result = macTable.lookup(mac1);
        assertSame("lookup should return the exact adapter instance", adapter1, result);
    }

    // —— Tests for add(...) —— //

    @Test(expected = IllegalArgumentException.class)
    public void addRejectsNullAddress() {
        macTable.add(null, adapter1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRejectsNullAdapter() {
        macTable.add(mac1, null);
    }

    @Test
    public void addMultipleEntriesAndLookup() {
        macTable.add(mac1, adapter1);
        macTable.add(mac2, adapter2);
        assertSame(adapter1, macTable.lookup(mac1));
        assertSame(adapter2, macTable.lookup(mac2));
    }

    // —— Tests for remove(...) —— //

    @Test(expected = IllegalArgumentException.class)
    public void removeRejectsNullAddress() {
        macTable.remove(null);
    }

    @Test(expected = NullPointerException.class)
    public void removeNonExistentKeyThrows() {
        macTable.remove(mac1);
    }

    @Test
    public void removeExistingEntryLeavesTableCorrect() {
        macTable.add(mac1, adapter1);
        macTable.add(mac2, adapter2);
        // Remove mac1
        macTable.remove(mac1);
        // lookup mac1 should now throw
        try {
            macTable.lookup(mac1);
            fail("Expected NullPointerException after removing mac1");
        } catch (NullPointerException ignored) {}
        // mac2 should still be present
        assertSame("mac2 should still map to adapter2", adapter2, macTable.lookup(mac2));
    }
}