package com.cartesian.agentservice.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ProductSearchResponse(
        @JsonProperty("item_search_keyword") String itemSearchKeyword,
        @JsonProperty("recommendation_count") int recommendationCount,
        List<Recommendation> recommendations
) {}
