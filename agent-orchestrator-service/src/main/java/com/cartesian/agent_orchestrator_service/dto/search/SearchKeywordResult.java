package com.cartesian.agent_orchestrator_service.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SearchKeywordResult(
        @JsonProperty("item_name") String itemName,
        @JsonProperty("no_of_agent_recommendations") int noOfAgentRecommendations,
        @JsonProperty("Items") List<Recommendation> items
) {}
