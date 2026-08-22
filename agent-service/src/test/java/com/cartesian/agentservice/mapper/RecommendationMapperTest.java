package com.cartesian.agentservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cartesian.agentservice.dto.ProductDto;
import com.cartesian.agentservice.dto.RecommendationItemDto;
import com.cartesian.agentservice.dto.search.Recommendation;
import com.fasterxml.jackson.databind.ObjectMapper;

class RecommendationMapperTest {

    private RecommendationMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mapper = new RecommendationMapperImpl();
    }

    private ProductDto product(String name, String price, String shippingFee) {
        ProductDto product = new ProductDto();
        product.setName(name);
        if (price != null) {
            product.setPrice(new BigDecimal(price));
        }
        if (shippingFee != null) {
            product.setShippingFee(new BigDecimal(shippingFee));
        }
        return product;
    }

    @Test
    void toRecommendation_withValidItem_mapsFieldsCorrectly() {
        ProductDto product = product("Test Product", "100.00", "15.50");
        product.setId(UUID.randomUUID());

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
        ProductDto product = product("Fallback Product Name", "50.00", null);
        product.setId(UUID.randomUUID());

        RecommendationItemDto item = new RecommendationItemDto(null, product);

        Recommendation result = mapper.toRecommendation(item, objectMapper);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Fallback Product Name");
        assertThat(result.totalLandedCost()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void toRecommendation_withBlankName_fallsBackToProductName() {
        ProductDto product = product("Fallback Product Name", "50.00", null);
        product.setId(UUID.randomUUID());

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
        ProductDto product = product("Product 1", "20.00", null);

        RecommendationItemDto item = new RecommendationItemDto("Item 1", product);

        List<Recommendation> result = mapper.toRecommendationList(Arrays.asList(item, null), objectMapper);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Item 1");
    }

    @Test
    void toRecommendationList_withNullList_returnsEmptyList() {
        List<Recommendation> result = mapper.toRecommendationList(null, objectMapper);
        assertThat(result).isEmpty();
    }
}
