# Local Development Environment

## Pre-requisites

* [Docker compose](https://docs.docker.com/compose/)

## Set up

The following files were downloaded from the [Camunda Platform 8 repository](https://github.com/camunda/camunda-platform), more precisely from the [`8.4.5` release branch](https://github.com/camunda/camunda-platform/tree/8.4.5):

    .env
    docker-compose.yaml
    connector-secrets.txt

Run the following command:

> `docker-compose up`

The following containers should be started:

    connectors
    elasticsearch
    identity
    keycloak
    operate
    optimize
    postgres
    tasklist
    zeebe

