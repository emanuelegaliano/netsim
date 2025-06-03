package com.netsim.node;

public interface NetworkNode {
      String getName();
      void send(byte[] data);
      void receive(byte[] data);
}
