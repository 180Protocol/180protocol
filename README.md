<img src=".github/180protocol-logo-dark.png" alt="180Protocol Logo" width="40%"/>

### Introduction
180Protocol is an open-source toolkit for data sharing. 180Protocol represents the next evolution of data
sharing infrastructure, an infrastructure that ensures a secure, rewarding, and efficient data sharing experience.

180Protocol targets enterprise use cases and is uniquely positioned to improve the value and mobility of sensitive business data. 
The software introduces a distributed network design to the problem of enterprise data sharing,
solving for the legacy barriers that have limited data mobility and value. 180Protocol makes data available where it needs to be,
moving it and transforming it along they way, all the while preserving privacy.

### Design and Components

Developers can utilize 180Protocol to create data unions - permissioned networks on [R3 Corda](https://docs.r3.com/) 
to share their private structured data. Unions (also referred to as Coalitions) are composed of corda nodes that can act as Data Providers and Data Consumers,
and a union host.

* Data Providers - nodes that have pre-approved structured private data assets that they want to share and be rewarded for
* Data Consumer - nodes that have a need to consume unique and commercially valuable data outputs
* Union Host - node that runs the trusted enclave (via [R3 Conclave](https://docs.conclave.net/)) and arbitrates communication between providers and consumers

180Protocol comprises the following components:

1. **Aggregator SDK** - allows defining a data transformation service that can take flexible data input, data output, 
and provenance data definitions. A data transformation algorithm can also be configured to map inputs to outputs. 
The Aggregator includes a Rewards Engine that can be configured to reward data providers 
in a union for sharing sensitive data. Since the aggregator runs inside a trusted enclave, the sensitive data is safe from exploitation. Additionally, the Rewards Engine, 
providers data inputs are rewarded unbiasedly. Data Consumers get data outputs based on a known transformation algorithm.  

2. **180Dashboard** - a React based front end application that allows Data Providers and Data Consumers to keep track of 
shared data, view data aggregation history and keep track of rewards for each data aggregation.

3. **Codaptor** - a middleware that connects to the Corda RPC and generates OpenAPI bindings automatically for any CordApp

The above components can be used by application developers to create decentralized applications that enable private 
structured data to be shared, transformed and rewarded for in a secure way.

This repository contains example applications built on the 180Protocol framework and these be utilized by developers
as a blueprint to start building more complex applications. We encourage application developers and businesses to build 
on 180Protocol and provide feedback, so we can introduce better features over time.

Please read more detailed design and API specifications on our [Wiki](https://docs.180protocol.com/)

### Use 180Protocol

Please follow the documentation on our [Wiki](https://docs.180protocol.com/develop/tutorials) to get started with 180Protocol -

* [Set up and run 180Protocol](https://docs.180protocol.com/develop/tutorials/set-up-and-run-180protocol)
* [Managing a 180Protocol union network](https://docs.180protocol.com/develop/tutorials/managing-a-180protocol-coalition-network)
* [Writing your 180Protocol CoApp](https://docs.180protocol.com/develop/tutorials/writing-your-180protocol-coapp)
* [Using Filecoin as Storage](https://docs.180protocol.com/develop/tutorials/using-filecoin-as-storage)
* [Examples](https://github.com/180Protocol/180protocol/tree/main/examples/)
* [Aggregator SDK API's](https://docs.180protocol.com/develop/aggregator-sdk-apis)

### Features and design

Read about our features and design details -

* [Features](https://docs.180protocol.com/learn/features)
* [Network Architecture](https://docs.180protocol.com/learn/features/network-architecture)
* [Enclave Workflows](https://docs.180protocol.com/learn/features/enclave-workflows)

#### Storage

180Protocol received a grant from Filecoin to integrate with the Filecoin ecosystem. More details can be found [here.](https://github.com/filecoin-project/devgrants/issues/451)
As part of the grant, we have utilized the [Estuary API](https://estuary.tech/) to store input data from Providers and output
data for Consumers on Filecoin. We have created a separate module called `estuaryStorage` that can be optionally deployed
as a CordaApp. This module overrides base AggregatorSDK workflows and gives Providers and Consumers the option to store data
on Filecoin.

Read more about the architecture and design on our wiki - [Storage](https://docs.180protocol.com/learn/features/storage)

### Roadmap
This is the alpha release of 180Protocol and is experimental in nature. 
There are improvements we plan to release in the coming weeks including -

1. Introducing a variety of example use cases across industry verticals - :white_check_mark:
2. Introducing Filecoin as a storage layer for data inputs and outputs - :yellow_circle:
3. Supporting multiple enclave communication frameworks beyond R3 Conclave. We are currently testing compatibility with
   [Anjuna.io](https://anjuna.io/) - :yellow_circle:
4. Introducing a regression based rewards engine that can be utilized within the Aggregator SDK - :white_circle:
5. We have tested the enclave based computations in the Enclave 'Mock' mode. We are testing deployment using an [MS Azure](https://azure.microsoft.com/en-gb/solutions/confidential-compute/)
   cloud enclave - :white_circle:
6. The Aggregator SDK workflows are consumer driven only. All providers in the network are required to share data when 
requested by the consumer. We intend to introduce further variations of the Aggregation Flows that account for varied 
commercial use cases - :white_circle:
7. Integrating the Aggregator SDK with a public blockchain to enable data providers to monetize their rewards using a token
economy - :white_circle:

**Legend**
* :white_check_mark: - completed
* :yellow_circle: - in progress
* :white_circle: - planned

### Contact 
* Commercial queries: [management@180protocol.com](mailto:management@180protocol.com)
* Developer Discord: [180Protocol Discord](https://discord.com/invite/vvA8sRbs)
* Community channels: [www.180protocol.com](https://www.180protocol.com/)

### Licenses

Please take note of the license obligations you are bound under by downloading the source code. The repository contains
two folders, under different licenses -

1. 180Dashboard - released under the Apache2.0 License 
2. protocolAggregator - released under the GNU AGPL3.0 license 

Additionally, protocolAggregator has a dependency on R3 Conclave, which requires developers to download the Conclave API.
Please take a look at the R3 Conclave license considerations for further information.

We have tried to emulate MongoDB's philosophy in this regard. Please [read further](https://www.mongodb.com/blog/post/the-agpl)
