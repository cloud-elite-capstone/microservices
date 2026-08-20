package com.cartesian.agent_orchestrator_service.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cartesian.agent_orchestrator_service.dto.product.ProductDto;
import com.cartesian.agent_orchestrator_service.dto.search.ProductSearchRequest;
import com.cartesian.agent_orchestrator_service.dto.search.SearchRecommendationsRequest;
import com.cartesian.agent_orchestrator_service.dto.search.SearchResultsResponse;
import com.cartesian.agent_orchestrator_service.service.ProductSearchService;

import jakarta.validation.Valid;

@RestController
public class ProductSearchController {
    private final ProductSearchService searchOrchestratorService;

    public ProductSearchController(ProductSearchService searchOrchestratorService) {
        this.searchOrchestratorService = searchOrchestratorService;
    }

    @PostMapping(value = "/products/search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SearchResultsResponse> searchProductsMultipart(@Valid @ModelAttribute ProductSearchRequest request) {
        return ResponseEntity.ok(searchOrchestratorService.searchProducts(request));
    }

    @PostMapping("/orchestrator/recommendations")
    public ResponseEntity<SearchResultsResponse> getRecommendations(@Valid @RequestBody SearchRecommendationsRequest request) {
        return ResponseEntity.ok(searchOrchestratorService.searchRecommendations(request));
    }

    @PostMapping("/orchestrator/search")
    public ResponseEntity<SearchResultsResponse> orchestratorSearch(@Valid @RequestBody SearchRecommendationsRequest request) {
        return ResponseEntity.ok(searchOrchestratorService.searchRecommendations(request));
    }

    @GetMapping("/orchestrator/products/{productId}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable UUID productId) {
        return searchOrchestratorService.getProductById(productId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
