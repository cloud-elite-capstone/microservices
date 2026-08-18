package com.retasify.productservice.dto;

import java.util.List;

public record AgentSearchResponse(
        List<AgentRecommendation> recommendations
) {}
