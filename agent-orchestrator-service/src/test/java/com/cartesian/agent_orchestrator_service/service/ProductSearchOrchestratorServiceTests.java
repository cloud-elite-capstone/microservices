package com.cartesian.agent_orchestrator_service.service;

import com.cartesian.agent_orchestrator_service.adapter.geo.GeoAdapter;
import com.cartesian.agent_orchestrator_service.adapter.search.WebSearchAdapter;
import com.cartesian.agent_orchestrator_service.client.AgentServiceClient;
import com.cartesian.agent_orchestrator_service.client.ProductServiceClient;
import com.cartesian.agent_orchestrator_service.dto.agent.RecommendationItemDto;
import com.cartesian.agent_orchestrator_service.dto.agent.RecommendationResponse;
import com.cartesian.agent_orchestrator_service.dto.product.ProductDto;
import com.cartesian.agent_orchestrator_service.dto.search.ProductSearchRequest;
import com.cartesian.agent_orchestrator_service.dto.search.SearchResultsResponse;
import com.cartesian.agent_orchestrator_service.mapper.ProductOrchestratorMapper;
import com.cartesian.agent_orchestrator_service.mapper.ProductOrchestratorMapperImpl;
import com.cartesian.agent_orchestrator_service.mapper.RecommendationMapper;
import com.cartesian.agent_orchestrator_service.mapper.RecommendationMapperImpl;
import com.cartesian.agent_orchestrator_service.mapper.SearchCriteriaMapper;
import com.cartesian.agent_orchestrator_service.mapper.SearchCriteriaMapperImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSearchOrchestratorServiceTests {

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private WebSearchAdapter webSearchAdapter;

    @Mock
    private GeoAdapter geoAdapter;

    @Mock
    private AgentServiceClient agentServiceClient;

    private ProductSearchService searchOrchestratorService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @BeforeEach
    void setUp() {
        SearchCriteriaMapper searchCriteriaMapper = new SearchCriteriaMapperImpl();
        RecommendationMapper recommendationMapper = new RecommendationMapperImpl();
        ProductOrchestratorMapper productOrchestratorMapper = new ProductOrchestratorMapperImpl();

        searchOrchestratorService = new ProductSearchService(
                productServiceClient,
                webSearchAdapter,
                geoAdapter,
                agentServiceClient,
                searchCriteriaMapper,
                recommendationMapper,
                productOrchestratorMapper,
                objectMapper
        );
    }

    @Test
    void searchProducts_combinesLocalAndWebAndInvokesAgent() {
        ProductDto localProduct = ProductDto.builder()
                .id(UUID.randomUUID())
                .name("Wireless Earbuds")
                .price(new BigDecimal("49.99"))
                .build();

        ProductDto webProduct = ProductDto.builder()
                .id(UUID.randomUUID())
                .name("Noise Canceling Earbuds")
                .price(new BigDecimal("79.99"))
                .build();

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
        when(agentServiceClient.getRecommendations(any())).thenReturn(agentResponse);

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

        SearchResultsResponse response = searchOrchestratorService.searchProducts(request);

        assertThat(response).isNotNull();
        assertThat(response.searchResults()).hasSize(1);
        assertThat(response.searchResults().get(0).itemName()).isEqualTo("earbuds");
        assertThat(response.searchResults().get(0).noOfAgentRecommendations()).isEqualTo(1);
        assertThat(response.searchResults().get(0).items().get(0).name()).isEqualTo("Top Choice");
    }

    @Test
    void searchProducts_fallsBackToTopLocalWhenAgentReturnsEmpty() {
        ProductDto localProduct = ProductDto.builder()
                .id(UUID.randomUUID())
                .name("Running Shoes")
                .price(new BigDecimal("89.99"))
                .build();

        when(productServiceClient.searchLocalProducts("shoes")).thenReturn(List.of(localProduct));
        when(webSearchAdapter.search("shoes")).thenReturn(List.of());
        when(agentServiceClient.getRecommendations(any())).thenReturn(new RecommendationResponse(List.of()));

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

        SearchResultsResponse response = searchOrchestratorService.searchProducts(request);

        assertThat(response).isNotNull();
        assertThat(response.searchResults().get(0).noOfAgentRecommendations()).isEqualTo(1);
        assertThat(response.searchResults().get(0).items().get(0).name()).isEqualTo("Running Shoes");
    }
}
