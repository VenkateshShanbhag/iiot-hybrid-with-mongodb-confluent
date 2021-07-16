#!/bin/bash

HEADER="Content-Type: application/json"
source ./delta_configs/env.delta
DATA=$( cat << EOF
{
  "name": "iiot-replicate-topic-to-ccloud",
  "config": {
    "connector.class": "io.confluent.connect.replicator.ReplicatorSourceConnector",
    "topic.whitelist": "iiot.simulated",
    "topic.sync": "false",
    "max.request.size": 10485760,
    "batch.size": 16384,
    "offset.topic.commit": "true",
    "offset.translator.tasks.max": 0,
    "offset.timestamps.commit": "false",
    "offset.flush.interval.ms": 100,
    "offset.flush.timeout.ms": 1000,
    "src.kafka.request.timeout.ms": 60000,
    "buffer.memory": 17179868,
    "retry.backoff.ms": 500,
    "request.timeout.ms": 20000,
    "producer.override.linger.ms": 5,
    "producer.override.batch.size": 800000,
    "producer.override.compression.type": "zstd",
    "src.consumer.fetch.min.bytes": 800000,
    "src.consumer.max.partition.fetch.bytes": "10485760",
    "key.converter": "io.confluent.connect.replicator.util.ByteArrayConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schema.registry.url": "http://schema-registry:8081",
    "src.value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "src.value.converter.schema.registry.url": "http://schema-registry:8081",
    "dest.kafka.bootstrap.servers": "${BOOTSTRAP_SERVERS}",
    "dest.kafka.security.protocol": "SASL_SSL",
    "dest.kafka.sasl.mechanism": "PLAIN",
    "dest.kafka.sasl.jaas.config": "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"${CLOUD_KEY}\" password=\"${CLOUD_SECRET}\";",
    "dest.schema.registry.url": "${SCHEMA_REGISTRY_URL}",
    "dest.topic.replication.factor": 3,
    "dest.kafka.retry.backoff.ms": "500",
    "confluent.topic.replication.factor": 3,
    "src.kafka.bootstrap.servers": "broker:29092",
    "src.consumer.fetch.max.wait.ms": 500,
    "offset.timestamps.commit": "false",
    "src.consumer.interceptor.classes": "io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor",
    "tasks.max": "3",
    "topic.config.sync": false,
    "src.schema.registry.url": "http://schema-registry:8081",
    "provenance.header.enable": "true"
  }
}
EOF
)

docker-compose exec replicator curl -X POST -H "${HEADER}" --data "${DATA}" http://localhost:8084/connectors
