package com.cartesian.productservice.mapper;

import org.mapstruct.*;

import com.cartesian.productservice.dto.ProductRequest;
import com.cartesian.productservice.dto.ProductResponse;
import com.cartesian.productservice.model.Product;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = { UUID.class })
public interface ProductMapper {
    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product product);
}
