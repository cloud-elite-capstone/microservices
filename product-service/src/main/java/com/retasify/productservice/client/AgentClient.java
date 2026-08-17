package com.retasify.productservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class AgentClient {
    private final RestClient restClient;

    public AgentClient(RestClient.Builder builder,
                       @Value("${agent.service.url:http://localhost:8085}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public Map<String, Object> recommend(Map<String, Object> payload) {
        return restClient.post()
                .uri("/agent/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}