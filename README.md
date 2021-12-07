# 180Protocol POC

180Protocol is a next generation collaborative computing framework that helps unlock value in private data. Businesses 
can utilize 180Protocol to create coalitions - private networks on R3 Corda - to share their private structured data assets.
Coalitions are composed of corda nodes that can act as Data Providers and Data Consumers, and a coalition host.

* Data Providers - nodes that have pre-approved structured private data assets that they want to share and be rewarded for
* Data Consumer - nodes that have a need to consume data outputs
* Coalition Host - node that runs the trusted enclave and arbitrates communication between providers and consumers

180Protocol comprises the following components:

1. **Protocol broker** - a set of corda flows that manage communication between data consumers and data providers 
in a 180protocol coalition. The broker connects with the enclave based aggregator to enable consumers and providers to 
share data and be rewarded for it.

2. **Protocol aggregator** - a enclave based data transformation interface that can take flexible data input, data output, 
and provenance definitions. Furthermore, a data transformation algorithm can be configured to map inputs to outputs. 
The Aggregator includes a proprietary rewards engine that can be configured to reward data providers in a coalition for 
their private data inputs. Since the aggregator runs inside a trusted enclave, with a pre verified attestation, providers
data inputs are rewarded unbiasedly. Furthermore, consumers get data outputs based on a known transformation algorithm.  

4. **180Dashboard** - a React based front end application that allows Data Providers and Data Consumers to keep track of 
shared data, view data aggregation history and keep track of rewards for each data aggregation.

The above components can be used by application developers to create decentralized applications that enable private 
structured data to be shared, transformed and rewarded for in a secure way.

The 180ProtocolPoc is representative application built on the 180Protocol framework and can be utilized by developers
as a blueprint to start building more complex applications. We encourage application developers and businesses to build 
on 180Protocol and provide feedback so we can introduce better features over time. 

**Contact: [management@180protocol.com](mailto:management@180protocol.com)**

### How to run the 180Protocol POC

The protocol poc comprises the 180Dashboard and pocCordApp modules. The 180 Dashboard is the React based front end and the
pocCordApp is the sample backend.

To build pocCordApp:

1.  Run the `./gradlew build` command from within the pocCordApp folder
2.  Run `./gradlew deployNodes` command to generate nodes folder inside build folder.

To run the sample 180ProtocolPOC Coalition network using Docker:

1. Run below command from the project root path to start 180Dashboard service & pocCordApp
   service using docker compose

   `docker-compose -f ./compose-corda-network.yml -f ./compose-codaptor.yml up`

2. The 180Dashboard can be accessed at `http://localhost:3000` on a browser 
3. Each data provider data can be viewed on the dashboard using their login credentials (corda node hostname as user and
port number as the password) -
Ex, for provider A use 
`username: partya-node 
password: 9500`

#### Configuring 180Protocol Dashboard users info and rewards weights

We provide a yaml file to configure the number of nodes in the coalition - providers or consumers - who have access to 
the 180Dashboard. Users can be configured in `userInfo.yml` provided under the `180Dashboard/src` directory. These 
users must be in accordance with the Corda network setup configured under the `pocCordApp/build.gradle`. 
The corda network gradle config allows specifying the party name for each Corda node. This party name must also be 
specified under the userInfo.yml file. The node role (provider or consumer) can also be specified in the userInfo.yml.

Further, the port number under userInfo.yml must be the same as that specified `compose-codaptor.yml` file. This
is to ensure that the dashboard can query the correct OpenAPI endpoint for the said node. 

Finally, the rewards weights used by the Rewards Engine must be configured for the dashboard to display correctly.

#### Adding a new node to the 180Protocol network

To add a new node for the 180Protocol POC, configurations need to be added to the 180Dashboard, the Codaptor OpenAPI and
the corda network configurations -

1. Corda Network: add the new node config under the `deployNodes` task of the `pocCordApp/build.gradle` file, e.x. -

   ```
   node {
     name "O=PartyB,L=New York,C=US"
     p2pAddress "partyb-node:10008"
     rpcSettings {
       address("0.0.0.0:10009")
       adminAddress("0.0.0.0:10049")
     }
     rpcUsers = [[ user: "user1", "password": "test", "permissions": ["ALL"]]]
   }
   ```

   In the above, the name corresponds to the node name under the `180Dashboard/src/userInfo.yml`. Run the `deployNodes` 
task to generate the build for the new node. 

2. Codaptor OpenAPI: add the new node details to the `compose-corda-network.yml` and `compose-codaptor.yml`
files by following the examples given in these files. Note the Corda RPC and P2P port configurations under `compose-corda-network.yml` 
are the same as the those specified under the `build.gradle` file for the new node. Additionally, the `compose-codaptor.yml`
exposes the Codaptor OpenAPI for the new node. The `CORDAPTOR_API_EXTERNAL_ADDRESS` specified here should match with the 
port number specified under the `180Dashboard/src/userInfo.yml`. 
For further details around Codaptor configurations see: [Codaptor](https://github.com/180Protocol/codaptor)

3. 180Dashboard: add details of the new node under the `nodes` section of `180Dashboard/src/userInfo.yml` file. It includes
   `username`, `password` and `role` fields of the node user to enable login into the dashboard. You can also
   specify `port` number and party `name` of new node. 
   
   ```
   providerA:
     username: providerA
     password: test
     port: 9500
     role: provider
     name: O=PartyA,L=London,C=GB
   ```
   Here the `username`, `password` are arbitrary but `port` and `role` need to follow the dependencies described in the steps above

   
