package com.cartesian.agentservice.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import com.cartesian.agentservice.dto.ProductDto;
import com.cartesian.agentservice.dto.search.Recommendation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductOrchestratorMapper {

    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "rationale", constant = "Result from local or external product search")
    @Mapping(target = "totalLandedCost", source = "product", qualifiedByName = "calculateLandedCost")
    @Mapping(target = "product", source = "product", qualifiedByName = "toJsonNode")
    Recommendation toRecommendation(ProductDto product, @Context ObjectMapper objectMapper);

    default List<Recommendation> toRecommendationList(List<ProductDto> products, @Context ObjectMapper objectMapper) {
        if (products == null) {
            return List.of();
        }
        return products.stream()
                .filter(Objects::nonNull)
                .map(product -> toRecommendation(product, objectMapper))
                .filter(Objects::nonNull)
                .toList();
    }

    @Named("calculateLandedCost")
    default BigDecimal calculateLandedCost(ProductDto product) {
        if (product == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        BigDecimal shipping = product.getShippingFee() != null ? product.getShippingFee() : BigDecimal.ZERO;
        return price.add(shipping);
    }

    @Named("toJsonNode")
    default JsonNode toJsonNode(ProductDto product, @Context ObjectMapper objectMapper) {
        if (product == null || objectMapper == null) {
            return null;
        }
        return objectMapper.valueToTree(product);
    }
}
