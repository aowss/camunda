package com.micasa.tutorial.config;

import io.camunda.common.auth.*;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.exception.TaskListException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConfigurationProperties(prefix = "tasklist")
public class TaskListClientConfig {

    private String url;

    private String keycloakUrl;

    private String clientId;

    private String clientSecret;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKeycloakUrl() {
        return keycloakUrl;
    }

    public void setKeycloakUrl(String keycloakUrl) {
        this.keycloakUrl = keycloakUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Bean
    public CamundaTaskListClient camundaTaskListClient() throws TaskListException {
        JwtConfig jwtConfig = new JwtConfig();
        JwtCredential jwtCredential = new JwtCredential(clientId, clientSecret, null, null);
        jwtConfig.addProduct(Product.TASKLIST, jwtCredential);

        Authentication auth = SelfManagedAuthentication.builder()
                                  .keycloakUrl(keycloakUrl)
                                  .jwtConfig(jwtConfig)
                                  .build();

        var client = CamundaTaskListClient.builder()
                   .taskListUrl(url)
                   .shouldReturnVariables()
                   .shouldLoadTruncatedVariables()
                   .authentication(auth)
                   .build();

        return client;
    }

}