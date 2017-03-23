#!/usr/bin/env bash
docker run -p 8088:8088 -p 50070:50070 -p 5432:5432 -p 9696:9696 --name simsql_container -i -t dimitrijejankov/simsql-debug /etc/bootstrap.sh -bash