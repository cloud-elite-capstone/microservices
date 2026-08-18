package com.cartesian.productservice.client.dto.serpapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SerpApiResponse(
        @JsonProperty("shopping_results") List<SerpApiItem> shoppingResults,
        @JsonProperty("organic_results") List<SerpApiItem> organicResults
) {
    public List<SerpApiItem> getEffectiveResults() {
        if (shoppingResults != null && !shoppingResults.isEmpty()) {
            return shoppingResults;
        }
        if (organicResults != null && !organicResults.isEmpty()) {
            return organicResults;
        }
        return List.of();
    }
}
