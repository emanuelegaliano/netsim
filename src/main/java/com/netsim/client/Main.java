package com.netsim.client;

import com.netsim.app.netsimmsg.NetsimMsg;
import com.netsim.node.NetworkNode;

public class Main {
      public static void main(String[] args) {
            NetworkNode node = new NetworkNode();
            NetsimMsg app = new NetsimMsg(node);
            app.start();
      }
}
