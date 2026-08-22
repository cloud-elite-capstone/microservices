package com.cartesian.agentservice.dto.search;

import java.math.BigDecimal;
import java.util.UUID;
import org.locationtech.jts.geom.Polygon;

public record SearchCriteria(
        String search,
        String locationText,
        Polygon locationPolygon,
        BigDecimal minBudget,
        BigDecimal maxBudget,
        Double minRating,
        Double maxRating,
        UUID sourceShop,
        String imageUrl
) {}
