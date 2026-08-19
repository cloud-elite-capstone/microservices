package com.cartesian.agentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class RecommendationRequest {

    @JsonProperty("search_for")
    private List<SearchItemCriteria> searchFor = new ArrayList<>();

    public RecommendationRequest() {
    }

    public RecommendationRequest(List<SearchItemCriteria> searchFor) {
        this.searchFor = searchFor;
    }

    public List<SearchItemCriteria> getSearchFor() {
        return searchFor;
    }

    public void setSearchFor(List<SearchItemCriteria> searchFor) {
        this.searchFor = searchFor;
    }
}
