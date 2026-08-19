package com.cartesian.agent_orchestrator_service.dto.agent;

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
public class RecommendationResponse {
    @Builder.Default
    private List<RecommendationItemDto> recommendations = new ArrayList<>();
}
