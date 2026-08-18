package com.cartesian.productservice.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueMappingStrategy;

import com.cartesian.productservice.dto.AgentRecommendation;
import com.cartesian.productservice.dto.Recommendation;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface RecommendationMapper {
    Recommendation toResponse(AgentRecommendation recommendation);

    List<Recommendation> toResponseList(List<AgentRecommendation> recommendations);
}
