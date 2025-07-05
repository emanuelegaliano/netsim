# Netsim
NetSim is an educational IP network simulator written in Java. It models a simple IP network with hosts, routers, and network links, allowing data to be sent and received between nodes using IPv4 and related protocols. For example, the Node interface in <code>com.netsim.network</code> represents a network node capable of sending and receiving IP packets and the Router class implements an IPv4 router that forwards packets according to its routing table. 

NetSim was developed as a university project for the “Ingegneria del Software” (Software Engineering) course at the Università degli Studi di Catania by Emanuele Galiano (Academic Year 2024/2025).

# Documentation

Full technical documentation is included in the **netsim/docs folder** of this repository. The documentation is written in Italian (the language of the course and project) and covers the design and usage of all components.

# Folder Overview

The code is organized into standard maven project structure:
- **src**: source code folder, that includes java packages and java tests (using JUnit)
- **docs**: documentation folder, with a short documentation and design patterns UML (written in Italian)

# Usage
NetSim uses plain Java packages and does not rely on an external build system. To use the simulator, you simply write your own Java program (with a main method) that creates network nodes, connects their adapters, configures addresses and tables, and attaches applications. The provided classes (in the packages above) can be instantiated directly from any Java code.

Two example programs are included in <code>src/main/java/com/netsim/client</code>: <code>Demo1.java</code> and <code>Demo2.java</code>. These demos show simple network setups: 
1. they create hosts, a router, and a server; 
2. connect them with <code>CabledAdapter</code> links; 
3. assign IP and MAC addresses; 
4. populate ARP and routing tables; 
5. start a messaging application. 


We recommend studying and running these demos first to see how the components fit together. In practice, you can run NetSim by compiling your Java code (along with the NetSim source files) and running your main method, which will use the NetSim classes at runtime.

# Requirements
- JDK installed (at least Java 11)
- Maven is required only for running tests