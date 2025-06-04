package com.netsim.table;

import java.util.HashMap;

import com.netsim.addresses.Mac;

import com.netsim.node.NetworkAdapter;

public class MacTable implements NetworkTable<Mac, NetworkAdapter> {
      private HashMap<Mac, NetworkAdapter> table;

      public MacTable() {
            this.table = new HashMap<>();
      }

      /**
       * @param key the Mac address to resolve
       * @return the physical port associated with that mac address
       * @throws IllegalArgumentException if key is null
       * @throws NullPointerException if no entry exist for key
       */
      public NetworkAdapter lookup(Mac key) throws IllegalArgumentException, NullPointerException {
            if(key == null)
                  throw new IllegalArgumentException("MacTable: key cannot be null");

            NetworkAdapter adapter = this.table.get(key);
            if(adapter == null)
                  throw new NullPointerException("MacTable: no network adapter associated with: " + key.stringRepresentation());

            return adapter;
      }     

      /**
       * @param address mac address of new entry
       * @param adapter network adapter of new entry
       * @throws IllegalArgumentException if either address or port is null
       */
      public void add(Mac address, NetworkAdapter adapter) throws IllegalArgumentException {
            if(address == null)
                  throw new IllegalArgumentException("MacTable: address cannot be null");
            if(adapter == null)
                  throw new IllegalArgumentException("MacTable: adapter cannot be null");
            
            this.table.put(address, adapter);
      }     

      public void remove(Mac address) throws IllegalArgumentException, NullPointerException {
            if(address == null)
                  throw new IllegalArgumentException("MacTable: address cannot be null");

            NetworkAdapter portCheck = this.table.remove(address);
            if(portCheck == null)
                  throw new NullPointerException(
                        "MacTable: no network adapter associated with mac: " + address.stringRepresentation() 
                  );
      }
}
