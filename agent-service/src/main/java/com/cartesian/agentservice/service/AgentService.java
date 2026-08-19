package com.cartesian.agentservice.service;

import com.cartesian.agentservice.dto.ProductDto;
import com.cartesian.agentservice.dto.RecommendationItemDto;
import com.cartesian.agentservice.dto.RecommendationRequest;
import com.cartesian.agentservice.dto.RecommendationResponse;
import com.cartesian.agentservice.dto.SearchItemCriteria;
import com.cartesian.agentservice.exception.InvalidSearchCriteriaException;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private final SpatialEvaluator spatialEvaluator;

    public AgentService(SpatialEvaluator spatialEvaluator) {
        this.spatialEvaluator = spatialEvaluator;
    }

    public RecommendationResponse generateRecommendations(RecommendationRequest request) {
        if (request == null || request.getSearchFor() == null || request.getSearchFor().isEmpty()) {
            throw new InvalidSearchCriteriaException("search_for must contain at least one criteria item");
        }

        List<RecommendationItemDto> recommendations = new ArrayList<>();

        for (SearchItemCriteria criteria : request.getSearchFor()) {
            Polygon searchPolygon = spatialEvaluator.parsePolygon(criteria.getLocation());

            List<ProductDto> candidates = criteria.getCandidates();
            if (candidates == null || candidates.isEmpty()) {
                continue;
            }

            for (ProductDto candidate : candidates) {
                boolean isNearby = spatialEvaluator.isWithinPolygon(candidate.getLocation(), searchPolygon);
                if (isNearby) {
                    recommendations.add(new RecommendationItemDto(candidate.getName(), candidate));
                }
            }
        }

        return new RecommendationResponse(recommendations);
    }
}
