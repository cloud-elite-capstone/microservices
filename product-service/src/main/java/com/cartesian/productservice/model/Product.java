package com.cartesian.productservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "price", nullable = true)
    private BigDecimal price;

    @Column(name = "shipping_fee", nullable = true)
    private BigDecimal shippingFee;

    @Column(name = "quantity", nullable = true)
    private int quantity;

    @Column(name = "location")
    private Point location;

    @Column(name = "category_id", nullable = true)
    private UUID categoryId;

    @Column(name = "image_url", nullable = true)
    private String imageUrl;

    @Column(name = "shop_id", nullable = true)
    private UUID shopId;

    public Product() {
    }

    public Product(UUID id, String name, String description, BigDecimal price, BigDecimal shippingFee, int quantity,
                  Point location, UUID categoryId, String imageUrl, UUID shopId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.shippingFee = shippingFee;
        this.quantity = quantity;
        this.location = location;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
        this.shopId = shopId;
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
