package com.cartesian.productservice.mapper;

import com.cartesian.productservice.client.dto.serpapi.SerpApiItem;
import org.mapstruct.*;

import com.cartesian.productservice.dto.ProductRequest;
import com.cartesian.productservice.dto.ProductResponse;
import com.cartesian.productservice.model.Product;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = { UUID.class })
public interface ProductMapper {
    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "snippet")
    @Mapping(target = "price", source = "rawPrice", qualifiedByName = "parseSerpPrice")
    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "quantity", constant = "0")
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "shopId", ignore = true)
    Product toEntity(SerpApiItem item);

    @Named("parseSerpPrice")
    default BigDecimal parseSerpPrice(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.replaceAll("[^0-9.]", "").trim());
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product product);
}
