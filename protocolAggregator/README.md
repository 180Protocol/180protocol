# protocolaggregator Cordap

This Cordapp is the complete implementation of data aggregation protocol with conclave(enclave) environment.

## Concepts

Multiple Nodes within the corda network will be able to exchange encrypted-data with cryptography according to roles like `provider` & `consumer`. 


### Flows

The flow available is only for testing data-flow from client-node to host-node to enclave. 


## Usage

### Running the CorDapp

Build ProtocolAggregator.

Once your application passes all tests in `AggregatorEnlcaveTest` inside workflows, you can run the application and
interact with it via a web browser or command line terminal. To run the finished application, you have two choices for each language: from the terminal, and from IntelliJ.

Open a terminal and go to the project root directory and type: (to deploy the nodes using bootstrapper)
```
./gradlew clean deployNodes
```
Then type: (to run the nodes)
```
./build/nodes/runnodes
```

### Starting the webserver
Once the nodes are up, we will start the webservers next. This app consists of three nodes and one notary, so we will be starting 3 webservers separately. First, lets start PartyA's webserver. Open a new tab of the terminal (make sure you are still in the project directory) and run:
```
./gradlew runPartyAServer
```
repeat the same for PartyB and PartyC, run each of the commands in a new tab:
```
./gradlew runPartyBServer
```
and
```
./gradlew runPartyCServer
```

