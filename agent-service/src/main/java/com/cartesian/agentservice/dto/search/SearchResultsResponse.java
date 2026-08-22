package com.cartesian.agentservice.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SearchResultsResponse(
        @JsonProperty("search_results") List<SearchKeywordResult> searchResults
) {}
