package com.cartesian.agentservice.tools;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.cartesian.agentservice.context.ChatTurnContext;
import com.cartesian.agentservice.dto.ProductDto;
import com.cartesian.agentservice.dto.search.Recommendation;
import com.cartesian.agentservice.dto.search.SearchKeywordResult;
import com.cartesian.agentservice.dto.search.SearchRecommendationsRequest;
import com.cartesian.agentservice.dto.search.SearchResultsResponse;
import com.cartesian.agentservice.service.ProductSearchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AgentTools {

    private static final Logger log = LoggerFactory.getLogger(AgentTools.class);

    private final ProductSearchService productSearchService;
    private final ObjectMapper objectMapper;
    private final ChatTurnContext turnContext;

    public AgentTools(ProductSearchService productSearchService,
            ObjectMapper objectMapper,
            ChatTurnContext turnContext) {
        this.productSearchService = productSearchService;
        this.objectMapper = objectMapper;
        this.turnContext = turnContext;
    }

    @Tool(description = "Search for products by keyword. Optionally provide budget in PHP (e.g. 10000.0) and location. Returns a numbered list of matching products.")
    public String searchProducts(String keyword, Double budget, String location) {
        log.info("[TOOL] searchProducts called: keyword='{}', budget={}, location='{}'", keyword, budget, location);
        try {
            JsonNode locationNode = (location != null && !location.isBlank())
                    ? objectMapper.valueToTree(location)
                    : null;

            SearchRecommendationsRequest request = new SearchRecommendationsRequest(
                    List.of(keyword == null ? "" : keyword),
                    locationNode,
                    budget != null ? String.valueOf(budget) : null,
                    null
            );

            SearchResultsResponse response = productSearchService.searchRecommendations(request);

            List<SearchKeywordResult> groups = response == null ? List.of() : response.searchResults();
            if (groups == null || groups.isEmpty()) {
                return "No products found for: " + keyword;
            }

            StringBuilder sb = new StringBuilder();
            int index = 1;

            for (SearchKeywordResult group : groups) {
                if (group.items() == null) {
                    continue;
                }
                for (Recommendation recommendation : group.items()) {
                    JsonNode productNode = recommendation == null ? null : recommendation.product();
                    ProductDto productDto = null;
                    if (productNode != null && productNode.isObject()) {
                        try {
                            productDto = objectMapper.treeToValue(productNode, ProductDto.class);
                            if (productDto != null) {
                                turnContext.addProduct(productDto);
                            }
                        } catch (Exception ex) {
                            log.warn("Could not deserialize product item: {}", ex.getMessage());
                        }
                    }

                    String name = (recommendation != null && recommendation.name() != null && !recommendation.name().isBlank())
                            ? recommendation.name()
                            : (productDto != null && productDto.getName() != null ? productDto.getName() : "Unknown");

                    String desc = productDto != null && productDto.getDescription() != null ? productDto.getDescription() : "";
                    String price = productDto != null && productDto.getPrice() != null ? productDto.getPrice().toPlainString() : null;

                    sb.append("[").append(index++).append("] ").append(name);
                    if (price != null && !price.isBlank()) {
                        sb.append(" — \u20b1").append(price);
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
            ProductDto product = productSearchService.getProductById(UUID.fromString(productId)).orElse(null);

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
