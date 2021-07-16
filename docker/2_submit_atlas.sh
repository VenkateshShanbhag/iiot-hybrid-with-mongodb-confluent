#!/bin/bash
echo
echo "Creating MongoAtlas Connector in Confluent Cloud"

HEADER="Content-Type: application/json"
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
source ${DIR}/delta_configs/env.delta
echo "
{
    "\"name\"": "\"mongodb-sink\"",
    "\"connector.class\"": "\"MongoDbAtlasSink\"",
    "\"kafka.api.key\"": "\"${CLOUD_KEY}\"",
    "\"kafka.api.secret\"": "\"${CLOUD_SECRET}\"",
    "\"input.data.format\"" : "\"STRING\"",
    "\"connection.host\"": "\"${MONGODB_HOST}\"",
    "\"connection.user\"": "\"${MONGODB_USER}\"",
    "\"connection.password\"": "\"${MONGODB_PASSWORD}\"",
    "\"topics\"": "\"iiot.simulated.processed\"",
    "\"database\"": "\"iiot\"",
    "\"collection\"": "\"iiot-simulated\"",
    "\"doc.id.strategy\"": "\"PartialKeyStrategy\"",
    "\"doc.id.strategy.overwrite.existing\"": "\"false\"",
    "\"key.projection.type\"": "\"allowlist\"",
    "\"tasks.max\"": "\"1\""
} " > ./connect.json

ccloud connector create --config ./connect.json --output human
echo
rm ./connect.json
echo "Sleep for 120s to wait for all Confluent Cloud metadata to propagate"
sleep 120
