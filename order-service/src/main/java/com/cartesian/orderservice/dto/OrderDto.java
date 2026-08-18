package com.cartesian.orderservice.dto;

import com.cartesian.orderservice.model.Order;
import com.cartesian.orderservice.model.OrderItems;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderDto {

    private UUID id;
    private UUID shopId;
    private UUID buyerId;
    private LocalDateTime createdOn;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private List<OrderItemsDto> items = new ArrayList<>();

    public OrderDto() {
    }

    public static OrderDto fromEntity(Order order, List<OrderItems> items) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setShopId(order.getShopId());
        dto.setBuyerId(order.getBuyerId());
        dto.setCreatedOn(order.getCreatedOn());
        dto.setStatus(order.getStatus());
        dto.setSubtotal(order.getSubtotal());
        dto.setShippingFee(order.getShippingFee());
        dto.setTotal(order.getTotal());
        if (items != null) {
            for (OrderItems item : items) {
                dto.getItems().add(OrderItemsDto.fromEntity(item));
            }
        }
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getShopId() {
        return shopId;
    }

    public void setShopId(UUID shopId) {
        this.shopId = shopId;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(UUID buyerId) {
        this.buyerId = buyerId;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<OrderItemsDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemsDto> items) {
        this.items = items;
    }
}
