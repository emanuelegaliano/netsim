# Netsim

> Netsim è un simulatore di **topologie di rete**, realizzato in Java e organizzato come **progetto Maven**, il cui scopo principale è **fornire un’architettura modulare ed estensibile** per la **sperimentazione di protocolli** di rete, componenti infrastrutturali e **meccanismi di comunicazione** tra nodi.
> 

# 0. Analisi dei requisiti

## 0.1 Requisiti Funzionali

### RF1: Creazione e Configurazione della Rete

- **RF1.1:** L'utente deve poter creare nodi di rete quali Host, Router e Server.
- **RF1.2:** L'utente deve poter collegare i nodi tramite adattatori (NetworkAdapter) simulando connessioni fisiche.
- **RF1.3:** Il sistema deve permettere la configurazione dei vari indirizzi nei nodi.

### RF2: Routing dei Pacchetti

- **RF2.1:** I Router devono inoltrare i pacchetti IPv4 in base a una tabella di routing configurabile.
- **RF2.2:** Host e Server devono gestire la ricezione e invio di pacchetti IPv4 tramite le loro interfacce.
- **RF2.3:** Il sistema deve implementare il protocollo IPv4 per la gestione degli header IP, incluse operazioni di encapsulamento e decapsulamento.

### RF3: Applicazioni e Comunicazioni

- **RF3.1:** I nodi di tipo Host e Server devono poter eseguire applicazioni utente per inviare e ricevere messaggi.
- **RF3.2:** Deve essere presente almeno un'applicazione esempio per dimostrare la comunicazione end-to-end.
- **RF3.3:** Le applicazioni devono comunicare tramite protocolli di trasporto simulati come UDP.

> *NB: E’ stato utilizzato UDP e in generale protocolli inaffidabili per 2 motivi:
1. Lo scopo principale era quello di simulare una rete in modo semplice
2. La base di partenza era Java che forniva una comunicazione affidabile.*
> 

## 0.2 Requisiti Non Funzionali

### RNF1: Usabilità

- **RNF1.1:** Il sistema deve fornire messaggi di logging chiari e informativi per facilitare la comprensione delle azioni eseguite.

### RNF2: Affidabilità e Robustezza

- **RNF2.1:** Il sistema deve gestire correttamente input errati fornendo feedback precisi e appropriati.
- **RNF2.2:** Deve essere garantita la consistenza interna del simulatore durante operazioni di configurazione dinamica della rete.

### RNF3: Manutenzione ed Estendibilità

- **RNF3.1:** Il codice deve essere strutturato in maniera modulare con chiara separazione delle responsabilità tra classi e package.
- **RNF3.2:** Deve essere semplice aggiungere nuove tipologie di nodi, protocolli e applicazioni senza modifiche invasive.

### RNF4: Prestazioni

- **RNF4.1:** La simulazione deve essere sufficientemente performante per scenari didattici con un numero limitato di nodi (tipicamente < 100).
- **RNF4.2:** Le operazioni di invio e ricezione devono essere eseguite in tempi ragionevoli per garantire una risposta fluida alle interazioni dell'utente.

# 0.3 Strumenti utilizzati

## Strumenti Java

![image.png](Netsim%201eb9ef2c589080c790c1de176a865d47/image.png)

## Framework

![image.png](Netsim%201eb9ef2c589080c790c1de176a865d47/image%201.png)

## Suite di Test

![netsim/pom.xml](Netsim%201eb9ef2c589080c790c1de176a865d47/image%202.png)

netsim/pom.xml

# 1. Descrizione delle classi

## 1.1 Package addresses

### Address

La classe `Address` è una classe astratta che rappresenta un indirizzo di rete generico. Implementa un array di byte di lunghezza fissa e fornisce la conversione tra la forma testuale e quella binaria dell'indirizzo. Fornisce metodi comuni come byteRepresentation() e stringRepresentation(), ridefinendo equals() e hashCode() per permettere il confronto corretto tra indirizzi. In sostanza, funge da base per classi specifiche di indirizzo (come `IPv4` o `MAC`) incapsulando la logica di parsing e validazione degli indirizzi. 

Non esistono test specifici per `Address`, in quanto è una classe astratta; i test relativi agli indirizzi `IPv4` o `MAC` verificano indirettamente la correttezza del funzionamento ereditato da `Address`.

### IP

La classe astratta `IP` estende Address aggiungendo il concetto di maschera di sottorete. Gestisce un indirizzo `IP`con un prefisso di rete, consentendo operazioni come setMask() o isInSubnet(): quest'ultimo verifica se l'indirizzo appartiene a una rete specificata da un indirizzo di rete e un prefisso. Definisce inoltre metodi astratti che le sottoclassi concrete (come `IPv4`) devono implementare per classificare l'indirizzo (es. isLoopback() , isMulticast() , ecc.). In pratica, IP fornisce funzionalità comuni per la gestione degli indirizzi IP e delle maschere di rete.

Non ci sono test diretti per la classe `IP`; le funzionalità ereditate da questa classe vengono verificate principalmente nei test di `IPv4`, che mette alla prova i metodi di rete relativi ai sottoreti e alle categorie di indirizzi.

### IPv4

La classe `IPv4`estende `IP` implementando un indirizzo `IPv4`concreto. Supporta la creazione a
partire da una stringa in notazione puntata, con maschera specificata come prefisso o come stringa. Fornisce implementazioni dei metodi di classificazione di IP: ad esempio rileva se l'indirizzo è di loopback, multicast, broadcast, privato, link-local o non specificato, secondo le regole di `IPv4`. Ha un metodo subnetBroadcast() che calcola l'indirizzo broadcast della propria sottorete. In sostanza, gestisce la sintassi e i calcoli tipici degli indirizzi `IPv4`. 

Il test `IPv4Test`verifica le funzionalità di `IPv4`: controlla che la stringa venga interpretata correttamente, che isInSubnet() identifichi correttamente la vicinanza in sottoreti, e i metodi di
classificazione (loopback, multicast, broadcast, privato, link-local, non specificato). Viene inoltre testato il calcolo dell'indirizzo broadcast di sottorete, assicurando che subnetBroadcast() restituisca il valore corretto.

### Mac

La classe `Mac`rappresenta un indirizzo MAC a 6 byte (di livello data-link). Accetta una stringa di testo (tipicamente esadecimale con separatori) e la converte nell'array di byte corrispondente, fornendo i metodi di stringa e di confronto. Fornisce implementazioni di equals() e hashCode() per facilitare il confronto degli indirizzi MAC. Permette anche la formattazione a stringa standard di un indirizzo MAC (in lettere e cifre esadecimali). 

Il test `MacTest` verifica la corretta interpretazione delle stringhe in ingresso e la gestione degli errori. Controlla che indirizzi MAC ben formati vengano parseati correttamente e che formati non validi generino eccezioni IllegalArgumentException. Inoltre confronta indirizzi per verificarne l'uguaglianza e la corretta rappresentazione testuale.

### Mask

La classe `Mask` rappresenta la maschera di sottorete (network mask) associata a un indirizzo `IP`. Può essere creata specificando la lunghezza del prefisso (ad esempio /24 ), e calcola i 4 byte
corrispondenti. Fornisce metodi per ottenere e impostare il prefisso e la rappresentazione binaria
completa della maschera. In sostanza, gestisce la conversione tra lunghezza del prefisso e i byte della maschera. 

Il test `MaskTest` verifica la corretta traduzione tra prefisso e maschera completa: ad esempio
controlla che un prefisso /24 generi la maschera 255.255.255.0. Assicura inoltre che valori fuori
intervallo causino eccezioni.

### Port

La classe `Port`incapsula un numero di porta di livello trasporto (TCP/UDP). Controlla che il valore sia nell'intervallo valido (0–65535) e fornisce la rappresentazione numerica. Ridefinisce equals(), hashCode() e toString() per poter essere confrontato e stampato. In pratica, serve a manipolare e validare numeri di porta in modo consistente. 

Il test `PortTest` verifica che la classe accetti valori di porta validi e rifiuti valori non validi. Controlla che numeri all'interno del range vengano accettati e che valori negativi o superiori a 65535 generino eccezioni IllegalArgumentException.

## 1.2 Package app

### App

La classe astratta `App` è la base per un'applicazione utente che gira su un `NetworkNode` (ad esempio un host o un server). Fornisce attributi comuni come nome utente e riferimento al nodo proprietario, e metodi di utilità per l'interfaccia utente (ad es. stampa di messaggi). Definisce metodi astratti start() , send() e receive() che le sottoclassi concrete devono implementare per gestire l'avvio dell'applicazione e l'invio/ricezione di dati. Comprende inoltre un meccanismo per registrare e recuperare comandi testuali (getCommand() ) che facilitano la creazione di CLI nell'applicazione.

Non esistono test diretti per `App`; le sue funzionalità vengono verificate indirettamente dai test delle
classi concrete (come `MsgClient`e `MsgServer`) e dai comandi che ne estendono il comportamento.

### Command

La classe astratta `Command` rappresenta un comando eseguibile nell'ambito di un'applicazione di rete. Definisce un metodo astratto execute che esegue il comando dato l'oggetto `App`associato e la stringa di argomenti, e un metodo help() che ne restituisce la descrizione per l'utente. Comprende anche un metodo name() per ottenere il nome del comando. In pratica, permette di definire comandi specifici (estensioni concrete) che l'applicazione può invocare dinamicamente.

Non ci sono test per la classe base Command; i test interessano invece le implementazioni concrete (Help , Send), che ne utilizzano i metodi astratti per eseguire operazioni specifiche.

### CommandFactory

Interfaccia della factory per i comandi. Definisce il metodo get, che ottiene un comando.

Viene implementata da `MsgCommandFactory`.

## 1.3 Package app.msg

### MsgClient

La classe `MsgClient` estende `App` implementando un client per un semplice protocollo di
messaggistica. Al momento dell'avvio, si registra presso un server di messaggistica all'indirizzo `IPv4`specificato. Implementa start() per avviare un ciclo di lettura da console, send() per inviare
messaggi tramite lo stack di protocolli (tipicamente UDP/IP/DLL) e receive() per gestire i messaggi
in arrivo dal server. `MsgClient` modella il comportamento di un utente che invia messaggi a un server e riceve risposte.

Il test `MsgClientTest` verifica il corretto funzionamento di `MsgClient` . Controlla ad esempio che il
metodo di registrazione generi i pacchetti giusti e che la ricezione dei messaggi venga inoltrata
correttamente all'applicazione. Testa anche la gestione di casi di errore durante l'invio.

### MsgCommandFactory

La classe `MsgCommandFactory` implementa `CommandFactory` per il client di messaggistica. Dati i
nomi dei comandi disponibili (ad esempio "send" e "help"), restituisce l'istanza dell'oggetto Command corrispondente. Funge da factory che consente al client di ottenere il comando giusto in base alla stringa digitata dall'utente. 

Il test `MsgCommandFactoryTest` verifica che `MsgCommandFactory`.get restituisca la giusta classe di comando per i nomi validi (ad es. "send" → Send , "help" → Help ), e che produca eccezioni per stringhe non valide.

### MsgServer

La classe `MsgServer` estende `App` fornendo la logica di un server di messaggistica. Alla ricezione di
un nuovo client, registra il suo nome utente. Quando riceve un messaggio, lo indirizza al destinatario appropriato utilizzando la lista dei client registrati. MsgServer implementa start() per inizializzare il servizio, send() per inviare pacchetti verso un client specifico e receive() per processare i pacchetti in arrivo dai client. In pratica, gestisce l'inoltro dei messaggi tra gli utenti connessi. 

Il test `MsgServerTest` verifica le funzionalità di gestione dei client e dei messaggi. Controlla che la registrazione dei client salvi il nome utente corretto e che l'inoltro dei messaggi avvenga verso il destinatario designato. Vengono testati anche casi di errore come l'invio a utente non registrato.

## 1.4 Package app.msg.commands

### Help

La classe `Help` estende `Command` e implementa il comando di aiuto. Il metodo execute() di
`Help` stampa all'utente l'elenco dei comandi disponibili e le loro descrizioni, recuperando le
informazioni tramite help() di ciascun comando registrato. Il metodo help() restituisce una
stringa che descrive il comando help stesso. Non sono presenti test dedicati a Help ; la sua funzionalità viene coperta indirettamente dal test del client di messaggistica o manualmente dall'utente.

### Send

La classe `Send` estende `Command`e implementa il comando per inviare un messaggio a un altro
utente. Il metodo execute() interpreta gli argomenti (tipicamente nome utente e messaggio),
verifica la correttezza della sintassi e invoca l'applicazione cliente per spedire il messaggio. Il metodo help() restituisce informazioni su come utilizzare il comando send. 

Il test `SendTest` verifica che il comando Send gestisca correttamente argomenti validi e invalidi. Assicura che, dato un input ben formattato, venga chiamato l'invio del messaggio, mentre con formati errati venga generata un'eccezione adeguata.

## 1.5 Package network

### Node

Interfaccia per la struttura dei nodi di rete. Definisce i metodi send, receive e getName (del nodo). Viene implementata da `Host`, `Router` e `Server`.

### NetworkNode

La classe `NetworkNode`è astratta e modella un nodo di rete, che può gestire dei protocolli a livello 3 (host, router o server). Tiene traccia del nome del nodo, delle interfacce di rete collegate, della tabella di routing e della tabella ARP associate. Fornisce metodi di utilità comuni, come getInterface() per ottenere l'interfaccia collegata a un certo IP/Adapter, getRoute() per cercare un percorso verso un IP specificato, e funzioni per risolvere indirizzi MAC di destinazione tramite ARP. Definisce metodi astratti send() e receive() che devono essere implementati dalle sottoclassi concrete per gestire l'invio/ricezione di pacchetti secondo il comportamento del nodo. 

Non ci sono test diretti per `NetworkNode`, poiché è astratta. Le sue funzionalità di base vengono
esercitate indirettamente tramite i test di `Host` , `Router` e `Server` che ne estendono il
comportamento.

### NetworkNodeBuilder

La classe astratta `NetworkNodeBuilder<T extends NetworkNode>` è un builder per nodi di rete. Fornisce metodi per impostare il nome del nodo, aggiungere interfacce di rete ( addInterface() ), inserire rotte ( addRoute() ) e voci ARP ( addArpEntry() ). Il metodo build() astratto deve essere implementato per creare l'istanza del nodo concreto di tipo T, sfruttando i dati raccolti dal builder.

I test specifici di builder ( `HostBuilderTest` , `RouterBuilderTest` , `ServerBuilderTest` ) verificano che ciascun builder costruisca correttamente il tipo di nodo richiesto con tutti i parametri (interfacce, rotte, ARP). Questi test controllano anche che venga lanciata un'eccezione se mancano dati obbligatori.

## NetworkAdapter

L’interfaccia `NetworkAdapter` è l’interfaccia utilizza per rappresentare schede di rete. Definisce i metodi setOwner, setRemoteAdapter, getLinkedAdapter, getName, getMTU, getMacAddress, isUp, setUp, setDown, send e receive.

Viene implementata dalla classe CabledAdapter.

## CabledAdapter

La classe `CabledAdapter` implementa l’interfaccia `NetworkAdapter` ed è utilizzata per mandare frames punto-punto. Conosce infatti un riferimento interno a un’altra NetworkAdapter a cui invia le informazioni.

 Il test `CabledAdapterTest` testa il corretto funzionamento interno della classe e l’invio di informazioni punto-punto.

## 1.6 Package network.host

### Host

La classe `Host` estende `NetworkNode`e rappresenta un host di rete con un'applicazione ( App ) associata. Implementa i metodi send() e receive() specifici per un host: invia pacchetti destinati a un `IP`di destinazione calcolando via ARP l'indirizzo MAC e sfruttando la propria interfaccia, e riceve pacchetti che gli sono indirizzati inoltrandoli all'applicazione se il destinatario corrisponde. Fornisce anche metodi come setApp(App app) per collegare un'applicazione e runApp() per avviare l'elaborazione lato utente. In pratica, simula il comportamento di un dispositivo finale in rete. 

Il test `HostTest`controlla il funzionamento di `Host`: verifica anche che l'invio di dati all'interno della stessa sottorete utilizzi l'indirizzo MAC corretto (via ARP) e che receive() consegni i dati all'applicazione se appropriato. Vengono anche testate situazioni di errore, per esempio inviare pacchetti non raggiungibili.

### HostBuilder

La classe `HostBuilder` estende `NetworkNodeBuilder<Host>`fornendo un builder concreto per
istanziare un oggetto `Host` . Permette di accumulare le informazioni necessarie (nome, interfacce,
rotte, ARP) e infine, con build() , genera un oggetto Host configurato. Segue il design pattern builder per semplificare la creazione di host complessi.

Il test `HostBuilderTest` verifica che HostBuilder.build() crei un Host con le proprietà
specificate (ad es. corretto numero di interfacce, rotte impostate). Controlla inoltre che il builder lanci errori se mancano dati essenziali, come il nome del nodo.

## 1.7 Package network.router

### Router

La classe `Router` estende `NetworkNode` e rappresenta un router `IP`. Implementa send() in modo che inoltri pacchetti a un altro nodo piuttosto che consegnarli all'applicazione locale: usa la tabella di routing per determinare attraverso quale interfaccia inviare ogni pacchetto. Il metodo receive() riceve pacchetti in ingresso da un'interfaccia, decrementa il TTL, e li indirizza in uscita verso l'interfaccia di rete appropriata in base alla destinazione. In pratica, instrada i pacchetti tra diverse reti definite nelle sue rotte. 

Il test `RouterTest` verifica la logica di instradamento di Router. Ad esempio controlla che, data una
destinazione, il router scelga la rotta giusta e invii i pacchetti tramite l'adattatore corretto. Vengono
testate anche situazioni di errore (destinazione non raggiungibile, etc.).

### RouterBuilder

La classe `RouterBuilder` estende `NetworkNodeBuilder<Router>` per costruire oggetti Router. Permette di configurare nome, rotte e ARP del router prima di invocare build() per ottenere
un'istanza correttamente configurata.

Il test `RouterBuilderTest`conferma che il builder crei un Router con le rotte e gli indirizzi ARP
specificati. Verifica anche il comportamento in caso di dati mancanti o non validi.

## 1.8 Package network.server

### Server

La classe `Server<AppType extends App>` estende `NetworkNode`ed è un generico nodo server
che ospita un'applicazione. Il comportamento di send() e receive() è simile a quello di un Host ,
ma il server può gestire più client e scenari di comunicazione in entrata. Include un campo generico per 'applicazione ( AppType ) che elabora i pacchetti ricevuti. Il server si comporta come nodo finale che riceve richieste di servizi e invia risposte.

Il test `ServerTest` controlla il corretto funzionamento di Server . Verifica ad esempio che il server
riceva i pacchetti destinati a lui e li inoltri all'applicazione, e che l'invio di risposte avvenga attraverso
l'interfaccia corretta. Vengono testati anche casi di errore nella consegna dei pacchetti.

### ServerBuilder

La classe `ServerBuilder<AppType extends App>` estende `NetworkNodeBuilder<Server>` per
costruire istanze di `Server`. Similmente a `HostBuilder`, permette di configurare il nome del server,
le interfacce, le routing tables e le voci ARP, e quindi di costruire un oggetto Server<AppType> configurato.

Il test `ServerBuilderTest` verifica che `ServerBuilder` produca un server con i parametri
desiderati. Controlla che le interfacce siano collegate e che le voci di routing/ARP siano impostate
correttamente.

## 1.9 Package networkstack

### PDU

La classe astratta `PDU` (Protocol Data Unit) fornisce la struttura di base per tutte le unità di dati di
protocollo. Memorizza gli indirizzi di sorgente e destinazione di tipo Address e garantisce che non
siano nulli. È serializzabile per permettere il passaggio attraverso flussi di byte. PDU definisce
l'interfaccia minima (e.g. costruttore, accesso a sorgente/destinatario) usata dalle sue sottoclassi
concrete. 

Non ci sono test specifici per PDU ; le classi che la estendono ne ereditano il comportamento di base.

### Protocol

Protocol è l’interfaccia base dei protocoli. Definisce i metodi encapsulate, decapsulate, getSource, getDestination, extractSource, extractDestination e copy (design pattern prototype). 

Viene implementata da MSGProtocol, UDPProtocol, IPv4Protocol, SimpleDLLProtocol.

### ProtocolPipeline

La classe `ProtocolPipeline`gestisce una sequenza (pipeline) di protocolli. Fornisce metodi per
aggiungere (push() ) o rimuovere ( pop()) protocolli dalla fine della catena. Con encapsulate(byte[] data) , percorre tutti i protocolli nella pila applicando l'incapsulamento in ordine, mentre decapsulate(byte[] data) applica la decapsulazione inversa. In questo modo permette di processare i dati attraverso più livelli protocolari (ad es. incapsulare dati applicativi in UDP, poi in IP, poi in frame).

Il test `ProtocolPipelineTest` verifica l'aggiunta e rimozione di protocolli (push/pop) e controlla che
l'incapsulamento e decapsulazione dei dati restituiscano il payload originale dopo un ciclo completo.
Vengono anche testate situazioni di pila vuota o di parametro null.

## 1.10 Package protocols.IPv4

### IPv4Packet

La classe `IPv4Packet` rappresenta la struttura completa di un pacchetto IPv4, comprendente header e payload. Consente di costruire un header IPv4 completo da parametri sorgente, destinazione, porte, protocolli e dati di livello superiore. Fornisce metodi come toByte() per ottenere la rappresentazione binaria del pacchetto pronto per la trasmissione, e getHeader() per estrarre l'header. Gestisce l'assemblaggio dell'header secondo il formato standard di IPv4. 

Il test `IPv4PacketTest` verifica che IPv4Packet costruisca correttamente l'header IPv4: ad esempio controlla la corretta posizione dei byte corrispondenti alla sorgente, destinazione, protocolli e
lunghezze. Assicura inoltre che toByte() generi l'array di byte appropriato.

### IPv4Protocol

`IPv4Protocol` implementa l'interfaccia Protocol per il livello IP (livello 3). Nel costruttore si specificano indirizzo sorgente, destinazione e TTL. Il metodo encapsulate() costruisce un IPv4Packet incapsulando i dati dati e ne restituisce il byte array. Il metodo decapsulate() prende
un array di byte di un pacchetto IPv4, decrementa il TTL e restituisce il payload. Sono presenti anche metodi per estrarre sorgente, destinazione e altri campi dall'header. In pratica, simula la funzionalità di un protocollo IP per incapsulare e decapsulare dati. 

Il test `IPv4ProtocolTest` verifica che l'incapsulamento e la decapsulazione funzionino correttamente: i dati originali devono essere recuperati dopo un ciclo e il TTL deve essere decrementato. Controlla anche che gli indirizzi sorgente/destinazione siano gestiti correttamente

### VersionIHL

La classe `VersionIHL`modella i primi due campi dell'header IPv4 (versione e IHL). È una classe finale che contiene valori fissi (version = 4, IHL = 5 senza opzioni) e fornisce la rappresentazione binaria di questo byte iniziale. Viene utilizzata internamente in IPv4Packet per costruire l'header standard senza permettere modifiche. 

Non sono presenti test dedicati a `VersionIHL`; la correttezza viene implicitamente testata dai test di IPv4Packet e IPv4Protocol che ne utilizzano i valori.

## 1.11 Package protocols.MSG

### MSGHeader

`MSGHeader` estende PDU che specifica per il protocollo di messaggistica di livello applicativo. Contiene un nome utente e un messaggio testuale ( message ) come payload. Implementa la serializzazione dei campi in un array di byte (metodi toByte() e costruttori da byte) per l'invio in rete. 

Il test `MSGHeaderTest` verifica che la serializzazione e deserializzazione di MSGHeader funzioni correttamente: dati nome e messaggio, controlla che la conversione in byte array e il parsing inverso preservino i valori originali.

### MSGProtocol

`MSGProtocol` implementa l'interfaccia Protocol per il protocollo applicativo di messaggistica.
Definisce una porta fissa (9696) e usa un oggetto MSGHeader per incapsulare i dati. Il metodo
encapsulate() costruisce un `MSGHeader` con nome e payload e restituisce il byte array, mentre
decapsulate() estrae l'header. Viene utilizzato dal client e server per gestire le comunicazioni di
messaggi testuali tra utenti. 

Il test `MSGProtocolTest` assicura che MSGProtocol incapsuli e decapsuli i dati correttamente, cioè
che dopo un ciclo di encapsulate() e decapsulate() si ottengano i dati originali. Verifica inoltre
l'uso corretto della porta e la gestione della lunghezza dell'header.

## 1.12 Package protocols.SimpleDLL

### SimpleDLLFrame

La classe `SimpleDLLFrame`implementa una PDU a livello DLL con un indirizzo Mac di sorgente e un indirizzo Mac di destinazione. Fornisce metodi di serializzazione per creare un frame dal byte array.

Il test `SimpleDLLFrameTest` assicura la corretta implementazione della serializzazione.

### SimpleDLLProtocol

La classe `SimpleDLLProtocol`implementa l’interfaccia Protocol per il livello DLL. In encapsulate incapsula i dati in una frame e in decapsulate estrae il payload da una frame SimpleDLLFrame.

Il test `SimpleDLLProtocolTest` controlla che l’incapsulamento de decapsulamento siano consistenti: i dati ottenuti dopo un ciclo di encapsulate/decapsulate corrispondano a quelli iniziali. Verifica anche che un frame errato produca eccezioni.

## 1.13 Package protocols.UDP

### UDProtocol

`UDPProtocol` implementa l'interfaccia Protocol per il livello di trasporto UDP. Nel costruttore si
specificano porte sorgente e destinazione e l'MTU del livello inferiore. encapsulate() crea un
segmento UDP aggiungendo l'header (porte, lunghezza, checksum) ai dati applicativi. decapsulate() estrae il payload rimuovendo l'header UDP. Simula il comportamento di UDP
incapsulando i dati nel formato corretto. 

Il test `UDPProtocolTest` insieme a `UDPSegmentTest` verifica la correttezza del protocollo UDP: controlla che i campi di header (sorgente, destinazione, lunghezza) siano settati correttamente e che l'incapsulamento/decapsulazione restituisca i dati originari.

### UDPSegment

`UDPSegment` rappresenta un datagramma UDP, con campi di sorgente, destinazione, lunghezza,
checksum e payload. Fornisce metodi per costruire un segmento da byte e per ottenere i suoi byte
( toByte() ). Viene utilizzato da UDPProtocol per gestire il payload di livello trasporto.

Il test `UDPSegmentTest` assicura che la conversione tra `UDPSegment`e byte array sia corretta: dati
valori di porta sorgente/destinazione e payload, verifica che toByte() produca l'array giusto e che il
costruttore ricostruisca i campi senza errori.

## 1.14 Package table

### NetworkTable

Interfaccia che definisce una tabella utilizza a livello di rete dai nodi. Definisce i metodi lookup, add, remove e isEmpty.

### ArpTable

`ArpTable` gestisce la mappatura tra indirizzi `IPv4` e indirizzi `MAC` all'interno di un nodo. Permette di
aggiungere voci statiche ( add() ), rimuoverle e di definire un gateway di default ( setGateway() ).
Implementa l'interfaccia `NetworkTable<IPv4, Mac>` . Viene utilizzata da NetworkNode per
risolvere indirizzi MAC dati IP di destinazione.

Il test `ArpTableTest` verifica che `ArpTable` mantenga correttamente le voci: ad esempio che
add() memorizzi la coppia IP–MAC e che remove() la elimini. Controlla anche la gestione del
gateway predefinito e il comportamento in casi di chiavi non trovate o parametri nulli.

> *NB: il protocollo ARP non è stato implementato, questa tabella serve per possibili aggiornamenti del software.*
> 

### MacTable

`MacTable` implementa l'interfaccia `NetworkTable<Mac>`,  `NetworkAdapter>` per associare un
indirizzo `Mac`a un `NetworkAdapter`in una rete. Permette di aggiungere e rimuovere voci,
mappando l'indirizzo MAC di un nodo al relativo adattatore fisico. Viene utilizzata, per esempio, dai
componenti di rete (come gli switch) per mantenere la conoscenza di chi detiene quale MAC.

Il test `MacTableTest` controlla che le associazioni MAC→Adapter siano memorizzate correttamente:
che add() memorizzi il binding e lookup() (se presente) lo recuperi. Verifica anche la gestione di
input non validi.

> *NB: il dispositivo Switch (che implementerebbe l’interfaccia Node) non è stato implementato nel software, anche questa tabella è stata scritti per possibili aggiornamenti.*
> 

### RoutingInfo

`RoutingInfo`rappresenta un'informazione di routing: contiene l'indirizzo di rete (IPv4 + prefisso) di
una rotta e l'indirizzo IP del gateway successivo. Fornisce metodi di accesso a questi campi. Viene
impiegata all'interno di RoutingTable come valore associato a una destinazione di rete. 

Il test `RoutingInfoTest` controlla che due oggetti con gli stessi valori di rete e gateway siano
considerati uguali (override di equals() ), e che i campi siano memorizzati correttamente

### RoutingTable

`RoutingTable` implementa `NetworkTable<IPv4, RoutingInfo>` e mantiene un insieme di rotte.
Permette di aggiungere una rotta ( add() ), impostare una rotta di default ( setDefault() ), rimuovere rotte e svuotare la tabella. Ogni rotta associa un indirizzo di rete a un RoutingInfo che include il prossimo salto. Viene utilizzata dai nodi NetworkNode per determinare come instradare i pacchetti verso destinazioni remote. 

Il test `RoutingTableTest` verifica le operazioni base: ad esempio che add() inserisca
correttamente una rotta e che lookup() (se presente) restituisca il valore corretto. Testa anche la
rotta di default e la cancellazione completa della tabella.

## 1.15 Package utils

### Logger

La classe `Logger` è un singleton per la registrazione di messaggi di log. Fornisce metodi come info(String msg) per stampare messaggi informativi con timestamp e categoria. Ad esempio, molte classi del progetto chiamano Logger.getInstance().info(...) per segnalare eventi durante 'esecuzione. Non dipende da altri componenti, semplificando l'aggiunta di log in tutto il sistema. 

Il test `LoggerTest`verifica il formato dei messaggi prodotti da Logger . Assicura che, date stringhe
di input note, l'output generato contenga timestamp, livello e messaggio attesi.

# 2. Design patterns utilizzati

> NB: Ogni singola classe contiene 2 campi statici "logger" e "CLS" che servono per il logging. Poiché è un attributo comune, non verrà rappresentato nell'UML.

## 2.1 Singleton

E’ stato utilizzato il Singleton per il `Logger` per diverse ragioni:

1. **Singola istanza a runtime**: E’ ottimale avere una singola istanza piuttosto che tante istanze che cercano di loggare nello stesso file.
2. **Nessun utilizzo di variabili globali**: per evitare variabili globali che riducono la riusabilità del codice.
3. **Test più semplici rispetto a una classe statica**: fare il test di una classe con il singleton, il logger in questo caso, è molto più semplice rispetto a testare metodi statici.

![image.png](Netsim%201eb9ef2c589080c790c1de176a865d47/image%203.png)

## 2.2 Prototype

Il design pattern prototype è stato utilizzato principalmente per permettere la clonazione dei protocolli  (interfaccia `Protocol`) senza dover utilizzare la stessa istanza, così che i protocolli all’interno di `ProtocolPipeline`, una volta inseriti, siano immutabili.

![image.png](Netsim%201eb9ef2c589080c790c1de176a865d47/image%204.png)

## 2.3 Builder

Il design pattern builder è stato utilizzato per permettere di istanziare nodi di rete senza dover prima istanziare le componenti interne, come `RoutingTable`, `ArpTable` o `Interface` . Semplifica infatti la creazione modulare di un nodo nascondendo le interfacce all’interno. Viene esteso, utilizzando il tipo generics T, dalle classi `RouterBuilde`r, `ServerBuilder` e `HostBuilder` .

Non è previsto l’uso di una classe `Director` in quanto non esistono configurazione pre-fabbricate ma vanno decise a Runtime.

![image.png](Netsim%201eb9ef2c589080c790c1de176a865d47/image%205.png)

## 2.4 Factory method & Command

Questa parte di progetto comprende 2 design patterns che lavorano insieme:

- **Factory method**: il factory method è stato utilizzato per ottenere a runtime, grazie a una chiave di tipo Stringa (come “send”, “help”), il comando giusto. E’ stato implementato in maniera “monolitica” dall’interfaccia `CommandFactory`, senza ricorrere a un creator per ogni comando implementato in quanto il costruttore del comando non necessita di parametri e le le classi concrete non hanno bisogno di una necessaria attenzione durante la creazione.
- **Command**: il command è stato utilizzato per creare un modo di disaccoppiare la logica del comando da eseguire dall’applicazione chiamante. In questo specifico caso, l’`App` può attraverso `CommandFactory` ottenere un comando in base alla chiave e eseguirlo. Quindi l’`App` svolge sia il ruolo di Invoker che di Receiver, senza però che le classi si conoscano direttamente con una associazione ma hanno solo una dipendenza l’una con l’altra.
Sono state implementate le classi concrete `MSgServer`, `MsgClient`, `MsgCommandFactory`, `Send` ed `Help` (in blu).

![image.png](Netsim%201eb9ef2c589080c790c1de176a865d47/image%206.png)

## 2.5 Mediator & Facade

Questa parte di progetto comprende 2 design pattern:

- **Facade**: il facade viene utilizzato da `NetworkNode` per nascondere l’implementazione complessa di send e receive (che utilizzano `NetworkTable` e `NetworkAdapter` per inviare codice). Così che il client possa chiamare semplicemente send passando i parametri e il facade si occupi di tutte le operazioni interne definite poi nelle sue sottoclassi `Host`, `Router` e `Server`.
- **Mediator**: il mediator viene utilizzato per controllare come un gruppo di `NetworkAdapter` interagiscono tra di loro. E’ stato pensato principalmente per il forwarding nel router, però può essere esteso a possibili classi future. 
In questo design pattern l’interfaccia `Node` si comporta da **Mediator**, `NetworkAdapter`  da **Colleague**, `Host`, `Router` e `Server` da **ConcreteMediator** e `CabledAdapter` da **ConcreteColleague**. L’unica differenza rispetto a un mediator “puro” è che i ConcreteMediator non conoscono direttamente i ConcreteColleague ma devono prima passare per la classe Interface per ottenere quello corretto.

![image.png](Netsim%201eb9ef2c589080c790c1de176a865d47/image%207.png)
