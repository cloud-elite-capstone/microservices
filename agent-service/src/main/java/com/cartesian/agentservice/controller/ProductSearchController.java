package com.cartesian.agentservice.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cartesian.agentservice.dto.ProductDto;
import com.cartesian.agentservice.dto.search.ProductSearchRequest;
import com.cartesian.agentservice.dto.search.SearchRecommendationsRequest;
import com.cartesian.agentservice.dto.search.SearchResultsResponse;
import com.cartesian.agentservice.service.ProductSearchService;

import jakarta.validation.Valid;

@RestController
public class ProductSearchController {
    private final ProductSearchService searchService;

    public ProductSearchController(ProductSearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping(value = "/products/search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SearchResultsResponse> searchProductsMultipart(@Valid @ModelAttribute ProductSearchRequest request) {
        return ResponseEntity.ok(searchService.searchProducts(request));
    }

    @PostMapping("/orchestrator/recommendations")
    public ResponseEntity<SearchResultsResponse> getRecommendations(@Valid @RequestBody SearchRecommendationsRequest request) {
        return ResponseEntity.ok(searchService.searchRecommendations(request));
    }

    @PostMapping("/orchestrator/search")
    public ResponseEntity<SearchResultsResponse> orchestratorSearch(@Valid @RequestBody SearchRecommendationsRequest request) {
        return ResponseEntity.ok(searchService.searchRecommendations(request));
    }

    @GetMapping("/orchestrator/products/{productId}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable UUID productId) {
        return searchService.getProductById(productId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
