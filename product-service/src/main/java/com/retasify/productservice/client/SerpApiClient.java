package com.retasify.productservice.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.retasify.productservice.model.Product;

@Component
public class SerpApiClient {
    private static final Logger log = LoggerFactory.getLogger(SerpApiClient.class);

    private final RestClient restClient;
    private final String apiKey;

    public SerpApiClient(RestClient.Builder builder,
                         @Value("${serpapi.api.key:}") String apiKey) {
        this.restClient = builder.baseUrl("https://serpapi.com").build();
        this.apiKey = apiKey;
    }

    public List<Product> search(String keyword) {
        // TODO: should throw exception
        if (apiKey == null || apiKey.isBlank()) {
            return List.of();
        }

        JsonNode root = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search.json")
                        .queryParam("engine", "google_product")
                        .queryParam("q", keyword)
                        .queryParam("api_key", apiKey)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(JsonNode.class);

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
            product.setPrice(parsePrice(item.path("price").asText(null)));
            product.setQuantity(0);
            product.setLocation(null);
            product.setCategoryId(null);
            product.setImageUrl(item.path("thumbnail").asText(item.path("image").asText(null)));
            product.setShopId(null);
            converted.add(product);
        }
        return converted;
    }

    private BigDecimal parsePrice(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.replace("$", "").replace(",", "").trim());
        } catch (Exception ex) {
            log.warn("Unparseable price '{}'", value);
            return BigDecimal.ZERO;
        }
    }
}