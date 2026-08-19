package com.cartesian.agent_orchestrator_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient geocoderWebClient(WebClient.Builder builder, @Value("${adapter.geocoder.url}") String url) {
        return builder.clone()
                .baseUrl(url)
                .defaultHeader("User-Agent", "cartesian-agent-orchestrator-service")
                .build();
    }

    @Bean
    public WebClient serpApiWebClient(WebClient.Builder builder, @Value("${adapter.serpapi.url}") String url) {
        return builder.clone().baseUrl(url).build();
    }
}

