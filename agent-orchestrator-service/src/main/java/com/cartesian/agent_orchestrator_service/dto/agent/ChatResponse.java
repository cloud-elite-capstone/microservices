package com.cartesian.agent_orchestrator_service.dto.agent;

import com.cartesian.agent_orchestrator_service.dto.product.ProductDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    @JsonProperty("session_id")
    private UUID sessionId;

    private String reply;

    @Builder.Default
    @JsonProperty("referenced_products")
    private List<ProductDto> referencedProducts = new ArrayList<>();
}
