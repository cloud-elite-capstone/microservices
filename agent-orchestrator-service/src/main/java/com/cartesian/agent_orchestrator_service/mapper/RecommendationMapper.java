package com.cartesian.agent_orchestrator_service.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import com.cartesian.agent_orchestrator_service.dto.agent.RecommendationItemDto;
import com.cartesian.agent_orchestrator_service.dto.product.ProductDto;
import com.cartesian.agent_orchestrator_service.dto.search.Recommendation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RecommendationMapper {

    @Mapping(target = "name", source = "item", qualifiedByName = "resolveRecommendationName")
    @Mapping(target = "rationale", constant = "AI recommended matching candidate")
    @Mapping(target = "totalLandedCost", source = "item.product", qualifiedByName = "calculateLandedCost")
    @Mapping(target = "product", source = "item.product", qualifiedByName = "toJsonNode")
    Recommendation toRecommendation(RecommendationItemDto item, @Context ObjectMapper objectMapper);

    default List<Recommendation> toRecommendationList(List<RecommendationItemDto> items, @Context ObjectMapper objectMapper) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .filter(Objects::nonNull)
                .map(item -> toRecommendation(item, objectMapper))
                .filter(Objects::nonNull)
                .toList();
    }

    @Named("resolveRecommendationName")
    default String resolveRecommendationName(RecommendationItemDto item) {
        if (item == null) {
            return null;
        }
        if (item.getName() != null && !item.getName().isBlank()) {
            return item.getName();
        }
        if (item.getProduct() != null) {
            return item.getProduct().getName();
        }
        return null;
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

