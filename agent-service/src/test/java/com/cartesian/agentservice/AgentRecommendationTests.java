package com.cartesian.agentservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cartesian.agentservice.dto.ProductDto;
import com.cartesian.agentservice.dto.RecommendationItemDto;
import com.cartesian.agentservice.dto.RecommendationRequest;
import com.cartesian.agentservice.dto.RecommendationResponse;
import com.cartesian.agentservice.dto.SearchItemCriteria;
import com.cartesian.agentservice.exception.InvalidSearchCriteriaException;
import com.cartesian.agentservice.service.AgentService;
import com.cartesian.agentservice.service.SpatialEvaluator;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AgentRecommendationTests {

    @Autowired
    private AgentService agentService;

    @Autowired
    private SpatialEvaluator spatialEvaluator;

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @Test
    void testSpatialPolygonEvaluation() {
        Coordinate[] coords = new Coordinate[]{
            new Coordinate(120.0, 14.0),
            new Coordinate(121.0, 14.0),
            new Coordinate(121.0, 15.0),
            new Coordinate(120.0, 15.0),
            new Coordinate(120.0, 14.0)
        };
        LinearRing ring = GEOMETRY_FACTORY.createLinearRing(coords);
        Polygon polygon = GEOMETRY_FACTORY.createPolygon(ring);

        Point inside = GEOMETRY_FACTORY.createPoint(new Coordinate(120.5, 14.5));
        Point outside = GEOMETRY_FACTORY.createPoint(new Coordinate(125.0, 18.0));

        assertTrue(spatialEvaluator.isWithinPolygon(inside, polygon));
        assertFalse(spatialEvaluator.isWithinPolygon(outside, polygon));
    }

    @Test
    void testGenerateRecommendationsWithCandidatesInsidePolygon() {
        ProductDto candidate = new ProductDto();
        candidate.setId(UUID.randomUUID());
        candidate.setName("Minimalist Desk");
        candidate.setDescription("Solid oak desk");
        candidate.setPrice(new BigDecimal("8500.00"));
        candidate.setQuantity(5);

        SearchItemCriteria criteria = new SearchItemCriteria("Minimalist Desk", null, null, null);
        criteria.setCandidates(List.of(candidate));

        RecommendationRequest request = new RecommendationRequest(List.of(criteria));
        RecommendationResponse response = agentService.generateRecommendations(request);

        assertNotNull(response);
        assertEquals(1, response.getRecommendations().size());

        RecommendationItemDto item = response.getRecommendations().get(0);
        assertEquals("Minimalist Desk", item.getName());
        assertNotNull(item.getProduct());
    }

    @Test
    void testCandidateOutsidePolygonIsExcluded() {
        Coordinate[] coords = new Coordinate[]{
            new Coordinate(120.0, 14.0),
            new Coordinate(121.0, 14.0),
            new Coordinate(121.0, 15.0),
            new Coordinate(120.0, 15.0),
            new Coordinate(120.0, 14.0)
        };
        LinearRing ring = GEOMETRY_FACTORY.createLinearRing(coords);
        Polygon polygon = GEOMETRY_FACTORY.createPolygon(ring);

        // Build GeoJSON polygon map for the criteria location
        java.util.Map<String, Object> locationMap = new java.util.LinkedHashMap<>();
        locationMap.put("type", "Polygon");
        locationMap.put("coordinates", List.of(List.of(
            List.of(120.0, 14.0),
            List.of(121.0, 14.0),
            List.of(121.0, 15.0),
            List.of(120.0, 15.0),
            List.of(120.0, 14.0)
        )));

        ProductDto outsideCandidate = new ProductDto();
        outsideCandidate.setId(UUID.randomUUID());
        outsideCandidate.setName("Remote Shop Item");
        outsideCandidate.setLocation(GEOMETRY_FACTORY.createPoint(new Coordinate(125.0, 18.0)));

        SearchItemCriteria criteria = new SearchItemCriteria("Remote Shop Item", locationMap, null, null);
        criteria.setCandidates(List.of(outsideCandidate));

        RecommendationRequest request = new RecommendationRequest(List.of(criteria));
        RecommendationResponse response = agentService.generateRecommendations(request);

        assertNotNull(response);
        assertTrue(response.getRecommendations().isEmpty());
    }

    @Test
    void testEmptySearchForThrowsException() {
        RecommendationRequest request = new RecommendationRequest(List.of());
        assertThrows(InvalidSearchCriteriaException.class, () -> agentService.generateRecommendations(request));
    }
}
