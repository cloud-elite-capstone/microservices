package com.cartesian.agent_orchestrator_service.dto.agent;

import com.cartesian.agent_orchestrator_service.dto.product.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationItemDto {
    private String name;
    private ProductDto product;
}
