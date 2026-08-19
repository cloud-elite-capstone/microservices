package com.cartesian.agent_orchestrator_service.dto.product;

import java.math.BigDecimal;
import java.util.UUID;
import org.locationtech.jts.geom.Point;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal shippingFee;
    private int quantity;
    private Point location;
    private UUID categoryId;
    private String imageUrl;
    private UUID shopId;
}
