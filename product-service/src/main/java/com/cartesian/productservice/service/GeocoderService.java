package com.cartesian.productservice.service;

import com.cartesian.productservice.client.GeocoderClient;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeocoderService {
    private final GeocoderClient geocoderClient;
    private final GeometryFactory geometryFactory;

    public GeocoderService(GeocoderClient geocoderClient, GeometryFactory geometryFactory) {
        this.geocoderClient = geocoderClient;
        this.geometryFactory = geometryFactory;
    }

    public Polygon geocodeToBoundingBoxPolygon(String location) {
        List<GeocoderClient.GeocodeResult> results = geocoderClient.search(location);
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
    }
}
