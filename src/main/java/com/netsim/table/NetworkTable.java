package com.netsim.table;

import com.netsim.addresses.Address;

public interface NetworkTable<Key extends Address, Value extends Address, Device> {
      Value lookup(Key key);
      void add(Key key, Value value, Device device);
      void remove(Key key, Value value);
}
