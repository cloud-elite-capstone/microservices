package com.cartesian.agentservice.tools;

import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.cartesian.agentservice.adapter.geo.GeoAdapter;
import com.cartesian.agentservice.adapter.search.WebSearchAdapter;
import com.cartesian.agentservice.context.ChatTurnContext;
import com.cartesian.agentservice.dto.ProductDto;

@Component
public class AgentTools {
    private static final Logger log = LoggerFactory.getLogger(AgentTools.class);

    private final WebSearchAdapter webSearchAdapter;
    private final GeoAdapter geoAdapter;
    private final ChatTurnContext turnContext;

    public AgentTools(WebSearchAdapter webSearchAdapter,
            GeoAdapter geoAdapter,
            ChatTurnContext turnContext) {
        this.webSearchAdapter = webSearchAdapter;
        this.geoAdapter = geoAdapter;
        this.turnContext = turnContext;
    }

    @Tool(description = "Search the web for products matching a query. Returns a numbered list with name, price, and description.")
    public String searchWeb(String query) {
        log.info("[TOOL] searchWeb called: query='{}'", query);
        try {
            List<ProductDto> results = webSearchAdapter.search(query);
            if (results == null || results.isEmpty()) {
                return "No web results found for: " + query;
            }

            StringBuilder sb = new StringBuilder();
            int index = 1;
            for (ProductDto product : results) {
                turnContext.addProduct(product);
                sb.append("[").append(index++).append("] ").append(product.getName());
                if (product.getPrice() != null) {
                    sb.append(" — \u20b1").append(product.getPrice().toPlainString());
                }
                if (product.getDescription() != null && !product.getDescription().isBlank()) {
                    sb.append(" | ").append(product.getDescription());
                }
                sb.append("\n");
            }
            return sb.toString().trim();

        } catch (Exception e) {
            log.error("[TOOL] searchWeb failed: {}", e.getMessage(), e);
            return "Web search is temporarily unavailable. Please try again.";
        }
    }

    @Tool(description = "Geocode a location name into its bounding box. Returns the south/north/west/east coordinates.")
    public String geocode(String location) {
        log.info("[TOOL] geocode called: location='{}'", location);
        try {
            Polygon polygon = geoAdapter.geocodeToBoundingBoxPolygon(location);
            if (polygon == null) {
                return "Could not geocode location: " + location;
            }

            Envelope envelope = polygon.getEnvelopeInternal();
            return String.format(
                    "Bounding box for '%s': south=%s, north=%s, west=%s, east=%s",
                    location,
                    envelope.getMinY(),
                    envelope.getMaxY(),
                    envelope.getMinX(),
                    envelope.getMaxX());

        } catch (Exception e) {
            log.error("[TOOL] geocode failed: {}", e.getMessage(), e);
            return "Geocoding is temporarily unavailable. Please try again.";
        }
    }
}
