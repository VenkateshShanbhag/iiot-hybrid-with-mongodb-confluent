# IIoT Hybrid Demo with MongoDB and Confluent

The usecase is a subset, [Internet of Things (IoT) and Event Streaming at Scale with Apache Kafka and MQTT](https://www.confluent.io/blog/iot-with-kafka-connect-mqtt-and-rest-proxy/).

Apache Kafka® and its surrounding ecosystem, which includes Kafka Connect, Kafka Streams, and ksqlDB, have become the technology of choice for integrating and processing these kinds of datasets.

Kafka-native options to note for MQTT integration beyond Kafka client APIs like Java, Python, .NET, and C/C++ are:

- Kafka Connect source and sink connectors, which integrate with MQTT brokers in both directions
- Confluent MQTT Proxy, which ingests data from IoT devices without needing a MQTT broker
- Confluent REST Proxy for a simple but powerful HTTP-based integration

[Confluent replicator](https://docs.confluent.io/platform/current/multi-dc-deployments/replicator/index.html) provides mature, robust, and advanced Kafka replication. Features like topic auto-creation, schema replication, partition detection, and reliable active-active replication make replicated event-driven architectures reachable for even small teams.

In this demo of IIoT hybrid use case (confluent at the edge) with MongoDB atlas and Confluent, we leverage the hybrid functionality of the replicator. [Datagen connector](https://www.confluent.io/hub/confluentinc/kafka-connect-datagen) simulates IIoT data, and the replicator moves it securely Confluent cloud. The data is processed using ksqlDB and sent to MongoDB Atlas. With Confluent and technology partner [connectors](https://www.confluent.io/hub/), data can move to any plane (Apps, Object Store, BI and Visualization, etc.).

The demo is inspired by [Confluent CP Demo](https://docs.confluent.io/platform/current/tutorials/cp-demo/docs/hybrid-cloud.html).

### Caution
Any Confluent Cloud example uses real Confluent Cloud resources that may be billable. An example may create a new Confluent Cloud environment, Kafka cluster, topics, ACLs, and service accounts, as well as resources that have hourly charges like connectors and ksqlDB applications. To avoid unexpected charges, carefully evaluate the cost of resources before you start. After you are done running a Confluent Cloud example, destroy all Confluent Cloud resources to avoid accruing hourly charges for services and verify that they have been deleted.

### Confluent Cloud Promo Code
To receive an additional $50 free usage in Confluent Cloud, enter promo code CPDEMO50 (*may no longer valid*) in the Confluent Cloud UI Billing and payment section (details). This promo code should sufficiently cover up to one day of running this Confluent Cloud example, beyond which you may be billed for the services that have an hourly charge until you destroy the Confluent Cloud resources created by this example.

### A note on replicator
Confluent Replicator acts as a Source Connector which reads from a remote cluster to the Kafka Connect bootstrap cluster (by default). The configuration parameters such as dest.kafka.* suggest that all settings with regards to writing data topics are handled by those set of configs. However, in reality, dest.kafka.* is only used for non-produce operation like creating destination topic or translating
offset. Produce operation is controlled by Kafka Connect producer configuration because Confluent Replicator is a Source Connector.

## Architecture of data flow
<img align="center" src="./assets/IIoT%20Hybrid%20Usecase.png?raw=true">

## Get started

1. Create a [Confluent Cloud account](https://confluent.cloud/signup).

2. Setup a payment method for your Confluent Cloud account and optionally enter the promo code CPDEMO50 (*may no longer valid*) in the Confluent Cloud UI Billing and payment section to receive an additional $50 free usage.

3. [Install Confluent Cloud CLI] (https://docs.confluent.io/ccloud-cli/current/install.html) v1.34.0 or later.
Using the CLI, log in to Confluent Cloud with the command ccloud login, and use your Confluent Cloud username and password. The --save argument saves your Confluent Cloud user login credentials or refresh token (in the case of SSO) to the local netrc file.

4. Login to Confluent Cloud and Store Credentials in Local 

Execute: 

*ccloud login --save*

5. Cloud workflow

Execute:

*./docker/1_confluent_cloud_provisioning.sh*

It - 
- creates a new environment.
- creates a new service account.
- creates a new Kafka cluster and associated credentials.
- enables Confluent Cloud Schema Registry and associated credentials.
- creates ACLs with a wildcard for the service account.
- creates "iiot.simulated" topic and update the service account with ACL operations.

6. Create Atlas connector in Confluent cloud

Append MONGODB_HOST, MONGODB_USER, MONGODB_PASSWORD in the delta_configs/env.delta file.

*./docker/ccloud/2_submit_atlas.sh*

7. Docker workflow.

Execute:

*./docker/3_create_docker.sh*

It - 
- creates containers for zookeeper, broker, schema-registry, replicator, connect, and Control-center.

8. Connector workflow

Execute:

*./docker/4_create_topic_connectors_command.sh*

It -
- creates iiot.simulated topic on the connect service
- starts a replicator connector on the replicator service
- starts a datagen connector to populate simulated data to iiot.simulate topic.

##### 9. ksqlDB - TODO

## Destroy the environment
To destroy docker and cloud assets,

Execute:

*./docker/4_destroy-workflow.sh*
