#!/bin/bash

HEADER="Content-Type: application/json"

DATA=$( cat << EOF
{
    "name": "iiot-simulated",
    "config": {
      "connector.class": "io.confluent.kafka.connect.datagen.DatagenConnector",
      "producer.interceptor.classes": "io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor",
      "tasks.max": "3",
      "key.converter": "io.confluent.connect.avro.AvroConverter",
      "value.converter": "io.confluent.connect.avro.AvroConverter",
      "kafka.topic": "iiot.simulated",
      "max.interval": "5000",
      "iterations": "10000",
      "schema.string": "{   \"namespace\": \"iiot\",   \"name\": \"IIoT\",   \"doc\": \"Defines a hypothetical IIoT Data\",   \"type\": \"record\",   \"fields\": [     {       \"name\": \"status\",       \"doc\": \"Device Status\",       \"type\": {         \"type\": \"string\",         \"arg.properties\": {           \"options\": [ \"ON\", \"OFF\", \"UNKNOWN\"  ]         }       }     },     {       \"name\": \"temperature\",       \"doc\": \"Temperature read by sensor\",       \"type\": {         \"type\": \"int\",         \"arg.properties\": {           \"range\": {             \"min\": 20,             \"max\": 50           }         }       }     },     {       \"name\": \"sensorid\",       \"doc\": \"Sensor Id\",       \"type\": {         \"type\": \"string\",         \"arg.properties\": {           \"options\": [ \"ZBZX\", \"ZJZZT\", \"ZTEST\", \"ZVV\", \"ZVZZT\", \"ZWZZT\", \"ZXZZT\" ]         }       }     },     {       \"name\": \"speed\",       \"doc\": \"Speed of the Vehicle carrying the sensor\",       \"type\": {         \"type\": \"int\",         \"arg.properties\": {           \"range\": {             \"min\": 0,             \"max\": 100           }         }       }     },     {       \"name\": \"doorstatus\",       \"doc\": \"Cargo Door Satus\",       \"type\": {         \"type\": \"string\",         \"arg.properties\": {           \"options\": [ \"OPEN\", \"CLOSED\" ]         }       }     },     {       \"name\": \"truckid\",        \"doc\": \"The simulated truck carrying the sensor\",       \"type\": {         \"type\": \"string\",         \"arg.properties\": {             \"regex\": \"Truck_[1-9]{0,1}\"         }       }     }   ] }",
      "schema.keyfield": "truckid",
      "value.converter.schema.registry.url": "http://schema-registry:8081",
      "key.converter.schema.registry.url": "http://schema-registry:8081"
  }
}
EOF
)

docker-compose exec connect curl -X POST -H "${HEADER}" --data "${DATA}" http://localhost:8083/connectors
