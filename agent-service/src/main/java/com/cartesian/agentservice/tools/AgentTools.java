package com.cartesian.agentservice.tools;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.cartesian.agentservice.config.IdTokenExchangeFilter;
import com.cartesian.agentservice.context.ChatTurnContext;
import com.cartesian.agentservice.dto.ProductDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class AgentTools {

    private static final Logger log = LoggerFactory.getLogger(AgentTools.class);

    private final WebClient webClient;
    private final String orchestratorUrl;
    private final ObjectMapper objectMapper;
    private final ChatTurnContext turnContext;

    public AgentTools(WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            ChatTurnContext turnContext,
            @Value("${orchestrator.url:http://localhost:8086}") String orchestratorUrl) {
        this.objectMapper = objectMapper;
        this.turnContext = turnContext;
        this.orchestratorUrl = orchestratorUrl;
        try {
            this.webClient = webClientBuilder
                    .filter(new IdTokenExchangeFilter(orchestratorUrl))
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create ID token filter for orchestrator service", e);
        }
    }

    @Tool(description = "Search for products by keyword. Optionally provide budget in PHP (e.g. 10000.0) and location. Returns a numbered list of matching products.")
    public String searchProducts(String keyword, Double budget, String location) {
        log.info("[TOOL] searchProducts called: keyword='{}', budget={}, location='{}'", keyword, budget, location);
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.putArray("search_keywords").add(keyword == null ? "" : keyword);
            if (budget != null) {
                body.put("budget", String.valueOf(budget));
            }
            if (location != null && !location.isBlank()) {
                body.put("location", location);
            }

            String response = webClient.post()
                    .uri(orchestratorUrl + "/orchestrator/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank()) {
                return "No products found for: " + keyword;
            }

            JsonNode root = objectMapper.readTree(response);

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
                    JsonNode productNode = item.path("product");
                    if (!productNode.isObject()) {
                        productNode = item;
                    }

                    try {
                        ProductDto productDto = objectMapper.treeToValue(productNode, ProductDto.class);
                        if (productDto != null) {
                            turnContext.addProduct(productDto);
                        }
                    } catch (Exception ex) {
                        log.warn("Could not deserialize product item: {}", ex.getMessage());
                    }

                    String name = textOr(productNode.path("name"), textOr(item.path("name"), "Unknown"));
                    JsonNode priceNode = productNode.path("price");
                    String desc = textOr(productNode.path("description"), "");

                    sb.append("[").append(index++).append("] ").append(name);
                    if (priceNode != null && priceNode.isValueNode() && !priceNode.isNull()) {
                        sb.append(" — \u20b1").append(priceNode.asText());
                    }
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
                    .uri(orchestratorUrl + "/orchestrator/products/" + productId)
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

    private static String textOr(JsonNode node, String defaultValue) {
        if (node == null || node.isMissingNode() || node.isNull() || !node.isValueNode()) {
            return defaultValue;
        }
        String text = node.asText();
        return (text == null || text.isBlank()) ? defaultValue : text;
    }
}
