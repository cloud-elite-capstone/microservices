package com.cartesian.agentservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cartesian.agentservice.dto.ProductDto;
import com.cartesian.agentservice.dto.search.Recommendation;
import com.fasterxml.jackson.databind.ObjectMapper;

class ProductOrchestratorMapperTest {

    private ProductOrchestratorMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mapper = new ProductOrchestratorMapperImpl();
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
    void toRecommendation_withValidProduct_mapsFieldsCorrectly() {
        ProductDto product = product("Direct Product", "200.00", "25.00");
        product.setId(UUID.randomUUID());

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
        ProductDto product = product("Product 1", "30.00", null);

        List<Recommendation> result = mapper.toRecommendationList(Arrays.asList(product, null), objectMapper);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Product 1");
    }

    @Test
    void toRecommendationList_withNullList_returnsEmptyList() {
        List<Recommendation> result = mapper.toRecommendationList(null, objectMapper);
        assertThat(result).isEmpty();
    }
}
