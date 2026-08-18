package com.retasify.productservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;

public record Recommendation(
        String name,
        String rationale,
        BigDecimal totalLandedCost,
        JsonNode product
) {}
