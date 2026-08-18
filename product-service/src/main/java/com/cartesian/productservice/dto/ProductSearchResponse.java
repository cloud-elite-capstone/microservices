package com.cartesian.productservice.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductSearchResponse(
        @JsonProperty("item_search_keyword") String itemSearchKeyword,
        @JsonProperty("recommendation_count") int recommendationCount,
        List<Recommendation> recommendations
) {}
