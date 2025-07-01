package com.netsim.table;

/**
 * Generic table mapping keys to values, with basic CRUD operations.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface NetworkTable<K, V> {
    // no logger in interfaces per convention

    /**
     * Looks up the value associated with the given key.
     *
     * @param key the lookup key (non-null)
     * @return the value associated with key
     * @throws IllegalArgumentException if key is null
     * @throws NullPointerException     if no entry exists for key
     */
    V lookup(K key) throws IllegalArgumentException, NullPointerException;

    /**
     * Adds or updates a mapping from key to value.
     *
     * @param key   the key to add or update (non-null)
     * @param value the value to associate with key (non-null)
     * @throws IllegalArgumentException if key or value is null
     */
    void add(K key, V value) throws IllegalArgumentException;

    /**
     * Removes the entry for the given key.
     *
     * @param key the key whose mapping should be removed (non-null)
     * @throws IllegalArgumentException if key is null
     * @throws NullPointerException     if no entry exists for key
     */
    void remove(K key) throws IllegalArgumentException, NullPointerException;

    /**
     * Checks whether the table contains no entries.
     *
     * @return true if the table is empty, false otherwise
     */
    boolean isEmpty();
}