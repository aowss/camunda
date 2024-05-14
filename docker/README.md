<!-- TOC -->
* [Local Development Environment](#local-development-environment)
  * [Pre-requisites](#pre-requisites)
  * [Set up](#set-up)
    * [Complete Platform](#complete-platform)
    * [Core Components](#core-components)
      * [Exceptions](#exceptions)
<!-- TOC -->

# Local Development Environment

Most of the details here are documented in more details in the [Camunda Platform 8 repository](https://github.com/camunda/camunda-platform).

## Pre-requisites

* [Docker compose](https://docs.docker.com/compose/)

## Set up

The following files were downloaded from the [Camunda Platform 8 repository](https://github.com/camunda/camunda-platform), more precisely from the [`8.4` release branch](https://github.com/camunda/camunda-platform/tree/stable/8.4):

    .env
    docker-compose.yaml
    docker-compose-core.yaml
    connector-secrets.txt

The following properties were added to the `docker-compose.yaml` and `docker-compose-core.yaml` files to only run the REST connector as part of the `connectors` service:

```yaml
      - CONNECTOR_REST_FUNCTION=io.camunda.connector.http.rest.HttpJsonFunction
      - CONNECTOR_REST_TYPE=io.camunda:http-json:1
      - CONNECTOR_REST_INPUT_VARIABLES=url, method, authentication, headers, queryParameters, connectionTimeoutInSeconds, readTimeoutInSeconds, writeTimeoutInSeconds, body
      - CONNECTOR_REST_TIMEOUT=300000
```

### Complete Platform

Run the following command:

> `docker compose up`

The following containers should be started:

    connectors
    elasticsearch
    identity
    keycloak
    operate
    postgres
    tasklist
    zeebe
    kibana
    optimize

As mentioned in their documentation:

* Now you can navigate to the different web apps and log in with the user `demo` and password `demo`:  

Operate: http://localhost:8081  
Tasklist: http://localhost:8082  
Optimize: http://localhost:8083  
Identity: http://localhost:8084  
Elasticsearch: http://localhost:9200  

* Keycloak is used to manage users. Here you can log in with the user `admin` and password `admin`:  

Keycloak: http://localhost:18080/auth/

* The workflow engine Zeebe is available using gRPC at `localhost:26500`.

### Core Components

It is also possible to run only the core components, i.e. not Optimize or Identity and therefore not Keycloack & Postgres, using the following command:

> `docker compose --file docker-compose-core.yaml up`

The web apps are accessible in the same way as before, i.e. using `demo`/`demo` at:

Operate: http://localhost:8081  
Tasklist: http://localhost:8082  
Elasticsearch: http://localhost:9200 

#### Exceptions

You can find a sample startup logs in the [`core.log` file](./core.log).  

As you can see, the Connectors component will throw the following exceptions:

  `connectors     | Caused by: org.apache.hc.client5.http.HttpHostConnectException: Connect to http://operate:8080 [operate/172.20.0.5] failed: Connection refused`

until Operate is fully operational, i.e. the following log is printed:

  `operate        | 2024-05-14 14:59:01.129  INFO 7 --- [nio-8080-exec-1] o.s.w.s.DispatcherServlet                : Completed initialization in 3 ms`

