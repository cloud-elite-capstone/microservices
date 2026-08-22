package com.cartesian.agentservice.adapter.geo;

import com.cartesian.agentservice.adapter.geo.dto.GeocodeResult;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class GeoAdapter {
    private static final Logger log = LoggerFactory.getLogger(GeoAdapter.class);

    private final WebClient geocoderWebClient;
    private final GeometryFactory geometryFactory;

    public GeoAdapter(@Qualifier("geocoderWebClient") WebClient geocoderWebClient,
                      GeometryFactory geometryFactory) {
        this.geocoderWebClient = geocoderWebClient;
        this.geometryFactory = geometryFactory;
    }

    public Polygon geocodeToBoundingBoxPolygon(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }

        try {
            List<GeocodeResult> results = geocoderWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", location)
                            .queryParam("format", "jsonv2")
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GeocodeResult>>() {})
                    .block();

            if (results == null || results.isEmpty()) {
                return null;
            }

            List<Double> bbox = results.get(0).boundingBox();
            if (bbox == null || bbox.size() < 4) {
                return null;
            }

            double south = bbox.get(0);
            double north = bbox.get(1);
            double west = bbox.get(2);
            double east = bbox.get(3);

            Envelope envelope = new Envelope(west, east, south, north);
            return (Polygon) geometryFactory.toGeometry(envelope);
        } catch (Exception e) {
            log.error("Failed to geocode location '{}': {}", location, e.getMessage());
            return null;
        }
    }
}
