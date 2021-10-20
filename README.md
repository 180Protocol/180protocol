## 180Protocol POC

180Protocol is a next generation collaborative computing framework that helps unlock value in private data. Businesses 
can utilize 180Protocol to create coalitions - private networks on corda - to share their private structured data assets.
Coalitions are composed of corda nodes that can act as Data Providers and Data Consumers, and a coalition host.

* Data Providers - nodes that have pre-approved structured private data assets that they want to share and be rewarded for
* Data Consumer - nodes that have a need to consumer data outputs
* Coalition Host - node that runs the trusted enclave and arbitrates communication between providers and consumers

180Protocol comprises the following components:

1. **Protocol broker** - a set of corda flows that manage communication between data consumers and data providers in a 180protocol coalition
The broker connects with the enclave based aggregator to enable consumers and providers to share data and be rewarded for it.

2. **Protocol aggregator** - a enclave based data transformation interface that can take flexible data input, data output, and provenance definitions. 
Furthermore, a data transformation algorithm can be configured to map inputs to outputs. The Aggregator includes a proprietary 
rewards engine that can be configured to reward data providers in a coalition for their private data inputs. Since the 
aggregator runs inside a trusted enclave, it can 

3. **180Dashboard** - a React based front end application that allows Data Providers and Data Consumers to keep track of shared data,
view data aggregation history and keep track of rewards for each data aggregation.

The above components can be used by application developers to create decentralized application that enable private data assets
to be shared, transformed and rewarded for in a secure way.

The 180ProtocolPoc is representative usage of the 180Protocol framework.

###How to run the 180Protocol POC
The protocol poc comprises the 180Dashboard and pocCordApp modules. The 180 Dashboard is the React based front end and the
pocCordApp is the sample backend.

To build pocCordApp:

1.  Run the `./gradlew build` command from within the pocCordApp folder
2.  Run `./gradlew deployNodes` command to generate nodes folder inside build folder.

To run the sample 180ProtocolPOC Coalition network using Docker:

1. Run below command from the project root path to start 180Dashboard service & pocCordApp
   service using docker compose

   "docker-compose -f ./compose-corda-network.yml -f ./compose-cordaptor.yml up"

2. The 180Dashboard can be accessed at http://localhost:3000 on a browser 
3. Each data provider data can be viewed on the dashboard using their login credentials (corda node hostname as user and
port number as the password) -
Ex, for provider A use 
username: partya-node
password: 9500