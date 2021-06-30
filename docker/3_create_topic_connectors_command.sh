#!/bin/bash

source ./delta_configs/env.delta

echo "***** Creating iiot.simulated topic with 3 partitions ********"
docker-compose exec broker kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1  --partitions 3 --topic 'iiot.simulated' || exit 1

echo
echo "*************** Sleeping for 30s to propagate changes *****************************"
sleep 30

echo
echo "************** Creating replicator ***************************"

./3a_submit_replicator.sh || exit 1

echo
echo "************** Sleeping for 60s to propagate changes ************************"
sleep 60

echo
echo "************* Creating iiot datagen connector **********"
./3b_submit_datagen.sh || exit 1

echo
echo "************** Script ended ********************"
