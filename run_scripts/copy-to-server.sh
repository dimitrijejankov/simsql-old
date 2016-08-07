#!/usr/bin/env bash
docker exec simsql_container rm -rf /tmp/simsql/
docker cp ../ simsql_container:/tmp/simsql
docker exec simsql_container cp -rfT /tmp/simsql/ /simsql && rm -rf /temp/simsql