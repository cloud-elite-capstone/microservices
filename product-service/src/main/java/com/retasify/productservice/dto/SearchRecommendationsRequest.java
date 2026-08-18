package com.retasify.productservice.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotEmpty;

public record SearchRecommendationsRequest(
        @JsonProperty("search_keywords")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        @NotEmpty(message = "At least one search keyword is required")
        List<String> searchKeywords,

        JsonNode location,

        String budget,

        @JsonProperty("image_url") String imageUrl
) {}
