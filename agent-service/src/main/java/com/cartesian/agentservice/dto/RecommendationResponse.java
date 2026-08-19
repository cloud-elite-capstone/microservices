package com.cartesian.agentservice.dto;

import java.util.ArrayList;
import java.util.List;

public class RecommendationResponse {

    private List<RecommendationItemDto> recommendations = new ArrayList<>();

    public RecommendationResponse() {
    }

    public RecommendationResponse(List<RecommendationItemDto> recommendations) {
        this.recommendations = recommendations;
    }

    public List<RecommendationItemDto> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RecommendationItemDto> recommendations) {
        this.recommendations = recommendations;
    }
}
