package com.retasify.productservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GeocoderClient {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private final RestClient restClient;

    public GeocoderClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://nominatim.openstreetmap.org").build();
    }

    public Polygon geocodeToBoundingBoxPolygon(String location) {
        JsonNode root = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", location)
                        .queryParam("format", "jsonv2")
                        .queryParam("limit", "1")
                        .build())
                .header("User-Agent", "retasify-product-service")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(JsonNode.class);

        if (root == null || !root.isArray() || root.isEmpty()) {
            return null;
        }

        JsonNode bbox = root.get(0).path("boundingbox");
        if (bbox.isMissingNode() || !bbox.isArray() || bbox.size() < 4) {
            return null;
        }

        double south = bbox.get(0).asDouble();
        double north = bbox.get(1).asDouble();
        double west = bbox.get(2).asDouble();
        double east = bbox.get(3).asDouble();

        Coordinate[] coordinates = new Coordinate[] {
                new Coordinate(west, south),
                new Coordinate(east, south),
                new Coordinate(east, north),
                new Coordinate(west, north),
                new Coordinate(west, south)
        };
        LinearRing ring = GEOMETRY_FACTORY.createLinearRing(coordinates);
        return GEOMETRY_FACTORY.createPolygon(ring);
    }
}