package com.cartesian.productservice.service;

import com.cartesian.productservice.client.SerpApiClient;
import com.cartesian.productservice.client.dto.serpapi.SerpApiResponse;
import com.cartesian.productservice.mapper.ProductMapper;
import com.cartesian.productservice.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class ProductSearchService {
    private static final Logger log = LoggerFactory.getLogger(ProductSearchService.class);

    private final SerpApiClient serpApiClient;
    private final ProductMapper productMapper;
    private final String apiKey;

    public ProductSearchService(
            SerpApiClient serpApiClient,
            ProductMapper productMapper,
            @Value("${client.serpapi.api.key}") String apiKey
    ) {
        this.serpApiClient = serpApiClient;
        this.productMapper = productMapper;
        this.apiKey = apiKey;
    }

    public List<Product> search(String keyword) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("SerpAPI key is missing or blank");
            return List.of();
        }

        SerpApiResponse response = serpApiClient.search(keyword, "google_shopping", apiKey);
        if (response == null) {
            return List.of();
        }

        return response.getEffectiveResults().stream()
                .map(productMapper::toEntity)
                .toList();
    }
}
