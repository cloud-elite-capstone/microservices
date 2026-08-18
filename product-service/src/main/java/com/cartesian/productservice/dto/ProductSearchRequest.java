package com.cartesian.productservice.dto;

import java.math.BigDecimal;
import java.util.UUID;
import org.locationtech.jts.geom.Point;

public class ProductSearchRequest {

    private String search;
    private String location;
    private BigDecimal budget;
    private String image;

    public ProductSearchRequest() {
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
