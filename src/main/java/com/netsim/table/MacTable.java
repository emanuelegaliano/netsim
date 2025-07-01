package com.netsim.table;

import java.util.HashMap;

import com.netsim.addresses.Mac;
import com.netsim.network.NetworkAdapter;
import com.netsim.utils.Logger;

/**
 * Table mapping MAC addresses to their corresponding network adapters.
 */
public class MacTable implements NetworkTable<Mac, NetworkAdapter> {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = MacTable.class.getSimpleName();

    private final HashMap<Mac, NetworkAdapter> table;

    /**
     * Initializes an empty MacTable.
     */
    public MacTable() {
        this.table = new HashMap<>();
        logger.info("[" + CLS + "] initialized");
    }

    /**
     * Looks up the NetworkAdapter for the given MAC address.
     *
     * @param key the MAC address to resolve (non-null)
     * @return the associated NetworkAdapter
     * @throws IllegalArgumentException if key is null
     * @throws NullPointerException     if no adapter is found for key
     */
    @Override
    public NetworkAdapter lookup(Mac key) throws IllegalArgumentException, NullPointerException {
        if (key == null) {
            logger.error("[" + CLS + "] lookup: key cannot be null");
            throw new IllegalArgumentException("MacTable: key cannot be null");
        }
        NetworkAdapter adapter = this.table.get(key);
        if (adapter == null) {
            logger.error("[" + CLS + "] lookup failed for MAC " + key.stringRepresentation());
            throw new NullPointerException(
                "MacTable: no network adapter associated with MAC " + key.stringRepresentation()
            );
        }
        logger.info("[" + CLS + "] lookup succeeded for MAC " 
                    + key.stringRepresentation() + " -> adapter " + adapter.getName());
        return adapter;
    }

    /**
     * Adds or updates a mapping from MAC to NetworkAdapter.
     *
     * @param address the MAC address (non-null)
     * @param adapter the NetworkAdapter (non-null)
     * @throws IllegalArgumentException if address or adapter is null
     */
    @Override
    public void add(Mac address, NetworkAdapter adapter) throws IllegalArgumentException {
        if (address == null) {
            logger.error("[" + CLS + "] add: address cannot be null");
            throw new IllegalArgumentException("MacTable: address cannot be null");
        }
        if (adapter == null) {
            logger.error("[" + CLS + "] add: adapter cannot be null");
            throw new IllegalArgumentException("MacTable: adapter cannot be null");
        }
        this.table.put(address, adapter);
        logger.info("[" + CLS + "] added entry: MAC " 
                    + address.stringRepresentation() + " -> adapter " + adapter.getName());
    }

    /**
     * Removes the entry for the specified MAC address.
     *
     * @param address the MAC address to remove (non-null)
     * @throws IllegalArgumentException if address is null
     * @throws NullPointerException     if no entry exists for address
     */
    @Override
    public void remove(Mac address) throws IllegalArgumentException, NullPointerException {
        if (address == null) {
            logger.error("[" + CLS + "] remove: address cannot be null");
            throw new IllegalArgumentException("MacTable: address cannot be null");
        }
        NetworkAdapter removed = this.table.remove(address);
        if (removed == null) {
            logger.error("[" + CLS + "] remove failed: no adapter for MAC " 
                         + address.stringRepresentation());
            throw new NullPointerException(
                "MacTable: no network adapter associated with MAC " + address.stringRepresentation()
            );
        }
        logger.info("[" + CLS + "] removed entry for MAC " + address.stringRepresentation());
    }

    /**
     * Checks if the table contains no entries.
     *
     * @return true if empty, false otherwise
     */
    @Override
    public boolean isEmpty() {
        boolean empty = this.table.isEmpty();
        logger.debug("[" + CLS + "] isEmpty = " + empty);
        return empty;
    }
}