package com.retasify.productservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;

public record AgentRecommendation(
        String name,
        String rationale,
        BigDecimal totalLandedCost,
        JsonNode product
) {}
