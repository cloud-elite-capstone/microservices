package com.cartesian.productservice.mapper;

import com.cartesian.productservice.dto.ProductDto;
import org.mapstruct.*;

import com.cartesian.productservice.model.Product;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = { UUID.class })
public interface ProductMapper {
    ProductDto toDto(Product product);

    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductDto request);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(ProductDto request, @MappingTarget Product product);
}
