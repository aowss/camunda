# Spring Boot Samples

## Run Locally

The [Spring Boot Maven Plugin](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/) is configured in this [`pom.xml`](./pom.xml) to use the `local` profile by default.

In each module, the `application-local.yaml` file is configured to run against a local deployment of Camunda.  
The [`Camunda Platform 8` repository](https://github.com/camunda/camunda-platform) documents how to run a complete Camunda Platform 8 Self-Managed environment locally.

The following command will therefore run using that profile.

> `mvn spring-boot:run`

It is possible to run using another profile by setting the `spring-boot.run.profiles` property, e.g. `-Dspring-boot.run.profiles=dev`.