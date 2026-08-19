package com.cartesian.agent_orchestrator_service.dto.agent;

import com.cartesian.agent_orchestrator_service.dto.product.ProductDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
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
public class SearchItemCriteria {
    private String search;
    private Object location;
    private BigDecimal budget;

    @JsonProperty("image_url")
    private String imageUrl;

    @Builder.Default
    @JsonProperty("candidates")
    private List<ProductDto> candidates = new ArrayList<>();
}
