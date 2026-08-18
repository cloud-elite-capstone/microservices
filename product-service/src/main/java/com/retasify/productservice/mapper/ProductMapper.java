package com.retasify.productservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import com.retasify.productservice.dto.ProductRequest;
import com.retasify.productservice.dto.ProductResponse;
import com.retasify.productservice.model.Product;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {
    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product product);
}
