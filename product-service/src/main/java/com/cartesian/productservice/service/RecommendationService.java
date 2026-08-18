package com.cartesian.productservice.service;

import com.cartesian.productservice.client.AgentClient;
import com.cartesian.productservice.client.dto.agent.AgentSearchRequest;
import com.cartesian.productservice.client.dto.agent.AgentSearchResponse;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {
    private final AgentClient agentClient;

    public RecommendationService(
            AgentClient agentClient
    ) {
        this.agentClient = agentClient;
    }

    public AgentSearchResponse getRecommendations(AgentSearchRequest request) {
        return agentClient.recommend(request);
    }
}
