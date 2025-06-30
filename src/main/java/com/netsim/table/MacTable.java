package com.netsim.table;

import com.netsim.addresses.Mac;
import com.netsim.network.NetworkAdapter;
import com.netsim.utils.Logger;

import java.util.HashMap;

/**
 * Table mapping MAC addresses to network adapters.
 */
public class MacTable implements NetworkTable<Mac, NetworkAdapter> {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = MacTable.class.getSimpleName();

    private final HashMap<Mac, NetworkAdapter> table;

    public MacTable() {
        this.table = new HashMap<>();
        logger.info("[" + CLS + "] initialized");
    }

    /**
     * Looks up the NetworkAdapter associated with the given Mac key.
     *
     * @param key the Mac address to resolve
     * @return the network adapter if present
     * @throws IllegalArgumentException if key is null
     * @throws NullPointerException     if no entry exists for key
     */
    public NetworkAdapter lookup(Mac key) {
        if (key == null) {
            logger.error("[" + CLS + "] lookup: key cannot be null");
            throw new IllegalArgumentException("MacTable: key cannot be null");
        }
        NetworkAdapter adapter = table.get(key);
        if (adapter == null) {
            logger.error("[" + CLS + "] lookup failed for MAC " + key.stringRepresentation());
            throw new NullPointerException(
                "MacTable: no network adapter associated with: " + key.stringRepresentation()
            );
        }
        logger.info("[" + CLS + "] lookup succeeded for MAC " + key.stringRepresentation() +
                    " -> adapter " + adapter.getName());
        return adapter;
    }

    /**
     * Adds or updates a mapping from Mac to NetworkAdapter.
     *
     * @param address Mac address of new entry
     * @param adapter network adapter of new entry
     * @throws IllegalArgumentException if either address or adapter is null
     */
    public void add(Mac address, NetworkAdapter adapter) {
        if (address == null) {
            logger.error("[" + CLS + "] add: address cannot be null");
            throw new IllegalArgumentException("MacTable: address cannot be null");
        }
        if (adapter == null) {
            logger.error("[" + CLS + "] add: adapter cannot be null");
            throw new IllegalArgumentException("MacTable: adapter cannot be null");
        }
        table.put(address, adapter);
        logger.info("[" + CLS + "] added entry: MAC " + address.stringRepresentation() +
                    " -> adapter " + adapter.getName());
    }

    /**
     * Removes the entry for the given Mac address.
     *
     * @param address Mac address to remove
     * @throws IllegalArgumentException if address is null
     * @throws NullPointerException     if no entry exists for that Mac
     */
    public void remove(Mac address) {
        if (address == null) {
            logger.error("[" + CLS + "] remove: address cannot be null");
            throw new IllegalArgumentException("MacTable: address cannot be null");
        }
        NetworkAdapter removed = table.remove(address);
        if (removed == null) {
            logger.error("[" + CLS + "] remove failed: no adapter for MAC " +
                         address.stringRepresentation());
            throw new NullPointerException(
                "MacTable: no network adapter associated with mac: " +
                address.stringRepresentation()
            );
        }
        logger.info("[" + CLS + "] removed entry for MAC " + address.stringRepresentation());
    }

    /**
     * @return true if the table contains no entries
     */
    public boolean isEmpty() {
        boolean empty = table.isEmpty();
        logger.debug("[" + CLS + "] isEmpty = " + empty);
        return empty;
    }
}