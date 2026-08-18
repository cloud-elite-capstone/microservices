package com.cartesian.productservice.client.dto.agent;

import java.util.List;

public record AgentSearchResponse(
        List<AgentRecommendation> recommendations
) {}
