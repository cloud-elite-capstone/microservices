package com.cartesian.productservice.controller;

import org.springframework.web.bind.annotation.RestController;

import com.cartesian.productservice.service.ProductService;

@RestController
public class AgentController {
    private final ProductService productService;

    public AgentController(ProductService productService) {
        this.productService = productService;
    }

//    @PostMapping(value = "/agent/recommendations", consumes = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<SearchResultsResponse> getAgentRecommendations(@Valid @RequestBody SearchRecommendationsRequest request) {
//        return ResponseEntity.ok(productService.searchProducts(request));
//    }
}
