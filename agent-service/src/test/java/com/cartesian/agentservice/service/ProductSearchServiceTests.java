package com.cartesian.agentservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cartesian.agentservice.adapter.geo.GeoAdapter;
import com.cartesian.agentservice.adapter.search.WebSearchAdapter;
import com.cartesian.agentservice.client.ProductServiceClient;
import com.cartesian.agentservice.dto.ProductDto;
import com.cartesian.agentservice.dto.RecommendationItemDto;
import com.cartesian.agentservice.dto.RecommendationResponse;
import com.cartesian.agentservice.dto.search.ProductSearchRequest;
import com.cartesian.agentservice.dto.search.SearchResultsResponse;
import com.cartesian.agentservice.mapper.ProductOrchestratorMapper;
import com.cartesian.agentservice.mapper.ProductOrchestratorMapperImpl;
import com.cartesian.agentservice.mapper.RecommendationMapper;
import com.cartesian.agentservice.mapper.RecommendationMapperImpl;
import com.cartesian.agentservice.mapper.SearchCriteriaMapper;
import com.cartesian.agentservice.mapper.SearchCriteriaMapperImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ProductSearchServiceTests {

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private WebSearchAdapter webSearchAdapter;

    @Mock
    private GeoAdapter geoAdapter;

    @Mock
    private AgentService agentService;

    private ProductSearchService searchService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @BeforeEach
    void setUp() {
        SearchCriteriaMapper searchCriteriaMapper = new SearchCriteriaMapperImpl();
        RecommendationMapper recommendationMapper = new RecommendationMapperImpl();
        ProductOrchestratorMapper productOrchestratorMapper = new ProductOrchestratorMapperImpl();

        searchService = new ProductSearchService(
                productServiceClient,
                webSearchAdapter,
                geoAdapter,
                agentService,
                searchCriteriaMapper,
                recommendationMapper,
                productOrchestratorMapper,
                objectMapper
        );
    }

    private ProductDto product(String name, String price) {
        ProductDto product = new ProductDto();
        product.setId(UUID.randomUUID());
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        return product;
    }

    @Test
    void searchProducts_combinesLocalAndWebAndInvokesAgent() {
        ProductDto localProduct = product("Wireless Earbuds", "49.99");
        ProductDto webProduct = product("Noise Canceling Earbuds", "79.99");

        when(productServiceClient.searchLocalProducts("earbuds")).thenReturn(List.of(localProduct));
        when(webSearchAdapter.search("earbuds")).thenReturn(List.of(webProduct));

        Polygon mockPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 0),
                new Coordinate(0, 0)
        });
        when(geoAdapter.geocodeToBoundingBoxPolygon("Seattle")).thenReturn(mockPolygon);

        RecommendationItemDto recommendationItem = new RecommendationItemDto("Top Choice", localProduct);
        RecommendationResponse agentResponse = new RecommendationResponse(List.of(recommendationItem));
        when(agentService.generateRecommendations(any())).thenReturn(agentResponse);

        ProductSearchRequest request = new ProductSearchRequest(
                "earbuds",
                "Seattle",
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                null,
                null,
                null,
                null,
                null
        );

        SearchResultsResponse response = searchService.searchProducts(request);

        assertThat(response).isNotNull();
        assertThat(response.searchResults()).hasSize(1);
        assertThat(response.searchResults().get(0).itemName()).isEqualTo("earbuds");
        assertThat(response.searchResults().get(0).noOfAgentRecommendations()).isEqualTo(1);
        assertThat(response.searchResults().get(0).items().get(0).name()).isEqualTo("Top Choice");
    }

    @Test
    void searchProducts_fallsBackToTopLocalWhenAgentReturnsEmpty() {
        ProductDto localProduct = product("Running Shoes", "89.99");

        when(productServiceClient.searchLocalProducts("shoes")).thenReturn(List.of(localProduct));
        when(webSearchAdapter.search("shoes")).thenReturn(List.of());
        when(agentService.generateRecommendations(any())).thenReturn(new RecommendationResponse(List.of()));

        ProductSearchRequest request = new ProductSearchRequest(
                "shoes",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        SearchResultsResponse response = searchService.searchProducts(request);

        assertThat(response).isNotNull();
        assertThat(response.searchResults().get(0).noOfAgentRecommendations()).isEqualTo(1);
        assertThat(response.searchResults().get(0).items().get(0).name()).isEqualTo("Running Shoes");
    }
}
