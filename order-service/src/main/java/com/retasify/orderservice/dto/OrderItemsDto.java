package com.retasify.orderservice.dto;

import com.retasify.orderservice.model.OrderItems;
import java.math.BigDecimal;
import java.util.UUID;

public class OrderItemsDto {

    private UUID id;
    private UUID productId;
    private BigDecimal subtotal;
    private int quantity;

    public OrderItemsDto() {
    }

    public static OrderItemsDto fromEntity(OrderItems item) {
        OrderItemsDto dto = new OrderItemsDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setSubtotal(item.getSubtotal());
        dto.setQuantity(item.getQuantity());
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
