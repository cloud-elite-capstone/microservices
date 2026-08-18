package com.cartesian.shopservice.dto;

import java.util.UUID;
import org.locationtech.jts.geom.Point;

public class ShopRequest {

    private String name;
    private String description;
    private UUID sellerId;
    private Point location;

    public ShopRequest() {
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
