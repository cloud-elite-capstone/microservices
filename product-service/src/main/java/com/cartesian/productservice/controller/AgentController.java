package com.cartesian.productservice.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cartesian.productservice.dto.SearchRecommendationsRequest;
import com.cartesian.productservice.dto.SearchResultsResponse;
import com.cartesian.productservice.service.ProductService;

import jakarta.validation.Valid;

@RestController
public class AgentController {

    private final ProductService productService;

    public AgentController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(value = "/agent/recommendations", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SearchResultsResponse> getAgentRecommendations(@Valid @RequestBody SearchRecommendationsRequest request) {
        return ResponseEntity.ok(productService.searchProducts(request));
    }
}
