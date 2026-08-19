package com.cartesian.agentservice.dto;

import java.math.BigDecimal;
import java.util.UUID;
import org.locationtech.jts.geom.Point;

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

    public ProductDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public UUID getShopId() {
        return shopId;
    }

    public void setShopId(UUID shopId) {
        this.shopId = shopId;
    }
}
