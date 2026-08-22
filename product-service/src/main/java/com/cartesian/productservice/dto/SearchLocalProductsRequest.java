package com.cartesian.productservice.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Size;

public record SearchLocalProductsRequest(
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        @Size(max = 100, message = "Search query must not exceed 100 characters")
        String query
) {
    public SearchLocalProductsRequest {
        query = (query == null) ? "" : query.trim();
    }
}
