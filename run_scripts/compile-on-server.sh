#!/usr/bin/env bash
docker exec simsql_container mvn -f ./simsql/pom.xml generate-sources compile assembly:single