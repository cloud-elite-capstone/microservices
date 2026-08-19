package com.cartesian.agent_orchestrator_service.dto.search;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;

public record Recommendation(
        String name,
        String rationale,
        BigDecimal totalLandedCost,
        JsonNode product
) {}
