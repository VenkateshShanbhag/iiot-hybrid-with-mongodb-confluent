# iiot-hybrid-with-mongodb-confluent
[Confluent replicator](https://docs.confluent.io/platform/current/multi-dc-deployments/replicator/index.html) provides mature, robust, and advanced Kafka replication. Features like topic auto-creation, schema replication, partition detection, and reliable active-active replication make replicated event-driven architectures reachable for even small teams.

In this demo of IIoT hybrid use case (confluent at the edge) with MongoDB atlas and Confluent, we leverage the hybrid functionality of the replicator. [Datagen connector](https://www.confluent.io/hub/confluentinc/kafka-connect-datagen) simulates IIoT data, and the replicator moves it securely Confluent cloud. The data is processed using ksqlDB and sent to MongoDB Atlas. With Confluent and technology partner [connectors](https://www.confluent.io/hub/), data can move to any plane (Apps, Object Store, BI and Visualization, etc.).

## Architecture of data flow
<img align="center" src="./assets/IIoT%20Hybrid%20Usecase.png?raw=true">

## Steps to create the infrastructure
