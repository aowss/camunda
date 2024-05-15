# Mock API Server

This is used to mock external APIs that might be used in the samples.

We use the [Wiremock Docker image](https://wiremock.org/docs/standalone/docker/).

As mentioned in the documentation, the server can be started as follows:

> `docker run -it --rm -p 9999:8080 -v $PWD:/home/wiremock --name mock-api-server wiremock/wiremock`

The mock server is configured to respond without error to the external API calls.  

You can customize the responses by editing the files located in the [`__files`](./__files) and [`mappings`](./mappings) folders.

