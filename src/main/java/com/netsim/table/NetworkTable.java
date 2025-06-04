package com.netsim.table;

import com.netsim.node.NetworkAdapter;

public interface NetworkTable<Key, Value> {
      /**
       * @param key address used as key in the table
       * @param device the network adapter
       * @return address result from table using key
       */
      Value lookup(Key key, NetworkAdapter device);
      /**
       * @param key address used as key in the table
       * @param value return value of the table
       * @param device the network adapter
       */
      void add(Key key, Value value, NetworkAdapter device);
      /**
       * @param key 
       * @param value
       */
      void remove(Key key, Value value, NetworkAdapter device);
}
