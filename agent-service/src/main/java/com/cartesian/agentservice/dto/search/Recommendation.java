package com.cartesian.agentservice.dto.search;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;

public record Recommendation(
        String name,
        String rationale,
        BigDecimal totalLandedCost,
        JsonNode product
) {}
