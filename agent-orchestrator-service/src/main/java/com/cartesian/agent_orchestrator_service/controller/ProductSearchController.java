package com.cartesian.agent_orchestrator_service.controller;

import com.cartesian.agent_orchestrator_service.dto.search.ProductSearchRequest;
import com.cartesian.agent_orchestrator_service.dto.search.SearchRecommendationsRequest;
import com.cartesian.agent_orchestrator_service.dto.search.SearchResultsResponse;
import com.cartesian.agent_orchestrator_service.service.ProductSearchService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
