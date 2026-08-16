package com.retasify.productservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retasify.productservice.dto.ProductDto;
import com.retasify.productservice.dto.ProductSearchRequest;
import com.retasify.productservice.exception.CategoryNotFoundException;
import com.retasify.productservice.exception.ProductNotFoundException;
import com.retasify.productservice.model.Category;
import com.retasify.productservice.model.Product;
import com.retasify.productservice.repository.CategoryRepository;
import com.retasify.productservice.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ProductService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${serpapi.api.key:}")
    private String serpApiKey;

    @Value("${geocoding.api.key:}")
    private String geocodingApiKey;

    @Value("${agent.service.url:http://localhost:8085}")
    private String agentServiceUrl;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          WebClient.Builder webClientBuilder,
                          ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductDto.fromEntity(product);
    }

    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        validateRequest(productDto);
        Category category = categoryRepository.findById(productDto.getCategoryId())
            .orElseThrow(() -> new CategoryNotFoundException(productDto.getCategoryId()));
        Product product = productDto.toEntity();
        product.setCategoryId(category.getId());
        Product saved = productRepository.save(product);
        return ProductDto.fromEntity(saved);
    }

    @Transactional
    public ProductDto updateProduct(UUID id, ProductDto productDto) {
        validateRequest(productDto);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
        categoryRepository.findById(productDto.getCategoryId())
            .orElseThrow(() -> new CategoryNotFoundException(productDto.getCategoryId()));
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setQuantity(productDto.getQuantity());
        product.setLocation(productDto.getLocation());
        product.setCategoryId(productDto.getCategoryId());
        product.setImageUrl(productDto.getImageUrl());
        product.setShopId(productDto.getShopId());
        Product updated = productRepository.save(product);
        return ProductDto.fromEntity(updated);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> searchProducts(ProductSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request is required");
        }
        return searchProduct(request.getSearch(), request.getLocation(), request.getBudget(), request.getImage());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> searchProduct(String search, String location, BigDecimal budget, String image) {
        return searchProduct(search, (Object) location, budget, image);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> searchProduct(String search, Object location, BigDecimal budget, String image) {
        List<Product> initialProducts = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            initialProducts.addAll(searchDatabaseByKeyword(search));
            initialProducts.addAll(searchSerpApiProducts(search));
        }

        if ((location != null && !(location instanceof String s && s.isBlank())) || budget != null || (image != null && !image.isBlank())) {
            Polygon polygon = null;
            if (location instanceof String s) {
                if (!s.isBlank()) {
                    polygon = geocodeLocationToBoundingBoxPolygon(s);
                }
            } else if (location instanceof Map<?, ?> map) {
                polygon = toPolygon(map);
            }
            Map<String, Object> agentPayload = buildAgentPayload(search, polygon, budget, image);
            Map<String, Object> agentResponse = callAgentRecommendations(agentPayload);
            return normalizeAgentResponse(agentResponse, search, initialProducts);
        }

        return buildRecommendationResponse(search, deduplicateProducts(initialProducts));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> searchProducts(Map<String, Object> requestBody) {
        if (requestBody == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        Object keywordsObj = requestBody.get("search_keywords");
        List<String> keywords = new ArrayList<>();
        if (keywordsObj instanceof List<?> list) {
            for (Object item : list) {
                if (item != null && !item.toString().isBlank()) {
                    keywords.add(item.toString());
                }
            }
        } else if (keywordsObj instanceof String keywordString && !keywordString.isBlank()) {
            keywords.add(keywordString);
        }

        if (keywords.isEmpty()) {
            throw new IllegalArgumentException("At least one search keyword is required");
        }

        Object locationObj = requestBody.get("location");
        Object budgetObj = requestBody.get("budget");
        Object imageObj = requestBody.get("image_url");

        BigDecimal budget = budgetObj == null ? null : parseBigDecimal(budgetObj.toString());
        String imageUrl = imageObj == null ? null : imageObj.toString();

        List<Map<String, Object>> searchResults = new ArrayList<>();
        for (String keyword : keywords) {
            Map<String, Object> keywordResponse = searchProduct(keyword, locationObj, budget, imageUrl);
            if (keywordResponse == null || keywordResponse.isEmpty()) {
                continue;
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("item_name", keywordResponse.getOrDefault("item_search_keyword", keyword));
            result.put("no_of_agent_recommendations", keywordResponse.getOrDefault("recommendation_count", 0));
            result.put("Items", keywordResponse.getOrDefault("recommendations", List.of()));
            searchResults.add(result);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("search_results", searchResults);
        return response;
    }

    private List<Product> searchDatabaseByKeyword(String keyword) {
        return productRepository.findAll().stream()
            .filter(product -> productMatchesSearch(product, keyword))
            .collect(Collectors.toList());
    }

    private List<Product> searchSerpApiProducts(String keyword) {
        if (serpApiKey == null || serpApiKey.isBlank()) {
            return List.of();
        }

        try {
            JsonNode root = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .scheme("https")
                    .host("serpapi.com")
                    .path("/search.json")
                    .queryParam("engine", "google_product")
                    .queryParam("q", keyword)
                    .queryParam("api_key", serpApiKey)
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

            if (root == null) {
                return List.of();
            }

            JsonNode results = root.path("shopping_results");
            if (results.isMissingNode() || !results.isArray()) {
                results = root.path("organic_results");
            }
            if (results.isMissingNode() || !results.isArray()) {
                return List.of();
            }

            List<Product> converted = new ArrayList<>();
            for (JsonNode item : results) {
                Product product = new Product();
                product.setId(UUID.randomUUID());
                product.setName(item.path("title").asText(item.path("name").asText(null)));
                product.setDescription(item.path("snippet").asText(null));
                product.setPrice(parseBigDecimal(item.path("price").asText(null)));
                product.setQuantity(0);
                product.setLocation(null);
                product.setCategoryId(null);
                product.setImageUrl(item.path("thumbnail").asText(item.path("image").asText(null)));
                product.setShopId(null);
                converted.add(product);
            }
            return converted;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private Map<String, Object> buildAgentPayload(String search, Polygon polygon, BigDecimal budget, String image) {
        Map<String, Object> payload = new LinkedHashMap<>();
        List<Map<String, Object>> searchFor = new ArrayList<>();
        Map<String, Object> searchItem = new LinkedHashMap<>();
        searchItem.put("search", search == null ? "" : search);
        if (polygon != null) {
            searchItem.put("location", toGeoJsonPolygon(polygon));
        }
        if (budget != null) {
            searchItem.put("budget", budget);
        }
        if (image != null && !image.isBlank()) {
            searchItem.put("image_url", image);
        }
        searchFor.add(searchItem);
        payload.put("search_for", searchFor);
        return payload;
    }

    private Map<String, Object> callAgentRecommendations(Map<String, Object> payload) {
        try {
            JsonNode root = webClient.post()
                .uri(agentServiceUrl + "/agent/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

            if (root == null) {
                return Map.of();
            }
            return objectMapper.convertValue(root, Map.class);
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private Map<String, Object> normalizeAgentResponse(Map<String, Object> agentResponse, String search, List<Product> initialProducts) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        Object rawRecommendations = agentResponse.get("recommendations");
        if (rawRecommendations instanceof List<?>) {
            for (Object item : (List<?>) rawRecommendations) {
                if (item instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) item;
                    Map<String, Object> recommendation = new LinkedHashMap<>();
                    recommendation.put("name", map.getOrDefault("name", ""));
                    recommendation.put("rationale", map.getOrDefault("rationale", ""));
                    recommendation.put("totalLandedCost", map.getOrDefault("totalLandedCost", BigDecimal.ZERO));
                    recommendation.put("product", map.get("product"));
                    recommendations.add(recommendation);
                }
            }
        }
        if (recommendations.isEmpty() && !initialProducts.isEmpty()) {
            for (Product product : initialProducts.stream().limit(5).collect(Collectors.toList())) {
                Map<String, Object> recommendation = new LinkedHashMap<>();
                recommendation.put("name", product.getName());
                recommendation.put("rationale", "Recommended from initial search results");
                recommendation.put("totalLandedCost", product.getPrice() == null ? BigDecimal.ZERO : product.getPrice());
                recommendation.put("product", ProductDto.fromEntity(product));
                recommendations.add(recommendation);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("item_search_keyword", search == null || search.isBlank() ? "" : search);
        response.put("recommendation_count", recommendations.size());
        response.put("recommendations", recommendations);
        return response;
    }

    private Map<String, Object> buildRecommendationResponse(String search, List<Product> products) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (Product product : products.stream().limit(5).collect(Collectors.toList())) {
            Map<String, Object> recommendation = new LinkedHashMap<>();
            recommendation.put("name", product.getName());
            recommendation.put("rationale", "Result from local or external product search");
            recommendation.put("totalLandedCost", product.getPrice() == null ? BigDecimal.ZERO : product.getPrice());
            recommendation.put("product", ProductDto.fromEntity(product));
            recommendations.add(recommendation);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("item_search_keyword", search == null || search.isBlank() ? "" : search);
        response.put("recommendation_count", recommendations.size());
        response.put("recommendations", recommendations);
        return response;
    }

    private List<Product> deduplicateProducts(List<Product> products) {
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

    private Polygon geocodeLocationToBoundingBoxPolygon(String location) {
        try {
            JsonNode root = webClient.get()
                .uri(uriBuilder -> {
                    String encoded = java.net.URLEncoder.encode(location, java.nio.charset.StandardCharsets.UTF_8);
                    return uriBuilder
                        .scheme("https")
                        .host("nominatim.openstreetmap.org")
                        .path("/search")
                        .queryParam("q", location)
                        .queryParam("format", "jsonv2")
                        .queryParam("limit", "1")
                        .build();
                })
                .header("User-Agent", "retasify-product-service")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

            if (root == null || !root.isArray() || root.size() == 0) {
                return null;
            }
            JsonNode first = root.get(0);
            JsonNode bboxNode = first.path("boundingbox");
            if (bboxNode.isMissingNode() || !bboxNode.isArray() || bboxNode.size() < 4) {
                return null;
            }

            double south = bboxNode.get(0).asDouble();
            double north = bboxNode.get(1).asDouble();
            double west = bboxNode.get(2).asDouble();
            double east = bboxNode.get(3).asDouble();
            Coordinate[] coordinates = new Coordinate[] {
                new Coordinate(west, south),
                new Coordinate(east, south),
                new Coordinate(east, north),
                new Coordinate(west, north),
                new Coordinate(west, south)
            };
            LinearRing ring = GEOMETRY_FACTORY.createLinearRing(coordinates);
            return GEOMETRY_FACTORY.createPolygon(ring);
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, Object> toGeoJsonPolygon(Polygon polygon) {
        if (polygon == null) {
            return Map.of();
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "Polygon");
        List<List<List<Double>>> rings = new ArrayList<>();
        List<List<Double>> ring = new ArrayList<>();
        for (Coordinate coordinate : polygon.getExteriorRing().getCoordinates()) {
            ring.add(List.of(coordinate.x, coordinate.y));
        }
        rings.add(ring);
        body.put("coordinates", rings);
        return body;
    }

    private Polygon toPolygon(Map<?, ?> locationMap) {
        if (locationMap == null || !"Polygon".equalsIgnoreCase(String.valueOf(locationMap.get("type")))) {
            return null;
        }
        Object coordinates = locationMap.get("coordinates");
        if (!(coordinates instanceof List<?> rings) || rings.isEmpty()) {
            return null;
        }
        Object outerRing = rings.get(0);
        if (!(outerRing instanceof List<?> ringCoordinates) || ringCoordinates.size() < 4) {
            return null;
        }

        Coordinate[] coordinatesArray = new Coordinate[ringCoordinates.size()];
        for (int i = 0; i < ringCoordinates.size(); i++) {
            Object coordinate = ringCoordinates.get(i);
            if (!(coordinate instanceof List<?> point) || point.size() < 2) {
                return null;
            }
            double x = Double.parseDouble(point.get(0).toString());
            double y = Double.parseDouble(point.get(1).toString());
            coordinatesArray[i] = new Coordinate(x, y);
        }
        LinearRing linearRing = GEOMETRY_FACTORY.createLinearRing(coordinatesArray);
        return GEOMETRY_FACTORY.createPolygon(linearRing);
    }

    private boolean productMatchesSearch(Product product, String term) {
        String value = term.toLowerCase();
        return (product.getName() != null && product.getName().toLowerCase().contains(value))
            || (product.getDescription() != null && product.getDescription().toLowerCase().contains(value));
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            String normalized = value.replace("$", "").replace(",", "").trim();
            return new BigDecimal(normalized);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private void validateRequest(ProductDto productDto) {
        if (productDto == null) {
            throw new IllegalArgumentException("Product payload is required");
        }
        if (productDto.getName() == null || productDto.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (productDto.getPrice() == null) {
            throw new IllegalArgumentException("Product price is required");
        }
        if (productDto.getCategoryId() == null) {
            throw new IllegalArgumentException("Category id is required");
        }
        if (productDto.getShopId() == null) {
            throw new IllegalArgumentException("Shop id is required");
        }
    }
}
