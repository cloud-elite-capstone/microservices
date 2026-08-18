package com.cartesian.productservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import org.locationtech.jts.geom.Polygon;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgentSearchItem(
        String search,
        Polygon location,
        BigDecimal budget,
        @JsonProperty("min_budget") BigDecimal minBudget,
        @JsonProperty("min_rating") Double minRating,
        @JsonProperty("max_rating") Double maxRating,
        @JsonProperty("source_shop") UUID sourceShop,
        @JsonProperty("image_url") String imageUrl
) {}
