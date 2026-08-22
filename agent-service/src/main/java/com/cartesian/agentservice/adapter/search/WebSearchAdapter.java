package com.cartesian.agentservice.adapter.search;

import com.cartesian.agentservice.adapter.search.dto.SerpApiItem;
import com.cartesian.agentservice.adapter.search.dto.SerpApiResponse;
import com.cartesian.agentservice.dto.ProductDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class WebSearchAdapter {
    private static final Logger log = LoggerFactory.getLogger(WebSearchAdapter.class);

    private final WebClient serpApiWebClient;
    private final String apiKey;

    public WebSearchAdapter(
            @Qualifier("serpApiWebClient") WebClient serpApiWebClient,
            @Value("${adapter.serpapi.api.key:}") String apiKey
    ) {
        this.serpApiWebClient = serpApiWebClient;
        this.apiKey = apiKey;
    }

    public List<ProductDto> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("SerpAPI key is missing or blank; skipping external web search");
            return List.of();
        }

        try {
            SerpApiResponse response = serpApiWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search.json")
                            .queryParam("q", query)
                            .queryParam("engine", "google_shopping")
                            .queryParam("api_key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(SerpApiResponse.class)
                    .block();

            if (response == null) {
                return List.of();
            }

            return response.getEffectiveResults().stream()
                    .map(this::toProductDto)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to query SerpAPI for '{}': {}", query, e.getMessage());
            return List.of();
        }
    }

    private ProductDto toProductDto(SerpApiItem item) {
        ProductDto product = new ProductDto();
        product.setId(UUID.randomUUID());
        product.setName(item.title());
        product.setDescription(item.snippet());
        product.setPrice(parsePrice(item.rawPrice()));
        product.setImageUrl(item.imageUrl());
        product.setQuantity(0);
        return product;
    }

    private BigDecimal parsePrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            String cleaned = rawPrice.replaceAll("[^0-9.]", "").trim();
            return cleaned.isEmpty() ? BigDecimal.ZERO : new BigDecimal(cleaned);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
