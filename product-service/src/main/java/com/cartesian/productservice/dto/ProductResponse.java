package com.cartesian.productservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import org.locationtech.jts.geom.Point;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        BigDecimal shippingFee,
        int quantity,
        Point location,
        UUID categoryId,
        String imageUrl,
        UUID shopId
) {}
