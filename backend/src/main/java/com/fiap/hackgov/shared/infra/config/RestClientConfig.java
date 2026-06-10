package com.fiap.hackgov.shared.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient authRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8080")
                .build();
    }
}
