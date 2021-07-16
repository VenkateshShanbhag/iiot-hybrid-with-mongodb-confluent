#!/bin/bash

echo
echo " WARNING: ********* You get an error prompt_continue_ccloud_demo': not a valid identifier then make sure you are in bash shell *********"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
VALIDATE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
echo

echo "** Step 1 - Download ccloud_library and Log in to Confluent Cloud **"
curl -sS -o ccloud_library.sh https://raw.githubusercontent.com/confluentinc/examples/latest/utils/ccloud_library.sh
source ${DIR}/ccloud_library.sh
echo
echo "** Checking ccloud update .... **"
ccloud update
ccloud::prompt_continue_ccloud_demo || exit 1

# Log into Confluent Cloud CLI
echo
ccloud login --save || exit 1

echo "** Step 2 - Create a stack in Confluent Cloud **" 
echo
export EXAMPLE="cp-demo"
ccloud::create_ccloud_stack true || exit 1
export SERVICE_ACCOUNT_ID=$(ccloud kafka cluster list -o json | jq -r '.[0].name' | awk -F'-' '{print $4;}')
if [[ "$SERVICE_ACCOUNT_ID" == "" ]]; then
  echo "ERROR: Could not determine SERVICE_ACCOUNT_ID from 'ccloud kafka cluster list'. Please troubleshoot, destroy stack, and try again to create the stack."
  exit 1
fi
CONFIG_FILE=stack-configs/java-service-account-$SERVICE_ACCOUNT_ID.config
CCLOUD_CLUSTER_ID=$(ccloud kafka cluster list -o json | jq -c -r '.[] | select (.name == "'"demo-kafka-cluster-$SERVICE_ACCOUNT_ID"'")' | jq -r .id)
echo
echo "Sleep an additional 120s to wait for all Confluent Cloud metadata to propagate"
sleep 120
# Create parameters customized for Confluent Cloud instance created above
ccloud::generate_configs $CONFIG_FILE
source "delta_configs/env.delta"
echo
echo "Sleep an additional 60s to wait for all Confluent Cloud metadata to propagate"
sleep 60

echo " ** Step 3 - Set a Cluster in Confluent Cloud **"
echo ====== Set Kafka cluster and service account
ccloud::set_kafka_cluster_use_from_api_key $CLOUD_KEY || exit 1

echo "** Starting Step 4 - Validate Confluent Cloud **":  
echo ====== Validate credentials to Confluent Cloud Schema Registry
ccloud::validate_schema_registry_up $SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO $SCHEMA_REGISTRY_URL || exit 1
printf "Done\n\n"

echo "** Step 5 - Create Topics, ACL and Set to Service Account in Confluend Cloud **"
echo ====== Create topic users and set ACLs in Confluent Cloud cluster
# users
ccloud kafka topic create 'iiot.simulated'
ccloud::create_acls_replicator $SERVICE_ACCOUNT_ID 'iiot.simulated'
ccloud kafka topic create 'iiot.simulated.processed'
ccloud::create_acls_replicator $SERVICE_ACCOUNT_ID 'iiot.simulated.processed'

printf "\n"

echo "********** Script Ended *********"
