package com.retasify.productservice.dto;

import com.retasify.productservice.model.Product;
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

    public static ProductDto fromEntity(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setShippingFee(product.getShippingFee());
        dto.setQuantity(product.getQuantity());
        dto.setLocation(product.getLocation());
        dto.setCategoryId(product.getCategoryId());
        dto.setImageUrl(product.getImageUrl());
        dto.setShopId(product.getShopId());
        return dto;
    }

    public Product toEntity() {
        Product product = new Product();
        product.setId(this.id);
        product.setName(this.name);
        product.setDescription(this.description);
        product.setPrice(this.price);
        product.setShippingFee(this.shippingFee);
        product.setQuantity(this.quantity);
        product.setLocation(this.location);
        product.setCategoryId(this.categoryId);
        product.setImageUrl(this.imageUrl);
        product.setShopId(this.shopId);
        return product;
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
