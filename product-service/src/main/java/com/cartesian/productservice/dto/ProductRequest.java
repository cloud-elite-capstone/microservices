package com.cartesian.productservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.locationtech.jts.geom.Point;

public record ProductRequest(
        @NotBlank(message = "Product name is required")
        String name,

        String description,

        @NotNull(message = "Product price is required")
        @PositiveOrZero(message = "Price must be zero or positive")
        BigDecimal price,

        @PositiveOrZero(message = "Shipping fee must be zero or positive")
        BigDecimal shippingFee,

        int quantity,

        Point location,

        @NotNull(message = "Category id is required")
        UUID categoryId,

        String imageUrl,

        @NotNull(message = "Shop id is required")
        UUID shopId
) {}
