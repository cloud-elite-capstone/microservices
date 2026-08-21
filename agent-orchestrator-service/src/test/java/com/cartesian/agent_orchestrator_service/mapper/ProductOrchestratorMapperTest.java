package com.cartesian.agent_orchestrator_service.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cartesian.agent_orchestrator_service.dto.product.ProductDto;
import com.cartesian.agent_orchestrator_service.dto.search.Recommendation;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ProductOrchestratorMapperTest {

    private ProductOrchestratorMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mapper = new ProductOrchestratorMapperImpl();
    }

    @Test
    void toRecommendation_withValidProduct_mapsFieldsCorrectly() {
        ProductDto product = ProductDto.builder()
                .id(UUID.randomUUID())
                .name("Direct Product")
                .price(new BigDecimal("200.00"))
                .shippingFee(new BigDecimal("25.00"))
                .build();

        Recommendation result = mapper.toRecommendation(product, objectMapper);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Direct Product");
        assertThat(result.rationale()).isEqualTo("Result from local or external product search");
        assertThat(result.totalLandedCost()).isEqualByComparingTo(new BigDecimal("225.00"));
        assertThat(result.product()).isNotNull();
        assertThat(result.product().get("name").asText()).isEqualTo("Direct Product");
    }

    @Test
    void toRecommendation_withNullProduct_returnsNull() {
        Recommendation result = mapper.toRecommendation(null, objectMapper);
        assertThat(result).isNull();
    }

    @Test
    void toRecommendationList_withNullAndValidElements_filtersNullSafely() {
        ProductDto product = ProductDto.builder()
                .name("Product 1")
                .price(new BigDecimal("30.00"))
                .build();

        List<Recommendation> result = mapper.toRecommendationList(java.util.Arrays.asList(product, null), objectMapper);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Product 1");
    }

    @Test
    void toRecommendationList_withNullList_returnsEmptyList() {
        List<Recommendation> result = mapper.toRecommendationList(null, objectMapper);
        assertThat(result).isEmpty();
    }
}
