package com.cartesian.agent_orchestrator_service.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SearchResultsResponse(
        @JsonProperty("search_results") List<SearchKeywordResult> searchResults
) {}
