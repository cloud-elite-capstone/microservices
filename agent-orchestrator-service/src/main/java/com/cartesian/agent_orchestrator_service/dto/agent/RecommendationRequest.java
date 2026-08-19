package com.cartesian.agent_orchestrator_service.dto.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    @Builder.Default
    @JsonProperty("search_for")
    private List<SearchItemCriteria> searchFor = new ArrayList<>();
}
