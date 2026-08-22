package com.cartesian.agentservice.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cartesian.agentservice.adapter.geo.GeoAdapter;
import com.cartesian.agentservice.adapter.search.WebSearchAdapter;
import com.cartesian.agentservice.client.ProductServiceClient;
import com.cartesian.agentservice.dto.ProductDto;
import com.cartesian.agentservice.dto.RecommendationRequest;
import com.cartesian.agentservice.dto.RecommendationResponse;
import com.cartesian.agentservice.dto.SearchItemCriteria;
import com.cartesian.agentservice.dto.search.ProductSearchRequest;
import com.cartesian.agentservice.dto.search.ProductSearchResponse;
import com.cartesian.agentservice.dto.search.Recommendation;
import com.cartesian.agentservice.dto.search.SearchCriteria;
import com.cartesian.agentservice.dto.search.SearchKeywordResult;
import com.cartesian.agentservice.dto.search.SearchRecommendationsRequest;
import com.cartesian.agentservice.dto.search.SearchResultsResponse;
import com.cartesian.agentservice.mapper.ProductOrchestratorMapper;
import com.cartesian.agentservice.mapper.RecommendationMapper;
import com.cartesian.agentservice.mapper.SearchCriteriaMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProductSearchService {
    private static final Logger log = LoggerFactory.getLogger(ProductSearchService.class);

    private final ProductServiceClient productServiceClient;
    private final WebSearchAdapter webSearchAdapter;
    private final GeoAdapter geoAdapter;
    private final AgentService agentService;
    private final SearchCriteriaMapper searchCriteriaMapper;
    private final RecommendationMapper recommendationMapper;
    private final ProductOrchestratorMapper productOrchestratorMapper;
    private final ObjectMapper objectMapper;

    public ProductSearchService(
            ProductServiceClient productServiceClient,
            WebSearchAdapter webSearchAdapter,
            GeoAdapter geoAdapter,
            AgentService agentService,
            SearchCriteriaMapper searchCriteriaMapper,
            RecommendationMapper recommendationMapper,
            ProductOrchestratorMapper productOrchestratorMapper,
            ObjectMapper objectMapper
    ) {
        this.productServiceClient = productServiceClient;
        this.webSearchAdapter = webSearchAdapter;
        this.geoAdapter = geoAdapter;
        this.agentService = agentService;
        this.searchCriteriaMapper = searchCriteriaMapper;
        this.recommendationMapper = recommendationMapper;
        this.productOrchestratorMapper = productOrchestratorMapper;
        this.objectMapper = objectMapper;
    }

    public SearchResultsResponse searchProducts(ProductSearchRequest request) {
        SearchCriteria criteria = searchCriteriaMapper.toSearchCriteria(request);
        ProductSearchResponse response = search(criteria);

        String itemName = (response.itemSearchKeyword() == null || response.itemSearchKeyword().isBlank())
                ? (request.search() != null ? request.search() : "")
                : response.itemSearchKeyword();

        SearchKeywordResult result = new SearchKeywordResult(
                itemName,
                response.recommendationCount(),
                response.recommendations()
        );

        return new SearchResultsResponse(List.of(result));
    }

    public SearchResultsResponse searchRecommendations(SearchRecommendationsRequest request) {
        List<SearchKeywordResult> searchResults = new ArrayList<>();
        if (request.searchKeywords() != null) {
            for (String keyword : request.searchKeywords()) {
                SearchCriteria criteria = searchCriteriaMapper.toSearchCriteria(keyword, request, objectMapper);
                ProductSearchResponse keywordResponse = search(criteria);
                String itemName = (keywordResponse.itemSearchKeyword() == null || keywordResponse.itemSearchKeyword().isBlank())
                        ? keyword
                        : keywordResponse.itemSearchKeyword();
                searchResults.add(new SearchKeywordResult(
                        itemName,
                        keywordResponse.recommendationCount(),
                        keywordResponse.recommendations()
                ));
            }
        }
        return new SearchResultsResponse(searchResults);
    }

    public Optional<ProductDto> getProductById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return productServiceClient.getProductById(id);
    }

    public ProductSearchResponse search(SearchCriteria criteria) {
        String search = criteria.search();

        // 1. Parallel multi-source fetch (local catalog + web search)
        List<ProductDto> initialProducts = fetchInitialProducts(search);

        // 2. Filter products based on budget and store constraints
        List<ProductDto> filteredProducts = filterProducts(initialProducts, criteria);

        // 3. Deduplicate
        List<ProductDto> deduplicatedProducts = deduplicateProducts(filteredProducts);

        // 4. Geocode spatial criteria if text location present
        Polygon polygon = resolveSearchPolygon(criteria);

        // 5. Invoke local Agent Service for AI recommendation ranking
        SearchItemCriteria searchItemCriteria = buildSearchItemCriteria(criteria, polygon, deduplicatedProducts);
        RecommendationRequest agentRequest = new RecommendationRequest(List.of(searchItemCriteria));

        RecommendationResponse agentResponse = agentService.generateRecommendations(agentRequest);

        return normalizeAgentResponse(agentResponse, search, deduplicatedProducts);
    }

    private List<ProductDto> fetchInitialProducts(String search) {
        if (!StringUtils.hasText(search)) {
            return new ArrayList<>();
        }

        CompletableFuture<List<ProductDto>> localFuture = CompletableFuture.supplyAsync(
                () -> productServiceClient.searchLocalProducts(search)
        );
        CompletableFuture<List<ProductDto>> webFuture = CompletableFuture.supplyAsync(
                () -> webSearchAdapter.search(search)
        );

        try {
            CompletableFuture.allOf(localFuture, webFuture).join();
            List<ProductDto> combined = new ArrayList<>();
            combined.addAll(localFuture.get());
            combined.addAll(webFuture.get());
            return combined;
        } catch (Exception e) {
            log.error("Error fetching multi-source products: {}", e.getMessage());
            List<ProductDto> fallback = new ArrayList<>();
            try {
                fallback.addAll(localFuture.getNow(List.of()));
            } catch (Exception ignored) {}
            try {
                fallback.addAll(webFuture.getNow(List.of()));
            } catch (Exception ignored) {}
            return fallback;
        }
    }

    private Polygon resolveSearchPolygon(SearchCriteria criteria) {
        if (StringUtils.hasText(criteria.locationText())) {
            return geoAdapter.geocodeToBoundingBoxPolygon(criteria.locationText());
        }
        return criteria.locationPolygon();
    }

    private SearchItemCriteria buildSearchItemCriteria(SearchCriteria criteria, Polygon polygon, List<ProductDto> candidates) {
        String imageUrl = StringUtils.hasText(criteria.imageUrl()) ? criteria.imageUrl() : null;

        SearchItemCriteria searchItemCriteria = new SearchItemCriteria();
        searchItemCriteria.setSearch(criteria.search() == null ? "" : criteria.search());
        searchItemCriteria.setLocation(polygon);
        searchItemCriteria.setBudget(criteria.maxBudget());
        searchItemCriteria.setImageUrl(imageUrl);
        searchItemCriteria.setCandidates(candidates);
        return searchItemCriteria;
    }

    private ProductSearchResponse normalizeAgentResponse(
            RecommendationResponse agentResponse,
            String search,
            List<ProductDto> initialProducts
    ) {
        List<Recommendation> recommendations = new ArrayList<>();
        if (agentResponse != null && agentResponse.getRecommendations() != null && !agentResponse.getRecommendations().isEmpty()) {
            recommendations.addAll(recommendationMapper.toRecommendationList(agentResponse.getRecommendations(), objectMapper));
        }

        if (recommendations.isEmpty() && !initialProducts.isEmpty()) {
            List<ProductDto> topCandidates = initialProducts.stream().limit(5).toList();
            recommendations.addAll(productOrchestratorMapper.toRecommendationList(topCandidates, objectMapper));
        }

        String keyword = search == null || search.isBlank() ? "" : search;
        return new ProductSearchResponse(keyword, recommendations.size(), recommendations);
    }

    private List<ProductDto> deduplicateProducts(List<ProductDto> products) {
        if (products == null) {
            return List.of();
        }
        return products.stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getName() != null && !p.getName().isBlank())
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                p -> p.getName().toLowerCase().trim(),
                                p -> p,
                                (existing, replacement) -> existing
                        ),
                        map -> new ArrayList<>(map.values())
                ));
    }

    private List<ProductDto> filterProducts(List<ProductDto> products, SearchCriteria criteria) {
        if (products == null) {
            return List.of();
        }
        return products.stream()
                .filter(Objects::nonNull)
                .filter(p -> criteria.minBudget() == null
                        || priceOrZero(p).compareTo(criteria.minBudget()) >= 0)
                .filter(p -> criteria.maxBudget() == null
                        || priceOrZero(p).compareTo(criteria.maxBudget()) <= 0)
                .filter(p -> criteria.sourceShop() == null
                        || criteria.sourceShop().equals(p.getShopId()))
                .toList();
    }

    private BigDecimal priceOrZero(ProductDto product) {
        return product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
    }
}
