package com.cartesian.agentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SearchItemCriteria {

    private String search;
    private Object location;
    private BigDecimal budget;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("candidates")
    private List<ProductDto> candidates = new ArrayList<>();

    public SearchItemCriteria() {
    }

    public SearchItemCriteria(String search, Object location, BigDecimal budget, String imageUrl) {
        this.search = search;
        this.location = location;
        this.budget = budget;
        this.imageUrl = imageUrl;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Object getLocation() {
        return location;
    }

    public void setLocation(Object location) {
        this.location = location;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<ProductDto> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<ProductDto> candidates) {
        this.candidates = candidates;
    }
}
