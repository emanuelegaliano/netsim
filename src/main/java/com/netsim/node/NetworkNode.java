package com.netsim.node;

import java.util.List;

import com.netsim.addresses.IP;
import com.netsim.networkstack.NetworkAdapter;
import com.netsim.table.RoutingInfo;
import com.netsim.table.RoutingTable;
import com.netsim.utils.Logger;

public class NetworkNode {
      protected String name;
      protected List<NetworkAdapter> adapters;
      protected List<IP> IPs;
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


      public void addIP(IP newIP) throws IllegalArgumentException {
            if(newIP == null)
                  throw new IllegalArgumentException(this.getClass().getSimpleName() + ": new IP cannot be null");

            for(final IP ip : this.IPs) {
                  if(ip.equals(newIP))
                        throw new IllegalArgumentException(this.getClass().getSimpleName() + ": new IP already a node IP");
            }

            this.IPs.add(newIP);
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
}
