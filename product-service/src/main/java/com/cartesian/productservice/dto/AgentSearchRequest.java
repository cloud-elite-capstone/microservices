package com.cartesian.productservice.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgentSearchRequest(
        @JsonProperty("search_for") List<AgentSearchItem> searchFor
) {}
