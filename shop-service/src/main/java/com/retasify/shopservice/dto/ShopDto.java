package com.retasify.shopservice.dto;

import com.retasify.shopservice.model.Shop;
import java.util.UUID;
import org.locationtech.jts.geom.Point;

public class ShopDto {

    private UUID id;
    private String name;
    private String description;
    private UUID sellerId;
    private Point location;

    public ShopDto() {
    }

    public static ShopDto fromEntity(Shop shop) {
        ShopDto dto = new ShopDto();
        dto.setId(shop.getId());
        dto.setName(shop.getName());
        dto.setDescription(shop.getDescription());
        dto.setSellerId(shop.getSellerId());
        dto.setLocation(shop.getLocation());
        return dto;
    }

    public Shop toEntity() {
        Shop shop = new Shop();
        shop.setId(this.id);
        shop.setName(this.name);
        shop.setDescription(this.description);
        shop.setSellerId(this.sellerId);
        shop.setLocation(this.location);
        return shop;
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

    public UUID getSellerId() {
        return sellerId;
    }

    public void setSellerId(UUID sellerId) {
        this.sellerId = sellerId;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
}
