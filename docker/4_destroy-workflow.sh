#!/bin/bash
echo "Destroying docker..."
echo
docker-compose down || exit 1

# Log into Confluent Cloud CLI
echo "Logging in..."
ccloud login --save || exit 1

#### Teardown ####

echo
read -p "This script will remove Replicator and destroy the Confluent Cloud environment for service account ID $SERVICE_ACCOUNT_ID.  Do you want to proceed? [y/n] " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
  echo
  echo "--> Don't forget to destroy your Confluent Cloud environment, which may accrue hourly charges even if you are not actively using it."
  echo
  exit 1
fi

echo "Destroying cloud instance...."
source ./ccloud_library.sh
export SERVICE_ACCOUNT_ID=$(ccloud kafka cluster list -o json | jq -r '.[0].name' | awk -F'-' '{print $4;}')
ccloud::destroy_ccloud_stack $SERVICE_ACCOUNT_ID
echo "Destroyed. Please validate by login to Confluent Cloud."
echo "***IMPORTANT****: DO NOT FORGET TO DELETE LOCAL FILES ~/.netrc, ./stack-configs, ./delta_configs"
