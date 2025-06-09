package com.netsim.node;

import java.util.List;
import java.util.Random;

import com.netsim.addresses.IP;
import com.netsim.addresses.Port;
import com.netsim.networkstack.NetworkAdapter;
import com.netsim.networkstack.ProtocolPipeline;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;
import com.netsim.utils.Logger;

public abstract class NetworkNode {
      protected String name;
      protected List<NetworkAdapter> adapters;
      protected IP ip;
      protected List<Port> openPorts;
      protected RoutingTable routingTable;      

      public String getName() {
            return this.name;
      }

      public void addAdapter(NetworkAdapter newAdapter) throws IllegalArgumentException {
            if(newAdapter == null)
                  throw new IllegalArgumentException(this.getClass().getSimpleName() + ": new adapter cannot be null");

            for(final NetworkAdapter adapter : this.adapters) {
                  if(adapter.getName() == newAdapter.getName())
                        throw new IllegalArgumentException(this.getClass().getSimpleName() + ": new adapter already in node");
            }

            this.adapters.add(newAdapter);
      }

      /**
       * add new route to internal routing table
       * @param subnet the subnet destination
       * @param adapterName the name of the adapter
       * @param nextHop the next node to reach to get to destination
       * @throws IllegalArgumentException if any of the arguments is null, 
       *                                  if subnet or nextHop are broadcast IPs, 
       *                                  if subnet isn't a subnet or nextHop is a subnet
       */
      public void addRoute(IP subnet, String adapterName, IP nextHop) throws IllegalArgumentException {
            if(subnet == null || adapterName == null || nextHop == null)
                  throw new IllegalArgumentException(this.getClass().getSimpleName() + ": arguments must be non-null");
            if(subnet.isBroadcast() || nextHop.isBroadcast()) 
                  throw new IllegalArgumentException(this.getClass().getSimpleName() + ": broadcast cannot be assigned");
            if(!subnet.isSubnet())
                  throw new IllegalArgumentException(this.getClass().getSimpleName() + ": subnet must be a subnet");  
            if(nextHop.isSubnet())
                  throw new IllegalArgumentException(this.getClass().getSimpleName() + ": next hop cannot be a subnet");                      
            boolean found = false;
            int i;
            for(i = 0; i < this.adapters.size() && !found; i++) {
                  if(this.adapters.get(i).getName().equals(adapterName)) 
                        found = true;
            }
            if(!found)
                  throw new IllegalArgumentException(
                        this.getClass().getSimpleName() 
                        + ": adapter " 
                        + adapterName 
                        + " not found"
                  );
            
            // IllegalArgumentException already handled
            try {
                  this.routingTable.add(subnet, new RoutingInfo(this.adapters.get(i), nextHop));
            } catch(RuntimeException e) {
                  Logger.getInstance().error(e.getLocalizedMessage());
            }
      }

      /**
       * Asks routing table to find route using destination
       * @param destination
       * @return
       * @throws IllegalArgumentException
       */
      public RoutingInfo getRoute(IP destination) throws IllegalArgumentException {
            RoutingInfo ri;

            try {
                  ri = this.routingTable.lookup(destination);
            } catch(final NullPointerException e) {
                  Logger.getInstance().info(e.getLocalizedMessage());
                  throw new RuntimeException(this.getClass().getSimpleName() + ": routing info not found");
            }

            return ri;
      }

      public IP getIP() {
            return this.ip;
      }

      /** @return minimum value of MTU from list of adapters */
      public int getMTU() {
            int minMTU = Integer.MAX_VALUE;
            for(NetworkAdapter adapter : this.adapters) {
                  if(adapter.getMTU() < minMTU)
                        minMTU = adapter.getMTU();
            }

            return minMTU;
      }

      /** @return a random port value in range 1024 - 65.535 */
      public int randomPort() {
            Random r = new Random();
            return r.nextInt((Integer.MAX_VALUE - 1024) + 1024);
      }

      public byte[] send(RoutingInfo route, ProtocolPipeline protocols, byte[] data) {
            
      }

      public byte[] receive(RoutingInfo route, ProtocolPipeline protocols, byte[] data) {

      }
}
