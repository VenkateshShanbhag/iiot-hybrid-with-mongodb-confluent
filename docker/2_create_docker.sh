#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
source ${DIR}/delta_configs/env.delta

echo
docker-compose up -d
echo
echo "Sleep an additional 120s to wait for docker containers to start"
sleep 120
echo "..."
