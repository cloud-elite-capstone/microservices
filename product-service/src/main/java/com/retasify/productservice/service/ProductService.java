package com.retasify.productservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retasify.productservice.client.AgentClient;
import com.retasify.productservice.client.GeocoderClient;
import com.retasify.productservice.client.SerpApiClient;
import com.retasify.productservice.dto.ProductDto;
import com.retasify.productservice.dto.ProductSearchRequest;
import com.retasify.productservice.exception.CategoryNotFoundException;
import com.retasify.productservice.exception.ProductNotFoundException;
import com.retasify.productservice.mapper.ProductMapper;
import com.retasify.productservice.model.Category;
import com.retasify.productservice.model.Product;
import com.retasify.productservice.repository.CategoryRepository;
import com.retasify.productservice.repository.ProductRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.annotation.Nonnull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ProductService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SerpApiClient serpApiClient;
    private final GeocoderClient geocoderClient;
    private final AgentClient agentClient;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          SerpApiClient serpApiClient,
                          GeocoderClient geocoderClient,
                          AgentClient agentClient,
                          ProductMapper productMapper
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.serpApiClient = serpApiClient;
        this.geocoderClient = geocoderClient;
        this.agentClient = agentClient;
        this.productMapper = productMapper;
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
    public ProductDto getProductById(UUID id) {
        Product product = findProductByIdOrThrow(id);
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        findCategoryByIdOrThrow(productDto.categoryId());

        Product product = productMapper.toEntity(productDto);

        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDto updateProduct(UUID id, ProductDto productDto) {
        Product product = findProductByIdOrThrow(id);
        findCategoryByIdOrThrow(productDto.categoryId());

        productMapper.updateEntityFromDto(productDto, product);

        Product updated = productRepository.save(product);
        return productMapper.toDto(updated);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        findProductByIdOrThrow(id);
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> searchProducts(@Nonnull ProductSearchRequest request) {
        return searchProduct(request.search(), request.location(), request.budget(), request.image());
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
    public Map<String, Object> searchProducts(@Nonnull Map<String, Object> requestBody) {
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

    private String resolveImageSource(ProductSearchRequest request) {
        String imageUrl = request.imageUrl();
        if (imageUrl != null && !imageUrl.isBlank()) {
            return imageUrl;
        }

        MultipartFile image = request.image();
        if (image != null && !image.isEmpty()) {
            try {
                String contentType = image.getContentType() != null ? image.getContentType() : MediaType.IMAGE_JPEG_VALUE;
                String base64Data = Base64.getEncoder().encodeToString(image.getBytes());
                return "data:" + contentType + ";base64," + base64Data;
            } catch (IOException e) {
                throw new ImageProcessingException("Failed to process uploaded image", e);
            }
        }

        return null;
    }
}
