package com.cartesian.agentservice.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.cartesian.agentservice.context.ChatTurnContext;
import com.cartesian.agentservice.dto.ProductDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AgentTools {

    private static final Logger log = LoggerFactory.getLogger(AgentTools.class);

    private final WebClient webClient;
    private final String productServiceUrl;
    private final ObjectMapper objectMapper;
    private final ChatTurnContext turnContext;

    public AgentTools(WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            ChatTurnContext turnContext,
            @Value("${product.service.url:http://localhost:8083}") String productServiceUrl) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.turnContext = turnContext;
        this.productServiceUrl = productServiceUrl;
    }

    @Tool(description = "Search for products by keyword. Optionally provide budget in PHP (e.g. 10000.0) and location. Returns a numbered list of matching products.")
    public String searchProducts(String keyword, Double budget, String location) {
        log.info("[TOOL] searchProducts called: keyword='{}', budget={}, location='{}'", keyword, budget, location);
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            if (keyword != null && !keyword.isBlank()) {
                builder.part("search", keyword);
            }
            if (budget != null) {
                builder.part("budget", String.valueOf(budget));
            }
            if (location != null && !location.isBlank()) {
                builder.part("location", location);
            }

            JsonNode root = webClient.post()
                    .uri(productServiceUrl + "/products/search")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (root == null) {
                return "No products found for: " + keyword;
            }

            JsonNode results = root.path("search_results");
            if (results.isMissingNode() || !results.isArray() || results.isEmpty()) {
                return "No products found for: " + keyword;
            }

            StringBuilder sb = new StringBuilder();
            int index = 1;

            for (JsonNode group : results) {
                JsonNode items = group.path("Items");
                if (!items.isArray()) {
                    continue;
                }
                for (JsonNode item : items) {
                    try {
                        ProductDto productDto = objectMapper.treeToValue(item, ProductDto.class);
                        if (productDto != null) {
                            turnContext.addProduct(productDto);
                        }
                    } catch (Exception ex) {
                        log.warn("Could not deserialize product item: {}", ex.getMessage());
                    }

                    sb.append("[").append(index++).append("] ");
                    sb.append(item.path("name").asText("Unknown"));
                    if (!item.path("price").isMissingNode()) {
                        sb.append(" — \u20b1").append(item.path("price").asText());
                    }
                    String desc = item.path("description").asText("");
                    if (!desc.isBlank()) {
                        sb.append(" | ").append(desc.length() > 150 ? desc.substring(0, 147) + "..." : desc);
                    }
                    sb.append("\n");
                }
            }

            return sb.length() > 0 ? sb.toString().trim() : "No products found for: " + keyword;

        } catch (Exception e) {
            log.error("[TOOL] searchProducts failed: {}", e.getMessage(), e);
            return "Product search is temporarily unavailable. Please try again.";
        }
    }

    @Tool(description = "Get detailed information about a specific product by its UUID.")
    public String getProductById(String productId) {
        log.info("[TOOL] getProductById called: productId='{}'", productId);
        try {
            ProductDto product = webClient.get()
                    .uri(productServiceUrl + "/products/" + productId)
                    .retrieve()
                    .bodyToMono(ProductDto.class)
                    .block();

            if (product == null) {
                return "Product not found: " + productId;
            }

            turnContext.addProduct(product);

            return "Product: " + product.getName()
                    + " | Price: \u20b1" + product.getPrice()
                    + " | Description: " + product.getDescription();

        } catch (Exception e) {
            log.error("[TOOL] getProductById failed: {}", e.getMessage(), e);
            return "Could not retrieve product details. The product may not exist.";
        }
    }
}
