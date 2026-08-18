package com.cartesian.productservice.mapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;

import org.locationtech.jts.geom.Polygon;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cartesian.productservice.dto.ProductSearchRequest;
import com.cartesian.productservice.dto.SearchCriteria;
import com.cartesian.productservice.dto.SearchRecommendationsRequest;
import com.cartesian.productservice.exception.ImageProcessingException;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SearchCriteriaMapper {
    @Mapping(target = "locationText", source = "location")
    @Mapping(target = "locationPolygon", ignore = true)
    @Mapping(target = "imageUrl", source = "request", qualifiedByName = "resolveImageSource")
    SearchCriteria toSearchCriteria(ProductSearchRequest request);

    @Mapping(target = "search", source = "keyword")
    @Mapping(target = "locationText", source = "request", qualifiedByName = "resolveLocationText")
    @Mapping(target = "locationPolygon", source = "request", qualifiedByName = "resolveLocationPolygon")
    @Mapping(target = "minBudget", ignore = true)
    @Mapping(target = "maxBudget", source = "request", qualifiedByName = "resolveBudget")
    @Mapping(target = "minRating", ignore = true)
    @Mapping(target = "maxRating", ignore = true)
    @Mapping(target = "sourceShop", ignore = true)
    @Mapping(target = "imageUrl", source = "request.imageUrl")
    SearchCriteria toSearchCriteria(String keyword, SearchRecommendationsRequest request, @Context ObjectMapper objectMapper);

    @Named("resolveImageSource")
    default String resolveImageSource(ProductSearchRequest request) {
        String imageUrl = request.imageUrl();
        if (StringUtils.hasText(imageUrl)) {
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

    @Named("resolveLocationText")
    default String resolveLocationText(SearchRecommendationsRequest request) {
        JsonNode location = request.location();
        if (location != null && location.isTextual()) {
            String text = location.asText();
            return text.isBlank() ? null : text;
        }
        return null;
    }

    @Named("resolveLocationPolygon")
    default Polygon resolveLocationPolygon(SearchRecommendationsRequest request, @Context ObjectMapper objectMapper) {
        JsonNode location = request.location();
        if (location != null && location.isObject()) {
            return objectMapper.convertValue(location, Polygon.class);
        }
        return null;
    }

    @Named("resolveBudget")
    default BigDecimal resolveBudget(SearchRecommendationsRequest request) {
        String value = request.budget();
        if (value == null) {
            return null;
        }
        if (!StringUtils.hasText(value)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.replace("$", "").replace(",", "").trim());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }
}
