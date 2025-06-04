package com.netsim.table;

public interface NetworkTable<Key, Value> {
      /**
       * @param key address used as key in the table
       * @param device the network adapter
       * @return address result from table using key
       */
      Value lookup(Key key);
      
      /**
       * @param key address used as key in the table
       * @param value return value of the table
       * @param device the network adapter
       */
      void add(Key key, Value value);

      /** @param key key that will be removed */
      void remove(Key key);
}

