package com.cartesian.productservice.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cartesian.productservice.client.AgentClient;
import com.cartesian.productservice.client.GeocoderClient;
import com.cartesian.productservice.client.SerpApiClient;
import com.cartesian.productservice.dto.AgentSearchItem;
import com.cartesian.productservice.dto.AgentSearchRequest;
import com.cartesian.productservice.dto.AgentSearchResponse;
import com.cartesian.productservice.dto.ProductRequest;
import com.cartesian.productservice.dto.ProductResponse;
import com.cartesian.productservice.dto.ProductSearchRequest;
import com.cartesian.productservice.dto.ProductSearchResponse;
import com.cartesian.productservice.dto.Recommendation;
import com.cartesian.productservice.dto.SearchCriteria;
import com.cartesian.productservice.dto.SearchKeywordResult;
import com.cartesian.productservice.dto.SearchRecommendationsRequest;
import com.cartesian.productservice.dto.SearchResultsResponse;
import com.cartesian.productservice.exception.CategoryNotFoundException;
import com.cartesian.productservice.exception.ProductNotFoundException;
import com.cartesian.productservice.mapper.ProductMapper;
import com.cartesian.productservice.mapper.RecommendationMapper;
import com.cartesian.productservice.mapper.SearchCriteriaMapper;
import com.cartesian.productservice.model.Category;
import com.cartesian.productservice.model.Product;
import com.cartesian.productservice.repository.CategoryRepository;
import com.cartesian.productservice.repository.ProductRepository;

import jakarta.annotation.Nonnull;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SerpApiClient serpApiClient;
    private final GeocoderClient geocoderClient;
    private final AgentClient agentClient;
    private final ProductMapper productMapper;
    private final RecommendationMapper recommendationMapper;
    private final SearchCriteriaMapper searchCriteriaMapper;
    private final ObjectMapper objectMapper;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          SerpApiClient serpApiClient,
                          GeocoderClient geocoderClient,
                          AgentClient agentClient,
                          ProductMapper productMapper,
                          RecommendationMapper recommendationMapper,
                          SearchCriteriaMapper searchCriteriaMapper,
                          ObjectMapper objectMapper
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.serpApiClient = serpApiClient;
        this.geocoderClient = geocoderClient;
        this.agentClient = agentClient;
        this.productMapper = productMapper;
        this.recommendationMapper = recommendationMapper;
        this.searchCriteriaMapper = searchCriteriaMapper;
        this.objectMapper = objectMapper;
    }

    private Product findProductByIdOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private Category findCategoryByIdOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = findProductByIdOrThrow(id);
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        findCategoryByIdOrThrow(request.categoryId());

        Product product = productMapper.toEntity(request);

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = findProductByIdOrThrow(id);
        findCategoryByIdOrThrow(request.categoryId());

        productMapper.updateEntityFromRequest(request, product);

        Product updated = productRepository.save(product);
        return productMapper.toResponse(updated);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        findProductByIdOrThrow(id);
        productRepository.deleteById(id);
    }

    public ProductSearchResponse searchProducts(@Nonnull ProductSearchRequest request) {
        SearchCriteria criteria = searchCriteriaMapper.toSearchCriteria(request);
        return search(criteria);
    }

    public SearchResultsResponse searchProducts(@Nonnull SearchRecommendationsRequest request) {
        List<SearchKeywordResult> searchResults = new ArrayList<>();
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

        return new SearchResultsResponse(searchResults);
    }

    private ProductSearchResponse search(SearchCriteria criteria) {
        String search = criteria.search();

        List<Product> initialProducts = new ArrayList<>();
        if (StringUtils.hasText(search)) {
            // TODO: can make this filter already in the database
            initialProducts.addAll(productRepository.searchByNameOrDescription(search));

            // TODO: can apply caching
            initialProducts.addAll(serpApiClient.search(search));
        }

        List<Product> filteredProducts = filterProducts(initialProducts, criteria);

        if (requiresAgent(criteria)) {
            Polygon polygon = resolveSearchPolygon(criteria);
            AgentSearchRequest agentRequest = buildAgentRequest(criteria, polygon);
            AgentSearchResponse agentResponse = agentClient.recommend(agentRequest);
            return normalizeAgentResponse(agentResponse, search, filteredProducts);
        }

        return buildRecommendationResponse(search, deduplicateProducts(filteredProducts));
    }

    private static boolean requiresAgent(SearchCriteria criteria) {
        return criteria.locationText() != null
                || criteria.locationPolygon() != null
                || criteria.minBudget() != null
                || criteria.maxBudget() != null
                || criteria.minRating() != null
                || criteria.maxRating() != null
                || criteria.sourceShop() != null
                || (criteria.imageUrl() != null && !criteria.imageUrl().isBlank());
    }

    private Polygon resolveSearchPolygon(SearchCriteria criteria) {
        if (criteria.locationText() != null) {
            return geocoderClient.geocodeToBoundingBoxPolygon(criteria.locationText());
        }
        return criteria.locationPolygon();
    }

    private static AgentSearchRequest buildAgentRequest(SearchCriteria criteria, Polygon polygon) {
        String imageUrl = StringUtils.hasText(criteria.imageUrl()) ? criteria.imageUrl() : null;
        AgentSearchItem searchItem = new AgentSearchItem(
                criteria.search() == null ? "" : criteria.search(),
                polygon,
                criteria.maxBudget(),
                criteria.minBudget(),
                criteria.minRating(),
                criteria.maxRating(),
                criteria.sourceShop(),
                imageUrl
        );
        return new AgentSearchRequest(List.of(searchItem));
    }

    private ProductSearchResponse normalizeAgentResponse(AgentSearchResponse agentResponse, String search, List<Product> initialProducts) {
        List<Recommendation> recommendations = Optional.ofNullable(agentResponse)
                .map(AgentSearchResponse::recommendations)
                .map(recommendationMapper::toResponseList)
                .orElseGet(ArrayList::new);

        if (recommendations.isEmpty() && !initialProducts.isEmpty()) {
            recommendations.addAll(buildRecommendationsFromProducts(initialProducts, "Recommended from initial search results"));
        }

        String keyword = search == null || search.isBlank() ? "" : search;
        return new ProductSearchResponse(keyword, recommendations.size(), recommendations);
    }

    private ProductSearchResponse buildRecommendationResponse(String search, List<Product> products) {
        List<Recommendation> recommendations = buildRecommendationsFromProducts(products, "Result from local or external product search");
        String keyword = search == null || search.isBlank() ? "" : search;
        return new ProductSearchResponse(keyword, recommendations.size(), recommendations);
    }

    private List<Recommendation> buildRecommendationsFromProducts(List<Product> products, String rationale) {
        List<Recommendation> recommendations = new ArrayList<>();
        for (Product product : products.stream().limit(5).toList()) {
            recommendations.add(new Recommendation(
                    product.getName(),
                    rationale,
                    product.getPrice() == null ? BigDecimal.ZERO : product.getPrice(),
                    objectMapper.valueToTree(productMapper.toResponse(product))
            ));
        }
        return recommendations;
    }

    private static List<Product> deduplicateProducts(List<Product> products) {
        return products.stream()
            .filter(Objects::nonNull)
            .filter(product -> product.getName() != null && !product.getName().isBlank())
            .collect(Collectors.collectingAndThen(
                Collectors.toMap(
                    product -> product.getName().toLowerCase(),
                    product -> product,
                    (left, right) -> left),
                map -> new ArrayList<>(map.values())));
    }

    private static List<Product> filterProducts(List<Product> products, SearchCriteria criteria) {
        return products.stream()
                .filter(Objects::nonNull)
                .filter(product -> criteria.minBudget() == null
                        || priceOrZero(product).compareTo(criteria.minBudget()) >= 0)
                .filter(product -> criteria.maxBudget() == null
                        || priceOrZero(product).compareTo(criteria.maxBudget()) <= 0)
                .filter(product -> criteria.sourceShop() == null
                        || criteria.sourceShop().equals(product.getShopId()))
                .toList();
    }

    private static BigDecimal priceOrZero(Product product) {
        return product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
    }
}
