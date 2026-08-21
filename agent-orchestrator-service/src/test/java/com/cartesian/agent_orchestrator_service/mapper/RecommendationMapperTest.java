package com.cartesian.agent_orchestrator_service.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cartesian.agent_orchestrator_service.dto.agent.RecommendationItemDto;
import com.cartesian.agent_orchestrator_service.dto.product.ProductDto;
import com.cartesian.agent_orchestrator_service.dto.search.Recommendation;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationMapperTest {

    private RecommendationMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mapper = new RecommendationMapperImpl();
    }

    @Test
    void toRecommendation_withValidItem_mapsFieldsCorrectly() {
        ProductDto product = ProductDto.builder()
                .id(UUID.randomUUID())
                .name("Test Product")
                .price(new BigDecimal("100.00"))
                .shippingFee(new BigDecimal("15.50"))
                .build();

        RecommendationItemDto item = new RecommendationItemDto("Custom Recommendation Name", product);

        Recommendation result = mapper.toRecommendation(item, objectMapper);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Custom Recommendation Name");
        assertThat(result.rationale()).isEqualTo("AI recommended matching candidate");
        assertThat(result.totalLandedCost()).isEqualByComparingTo(new BigDecimal("115.50"));
        assertThat(result.product()).isNotNull();
        assertThat(result.product().get("name").asText()).isEqualTo("Test Product");
    }

    @Test
    void toRecommendation_withNullName_fallsBackToProductName() {
        ProductDto product = ProductDto.builder()
                .id(UUID.randomUUID())
                .name("Fallback Product Name")
                .price(new BigDecimal("50.00"))
                .build();

        RecommendationItemDto item = new RecommendationItemDto(null, product);

        Recommendation result = mapper.toRecommendation(item, objectMapper);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Fallback Product Name");
        assertThat(result.totalLandedCost()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void toRecommendation_withBlankName_fallsBackToProductName() {
        ProductDto product = ProductDto.builder()
                .id(UUID.randomUUID())
                .name("Fallback Product Name")
                .price(new BigDecimal("50.00"))
                .build();

        RecommendationItemDto item = new RecommendationItemDto("   ", product);

        Recommendation result = mapper.toRecommendation(item, objectMapper);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Fallback Product Name");
    }

    @Test
    void toRecommendation_withNullItem_returnsNull() {
        Recommendation result = mapper.toRecommendation(null, objectMapper);
        assertThat(result).isNull();
    }

    @Test
    void toRecommendationList_withNullAndValidElements_filtersNullSafely() {
        ProductDto product = ProductDto.builder()
                .name("Product 1")
                .price(new BigDecimal("20.00"))
                .build();

        RecommendationItemDto item = new RecommendationItemDto("Item 1", product);

        List<Recommendation> result = mapper.toRecommendationList(java.util.Arrays.asList(item, null), objectMapper);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Item 1");
    }

    @Test
    void toRecommendationList_withNullList_returnsEmptyList() {
        List<Recommendation> result = mapper.toRecommendationList(null, objectMapper);
        assertThat(result).isEmpty();
    }
}
