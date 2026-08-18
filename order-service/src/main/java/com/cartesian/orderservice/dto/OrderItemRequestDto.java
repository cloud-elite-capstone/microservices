package com.cartesian.orderservice.dto;

import java.util.UUID;

public class OrderItemRequestDto {

    private UUID itemId;
    private int quantity;

    public OrderItemRequestDto() {
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
