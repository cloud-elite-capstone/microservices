package com.cartesian.productservice.dto;

import java.util.List;

public record AgentSearchResponse(
        List<AgentRecommendation> recommendations
) {}
