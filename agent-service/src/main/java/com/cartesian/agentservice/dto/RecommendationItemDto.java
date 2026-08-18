package com.cartesian.agentservice.dto;

public class RecommendationItemDto {

    private String name;
    private ProductDto product;

    public RecommendationItemDto() {
    }

    public RecommendationItemDto(String name, ProductDto product) {
        this.name = name;
        this.product = product;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductDto getProduct() {
        return product;
    }

    public void setProduct(ProductDto product) {
        this.product = product;
    }
}
