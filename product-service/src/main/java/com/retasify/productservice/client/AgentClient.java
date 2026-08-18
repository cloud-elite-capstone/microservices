package com.retasify.productservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.retasify.productservice.dto.AgentSearchRequest;
import com.retasify.productservice.dto.AgentSearchResponse;

@Component
public class AgentClient {
    private final RestClient restClient;

    public AgentClient(RestClient.Builder builder,
                       @Value("${agent.service.url:http://localhost:8085}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public AgentSearchResponse recommend(AgentSearchRequest request) {
        return restClient.post()
                .uri("/agent/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AgentSearchResponse.class);
    }
}